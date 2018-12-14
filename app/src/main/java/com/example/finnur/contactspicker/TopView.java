// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.view.menu.MenuView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.chromium.base.ApiCompatibilityUtils;
//import org.chromium.chrome.R;
import org.chromium.chrome.browser.widget.selection.SelectableItemView;
import org.chromium.chrome.browser.widget.selection.SelectionDelegate;

import java.util.List;

/**
 * A container class for a view showing a contact in the Contacts Picker.
 */
public class TopView extends LinearLayout implements CompoundButton.OnCheckedChangeListener {
    private CheckBox mSelectAllBox;
    private TextView mContactCount;
    private PickerCategoryView mCategoryView;
    private boolean mIgnoreCheck;

    public TopView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCategoryView(PickerCategoryView categoryView) {
        mCategoryView = categoryView;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();

        //if (multiSelectionAllowed) {
        mSelectAllBox = (CheckBox) findViewById(R.id.select_all_checkbox);
        mSelectAllBox.setOnCheckedChangeListener(this);

        mContactCount = (TextView) findViewById(R.id.contact_count);
        //} else {
        //    mSelectAllBox.setVisibility(GONE);
        //}
    }

    public void updateContactCount(int count) {
        mContactCount.setText(String.valueOf(count));
    }

    public void toggle() {
        mSelectAllBox.setChecked(!mSelectAllBox.isChecked());
    }

    /**
     * Updates the state of the checkbox to reflect whether everything is selected.
     * @param allSelected
     */
    public void syncCheckbox(boolean allSelected) {
        mIgnoreCheck = true;
        mSelectAllBox.setChecked(allSelected);
        mIgnoreCheck = false;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!mIgnoreCheck) mCategoryView.toggleSelectAll(mSelectAllBox.isChecked());
    }
}
