/*
 * This is the source code of Telegram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package com.couchgram.action_bar.ActionBar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.couchgram.action_bar.Components.LayoutHelper;
import com.couchgram.action_bar.android.AndroidUtilities;

public class ActionBarMenu extends LinearLayout {

    protected ActionBar parentActionBar;

    public ActionBarMenu(Context context, ActionBar layer) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
        parentActionBar = layer;
    }

    public ActionBarMenu(Context context) {
        super(context);
    }

    public ActionBarMenuItem addItem(int id, Drawable drawable) {
        return addItem(id, 0, parentActionBar.itemsBackgroundResourceId, drawable, AndroidUtilities.dp(48));
    }

    public ActionBarMenuItem addItem(int id, int icon) {
        return addItem(id, icon, parentActionBar.itemsBackgroundResourceId);
    }

    public ActionBarMenuItem addItem(int id, int icon, int backgroundResource) {
        return addItem(id, icon, backgroundResource, null, AndroidUtilities.dp(48));
    }

    public ActionBarMenuItem addItem(int id, int icon, int backgroundResource, Drawable drawable, int width) {
        ActionBarMenuItem menuItem = new ActionBarMenuItem(getContext(), this, backgroundResource);
        menuItem.setTag(id);
        if (drawable != null) {
            menuItem.iconView.setImageDrawable(drawable);
        } else {
            menuItem.iconView.setImageResource(icon);
        }
        addView(menuItem);
        LayoutParams layoutParams = (LayoutParams) menuItem.getLayoutParams();
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        layoutParams.width = width;
        menuItem.setLayoutParams(layoutParams);
        menuItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionBarMenuItem item = (ActionBarMenuItem) view;
                if (item.hasSubMenu()) {
                    if (parentActionBar.actionBarMenuOnItemClick.canOpenMenu()) {
                        item.toggleSubMenu();
                    }
                } else if (item.isSearchField()) {
                    parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch());
                } else {
                    onItemClick((Integer) view.getTag());
                }
            }
        });
        return menuItem;
    }

    public void hideAllPopupMenus() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ((ActionBarMenuItem) view).closeSubMenu();
            }
        }
    }

    public void onItemClick(int id) {
        if (parentActionBar.actionBarMenuOnItemClick != null) {
            parentActionBar.actionBarMenuOnItemClick.onItemClick(id);
        }
    }

    public void closeSearchField() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ActionBarMenuItem item = (ActionBarMenuItem) view;
                if (item.isSearchField()) {
                    EditText search = item.getSearchField();
                    if(search != null) {
                        search.setText("");
                    }
                    parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch());
                    break;
                }
            }
        }
    }
}
