/*
 * This is the source code of Telegram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package com.couchgram.action_bar.ActionBar;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchgram.action_bar.Components.LayoutHelper;
import com.couchgram.action_bar.android.AndroidUtilities;

public class ActionBar extends FrameLayout {

    private static final int DEFAULT_TITLE_SIZE = 18;

    public static class ActionBarMenuOnItemClick {
        public void onItemClick(int id) {

        }

        public boolean canOpenMenu() {
            return true;
        }
    }

    private ImageView backButtonImageView;
    private TextView titleTextView;
    private TextView subTitleTextView;
    private ActionBarMenu menu;
    private boolean occupyStatusBar = Build.VERSION.SDK_INT >= 21;

    protected boolean isSearchFieldVisible;
    protected int itemsBackgroundResourceId;
    public ActionBarMenuOnItemClick actionBarMenuOnItemClick;

    private static Context mContext;

    public ActionBar(Context context) {
        super(context);
        mContext = context;
    }

    private void createBackButtonImage() {
        if (backButtonImageView != null) {
            return;
        }
        backButtonImageView = new ImageView(getContext());
        backButtonImageView.setScaleType(ImageView.ScaleType.CENTER);
        backButtonImageView.setBackgroundResource(itemsBackgroundResourceId);
        addView(backButtonImageView, LayoutHelper.createFrame(54, 54, Gravity.LEFT | Gravity.TOP));

        backButtonImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSearchFieldVisible) {
                    closeSearchField();
                    return;
                }
                if (actionBarMenuOnItemClick != null) {
                    actionBarMenuOnItemClick.onItemClick(-1);
                }
            }
        });
    }

    public void setBackButtonDrawable(boolean is_arrow, Drawable drawable) {
        if (backButtonImageView == null) {
            createBackButtonImage();
        }
        if (is_arrow) {
            if (drawable != null && drawable instanceof MenuDrawable) {
                ((MenuDrawable) drawable).setRotation(1, false);
            }
        }
        backButtonImageView.setVisibility(drawable == null ? GONE : VISIBLE);
        backButtonImageView.setImageDrawable(drawable);
    }

    public void setBackButtonImage(int resource) {
        if (backButtonImageView == null) {
            createBackButtonImage();
        }
        backButtonImageView.setVisibility(resource == 0 ? GONE : VISIBLE);
        backButtonImageView.setImageResource(resource);
    }

    private void createSubtitleTextView() {
        if (subTitleTextView != null) {
            return;
        }
        subTitleTextView = new TextView(getContext());
        subTitleTextView.setGravity(Gravity.LEFT);
        subTitleTextView.setTextColor(0xffd7e8f7);
        subTitleTextView.setSingleLine(true);
        subTitleTextView.setLines(1);
        subTitleTextView.setMaxLines(1);
        subTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(subTitleTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
    }

    public void setSubtitle(CharSequence value) {
        if (value != null && subTitleTextView == null) {
            createSubtitleTextView();
        }
        if (subTitleTextView != null) {
            subTitleTextView.setVisibility(value != null && !isSearchFieldVisible ? VISIBLE : INVISIBLE);
            subTitleTextView.setText(value);
        }
    }

    private void createTitleTextView() {
        if (titleTextView != null) {
            return;
        }
        titleTextView = new TextView(getContext());
        titleTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        titleTextView.setMaxLines(2);
        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
        titleTextView.setTextColor(0xffffffff);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TITLE_SIZE);
        addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT));
    }

    public void setTitle(CharSequence value) {
        if (value != null && titleTextView == null) {
            createTitleTextView();
        }
        if (titleTextView != null) {
            titleTextView.setVisibility(value != null && !isSearchFieldVisible ? VISIBLE : INVISIBLE);
            titleTextView.setText(value);
        }
    }

    public String getTitle() {
        if (titleTextView == null) {
            return null;
        }
        return titleTextView.getText().toString();
    }

    public ActionBarMenu createMenu() {
        if (menu != null) {
            return menu;
        }
        menu = new ActionBarMenu(getContext(), this);
        addView(menu);
        LayoutParams layoutParams = (LayoutParams) menu.getLayoutParams();
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        layoutParams.width = LayoutHelper.WRAP_CONTENT;
        layoutParams.gravity = Gravity.RIGHT;
        menu.setLayoutParams(layoutParams);
        return menu;
    }

    public void setActionBarMenuOnItemClick(ActionBarMenuOnItemClick listener) {
        actionBarMenuOnItemClick = listener;
    }

    protected void onSearchFieldVisibilityChanged(boolean visible) {
        isSearchFieldVisible = visible;
        if (titleTextView != null) {
            titleTextView.setVisibility(visible ? INVISIBLE : VISIBLE);
        }
        if (subTitleTextView != null) {
            subTitleTextView.setVisibility(visible ? INVISIBLE : VISIBLE);
        }
        Drawable drawable = backButtonImageView.getDrawable();
        if (drawable != null && drawable instanceof MenuDrawable) {
            ((MenuDrawable) drawable).setRotation(visible ? 1 : 0, true);
        }
    }

    public void closeSearchField() {
        if (!isSearchFieldVisible || menu == null) {
            return;
        }
        menu.closeSearchField();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int actionBarHeight = getCurrentActionBarHeight();
        int actionBarHeightSpec = MeasureSpec.makeMeasureSpec(actionBarHeight, MeasureSpec.EXACTLY);

        setMeasuredDimension(width, actionBarHeight + (occupyStatusBar ? AndroidUtilities.statusBarHeight : 0));

        int textLeft;
        if (backButtonImageView != null && backButtonImageView.getVisibility() != GONE) {
            backButtonImageView.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(54), MeasureSpec.EXACTLY), actionBarHeightSpec);
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 80 : 72);
        } else {
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 26 : 18);
        }

        if (menu != null && menu.getVisibility() != GONE) {
            int menuWidth;
            if (isSearchFieldVisible) {
                menuWidth = MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(AndroidUtilities.isTablet() ? 74 : 66), MeasureSpec.EXACTLY);
            } else {
                menuWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
            }
            menu.measure(menuWidth, actionBarHeightSpec);
        }

        if (titleTextView != null && titleTextView.getVisibility() != GONE || subTitleTextView != null && subTitleTextView.getVisibility() != GONE) {
            int availableWidth = width - (menu != null ? menu.getMeasuredWidth() : 0) - AndroidUtilities.dp(16) - textLeft;

            if (titleTextView != null && titleTextView.getVisibility() != GONE) {
                int titleTextSize = (int) AndroidUtilities.dpFromPx(titleTextView.getTextSize());
                if (DEFAULT_TITLE_SIZE == titleTextSize) {
                    int adjustTextSize;
                    int lineCount = AndroidUtilities.getTextViewLineCount(titleTextView, getTitle(), availableWidth);
                    if (lineCount == 1)
                        adjustTextSize = DEFAULT_TITLE_SIZE;
                    else if (lineCount == 2)
                        adjustTextSize = DEFAULT_TITLE_SIZE - 2;
                    else
                        adjustTextSize = DEFAULT_TITLE_SIZE - 4;
                    titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, adjustTextSize);
                }
                titleTextView.measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(actionBarHeight, MeasureSpec.AT_MOST));
            }
            if (subTitleTextView != null && subTitleTextView.getVisibility() != GONE) {
                subTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, !AndroidUtilities.isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 14 : 16);
                subTitleTextView.measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(actionBarHeight, MeasureSpec.AT_MOST));
            }
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE || child == titleTextView || child == subTitleTextView || child == menu || child == backButtonImageView) {
                continue;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY), 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int additionalTop = occupyStatusBar ? AndroidUtilities.statusBarHeight : 0;

        int textLeft;
        if (backButtonImageView != null && backButtonImageView.getVisibility() != GONE) {
            backButtonImageView.layout(0, additionalTop, backButtonImageView.getMeasuredWidth(), additionalTop + backButtonImageView.getMeasuredHeight());
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 80 : 58);
        } else {
            textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 26 : 18);
        }

        if (menu != null && menu.getVisibility() != GONE) {
            int menuLeft = isSearchFieldVisible ? AndroidUtilities.dp(AndroidUtilities.isTablet() ? 74 : 66) : (right - left) - menu.getMeasuredWidth();
            menu.layout(menuLeft, additionalTop, menuLeft + menu.getMeasuredWidth(), additionalTop + menu.getMeasuredHeight());
        }

        int offset = AndroidUtilities.dp(!AndroidUtilities.isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 1 : 2);
        if (titleTextView != null && titleTextView.getVisibility() != GONE) {
            int textTop;
            if (subTitleTextView != null && subTitleTextView.getVisibility() != GONE) {
                textTop = (getCurrentActionBarHeight() / 2 - titleTextView.getMeasuredHeight()) / 2 + offset;
            } else {
                textTop = (getCurrentActionBarHeight() - titleTextView.getMeasuredHeight()) / 2 - AndroidUtilities.dp(1);
            }
            titleTextView.layout(textLeft, additionalTop + textTop, textLeft + titleTextView.getMeasuredWidth(), additionalTop + textTop + titleTextView.getMeasuredHeight());
        }
        if (subTitleTextView != null && subTitleTextView.getVisibility() != GONE) {
            int textTop = getCurrentActionBarHeight() / 2 + (getCurrentActionBarHeight() / 2 - subTitleTextView.getMeasuredHeight()) / 2 - offset;
            subTitleTextView.layout(textLeft, additionalTop + textTop, textLeft + subTitleTextView.getMeasuredWidth(), additionalTop + textTop + subTitleTextView.getMeasuredHeight());
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE || child == titleTextView || child == subTitleTextView || child == menu || child == backButtonImageView) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            int childLeft;
            int childTop;

            int gravity = lp.gravity;
            if (gravity == -1) {
                gravity = Gravity.TOP | Gravity.LEFT;
            }

            final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

            switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL:
                    childLeft = (right - left - width) / 2 + lp.leftMargin - lp.rightMargin;
                    break;
                case Gravity.RIGHT:
                    childLeft = right - width - lp.rightMargin;
                    break;
                case Gravity.LEFT:
                default:
                    childLeft = lp.leftMargin;
            }

            switch (verticalGravity) {
                case Gravity.TOP:
                    childTop = lp.topMargin;
                    break;
                case Gravity.CENTER_VERTICAL:
                    childTop = (bottom - top - height) / 2 + lp.topMargin - lp.bottomMargin;
                    break;
                case Gravity.BOTTOM:
                    childTop = (bottom - top) - height - lp.bottomMargin;
                    break;
                default:
                    childTop = lp.topMargin;
            }
            child.layout(childLeft, childTop, childLeft + width, childTop + height);
        }
    }

    protected void onPause() {
        if (menu != null) {
            menu.hideAllPopupMenus();
        }
    }

    public void setAllowOverlayTitle(boolean value) {
    }

    public void setItemsBackground(int resourceId) {
        itemsBackgroundResourceId = resourceId;
        if (backButtonImageView != null) {
            backButtonImageView.setBackgroundResource(itemsBackgroundResourceId);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    public static int getCurrentActionBarHeight() {
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return AndroidUtilities.dp(48);
        } else {
            return AndroidUtilities.dp(56);
        }
    }

    public static Context getCouchContext() {
        return mContext;
    }
}