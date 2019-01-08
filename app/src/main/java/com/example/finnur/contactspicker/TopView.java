// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

//import org.chromium.chrome.R;

/**
 * A container class for the Disclaimer and Select All functionality (and both associated labels).
 */
public class TopView extends LinearLayout implements CompoundButton.OnCheckedChangeListener {
    // The container box for the checkbox and its label and contact count.
    private View mCheckboxContainer;

    // The Select All checkbox.
    private CheckBox mSelectAllBox;

    // The label showing how many contacts were found.
    private TextView mContactCount;

    // Our parent PickerCategoryView.
    private PickerCategoryView mCategoryView;

    // Whether to temporarily ignore clicks on the checkbox.
    private boolean mIgnoreCheck;

    public TopView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCategoryView(PickerCategoryView categoryView) {
        mCategoryView = categoryView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mCheckboxContainer = findViewById(R.id.container);
        // TODO(finnur): Plumb through the necessary data to show which website will be receiving
        //               the contact data.
        mSelectAllBox = findViewById(R.id.select_all_checkbox);
        mContactCount = findViewById(R.id.contact_count);
    }

    public void updateViewVisibility() {
        if (mCategoryView.multiSelectionAllowed()) {
            mSelectAllBox.setOnCheckedChangeListener(this);
        } else {
            mCheckboxContainer.setVisibility(GONE);
        }
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
    public void updateSelectAllCheckbox(boolean allSelected) {
        mIgnoreCheck = true;
        mSelectAllBox.setChecked(allSelected);
        mIgnoreCheck = false;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!mIgnoreCheck) mCategoryView.toggleSelectAll(mSelectAllBox.isChecked());
    }
}
