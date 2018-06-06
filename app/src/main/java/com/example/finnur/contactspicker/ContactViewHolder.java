// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import android.support.v7.widget.RecyclerView.ViewHolder;

//import org.chromium.chrome.R;


/**
 * Holds on to a {@link ContactView} that displays information about a picker bitmap.
 */
public class ContactViewHolder extends ViewHolder {
    // The bitmap view we are holding on to.
    private final ContactView mItemView;

    /**
     * The ContactViewHolder.
     * @param itemView The {@link ContactView} view for showing the contact details.
     */
    public ContactViewHolder(ContactView itemView) {
        super(itemView);
        mItemView = itemView;
    }

    /**
     * Display a single contact, using the |contactDetails| provided.
     * @param contactDetails The details for the contact to display.
     */
    public void displayItem(ContactDetails contactDetails) {
        mItemView.initialize(contactDetails);
    }
}
