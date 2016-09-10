/*
 * This is the source code of Telegram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

//Thanks to https://github.com/JakeWharton/ActionBarSherlock/

package com.couchgram.action_bar.ActionBar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;


import com.couchgram.action_bar.Components.LayoutHelper;
import com.couchgram.action_bar.R;
import com.couchgram.action_bar.android.AndroidUtilities;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ActionBarPopupWindow extends PopupWindow {

    private static final Field superListenerField;
    private static final boolean animationEnabled = Build.VERSION.SDK_INT >= 18;
    private static DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private AnimatorSet windowAnimatorSet;
    static {
        Field f = null;
        try {
            f = PopupWindow.class.getDeclaredField("mOnScrollChangedListener");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            /* ignored */
        }
        superListenerField = f;
    }

    private static final ViewTreeObserver.OnScrollChangedListener NOP = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            /* do nothing */
        }
    };

    private ViewTreeObserver.OnScrollChangedListener mSuperScrollListener;
    private ViewTreeObserver mViewTreeObserver;

    public interface OnDispatchKeyEventListener {
        void onDispatchKeyEvent(KeyEvent keyEvent);
    }

    public static class ActionBarPopupWindowLayout extends FrameLayout {

        private OnDispatchKeyEventListener mOnDispatchKeyEventListener;
        protected static Drawable backgroundDrawable;
        private float backScaleX = 1;
        private float backScaleY = 1;
        private int backAlpha = 255;
        private int lastStartedChild = 0;
        private boolean showedFromBotton;
        private HashMap<View, Integer> positions = new HashMap<>();

        private ScrollView scrollView;
        private LinearLayout linearLayout;

        public ActionBarPopupWindowLayout(Context context) {
            super(context);

            if (backgroundDrawable == null) {
                backgroundDrawable = getResources().getDrawable(R.drawable.popup_fixed);
            }

            setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
            setWillNotDraw(false);

            scrollView = new ScrollView(context);
            scrollView.setVerticalScrollBarEnabled(false);
            addView(scrollView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

            linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(linearLayout, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        @Override
        public void addView(View child) {
            linearLayout.addView(child);
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (mOnDispatchKeyEventListener != null) {
                mOnDispatchKeyEventListener.onDispatchKeyEvent(event);
            }
            return super.dispatchKeyEvent(event);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (backgroundDrawable != null) {
                backgroundDrawable.setAlpha(backAlpha);
                if (showedFromBotton) {
                    backgroundDrawable.setBounds(0, (int) (getMeasuredHeight() * (1.0f - backScaleY)), (int) (getMeasuredWidth() * backScaleX), getMeasuredHeight());
                } else {
                    backgroundDrawable.setBounds(0, 0, (int) (getMeasuredWidth() * backScaleX), (int) (getMeasuredHeight() * backScaleY));
                }
                backgroundDrawable.draw(canvas);
            }
        }

        public int getItemsCount() {
            return linearLayout.getChildCount();
        }

        public View getItemAt(int index) {
            return linearLayout.getChildAt(index);
        }

        public void scrollToTop() {
            scrollView.scrollTo(0, 0);
        }
    }

    public ActionBarPopupWindow(View contentView, int width, int height) {
        super(contentView, width, height);
        init();
    }

    private void init() {
        if (superListenerField != null) {
            try {
                mSuperScrollListener = (ViewTreeObserver.OnScrollChangedListener) superListenerField.get(this);
                superListenerField.set(this, NOP);
            } catch (Exception e) {
                mSuperScrollListener = null;
            }
        }
    }

    private void unregisterListener() {
        if (mSuperScrollListener != null && mViewTreeObserver != null) {
            if (mViewTreeObserver.isAlive()) {
                mViewTreeObserver.removeOnScrollChangedListener(mSuperScrollListener);
            }
            mViewTreeObserver = null;
        }
    }

    private void registerListener(View anchor) {
        if (mSuperScrollListener != null) {
            ViewTreeObserver vto = (anchor.getWindowToken() != null) ? anchor.getViewTreeObserver() : null;
            if (vto != mViewTreeObserver) {
                if (mViewTreeObserver != null && mViewTreeObserver.isAlive()) {
                    mViewTreeObserver.removeOnScrollChangedListener(mSuperScrollListener);
                }
                if ((mViewTreeObserver = vto) != null) {
                    vto.addOnScrollChangedListener(mSuperScrollListener);
                }
            }
        }
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        try {
            super.showAsDropDown(anchor, xoff, yoff);
            registerListener(anchor);
        } catch (Exception e) {
//            FileLog.e("tmessages", e);
        }
    }

    public void startAnimation() {
        if (animationEnabled) {
            if (windowAnimatorSet != null) {
                return;
            }
            ActionBarPopupWindowLayout content = (ActionBarPopupWindowLayout) getContentView();
            content.setTranslationY(0);
            content.setAlpha(1.0f);
            content.setPivotX(content.getMeasuredWidth());
            content.setPivotY(0);
            int count = content.getItemsCount();
            content.positions.clear();
            int visibleCount = 0;
            for (int a = 0; a < count; a++) {
                View child = content.getItemAt(a);
                if (child.getVisibility() != View.VISIBLE) {
                    continue;
                }
                content.positions.put(child, visibleCount);
                child.setAlpha(0.0f);
                visibleCount++;
            }
            if (content.showedFromBotton) {
                content.lastStartedChild = count - 1;
            } else {
                content.lastStartedChild = 0;
            }
            windowAnimatorSet = new AnimatorSet();
            windowAnimatorSet.playTogether(
                    ObjectAnimator.ofFloat(content, "backScaleY", 0.0f, 1.0f),
                    ObjectAnimator.ofInt(content, "backAlpha", 0, 255));
            windowAnimatorSet.setDuration(150 + 16 * visibleCount);
            windowAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    windowAnimatorSet = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            windowAnimatorSet.start();
        }
    }

    @Override
    public void update(View anchor, int xoff, int yoff, int width, int height) {
        super.update(anchor, xoff, yoff, width, height);
        registerListener(anchor);
    }

    @Override
    public void update(View anchor, int width, int height) {
        super.update(anchor, width, height);
        registerListener(anchor);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        unregisterListener();
    }

    @Override
    public void dismiss() {
        dismiss(true);
    }

    public void dismiss(boolean animated) {
        if (animationEnabled && animated) {
            if (windowAnimatorSet != null) {
                windowAnimatorSet.cancel();
            }
            ActionBarPopupWindowLayout content = (ActionBarPopupWindowLayout) getContentView();
            windowAnimatorSet = new AnimatorSet();
            windowAnimatorSet.playTogether(
                    ObjectAnimator.ofFloat(content, "translationY", AndroidUtilities.dp(content.showedFromBotton ? 5 : -5)),
                    ObjectAnimator.ofFloat(content, "alpha", 0.0f));
            windowAnimatorSet.setDuration(150);
            windowAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    windowAnimatorSet = null;
                    setFocusable(false);
                    try {
                        ActionBarPopupWindow.super.dismiss();
                    } catch (Exception e) {
                        //don't promt
                    }
                    unregisterListener();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            windowAnimatorSet.start();
        } else {
            setFocusable(false);
            try {
                super.dismiss();
            } catch (Exception e) {
                //don't promt
            }
            unregisterListener();
        }
    }
}
