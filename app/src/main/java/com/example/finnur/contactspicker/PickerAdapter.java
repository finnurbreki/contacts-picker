// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A data adapter for the Contacts Picker.
 */
public class PickerAdapter extends Adapter<ViewHolder> {
    // The category view to use to show the contacts.
    private PickerCategoryView mCategoryView;

    // A cursor containing the raw contacts data.
    private Cursor mContactsCursor;

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
    };

    /**
     * The PickerAdapter constructor.
     * @param categoryView The category view to use to show the contacts.
     */
    public PickerAdapter(PickerCategoryView categoryView) {
        mCategoryView = categoryView;

        mContactsCursor = mCategoryView.getActivity().getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, PROJECTION,
                null, null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC");
    }

    // RecyclerView.Adapter:

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.contact_view, parent, false);
        ContactView bitmapView = (ContactView) itemView;
        bitmapView.setCategoryView(mCategoryView);
        return new ContactViewHolder(bitmapView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof ContactViewHolder) {
            ContactViewHolder myHolder = (ContactViewHolder) holder;

            String name = "";
            if (mContactsCursor.moveToPosition(position)) {
                name = mContactsCursor.getString(mContactsCursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
            }

            // Look up all associated emails for this contact. Would be nice to be able to do
            // this in one go with the original cursor...
            String id = mContactsCursor.getString(
                    mContactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor emailCursor = getEmailCursor(id);
            ArrayList<String> emails = new ArrayList<String>();
            while (emailCursor.moveToNext()) {
                emails.add(emailCursor.getString(
                        emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
            }
            emailCursor.close();

            ContactDetails contactDetails = new ContactDetails(name, emails);
            myHolder.displayItem(contactDetails);
        }
    }

    private Cursor getEmailCursor(String id) {
        Cursor emailCursor = mCategoryView.getActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id,
                null, ContactsContract.CommonDataKinds.Email.DATA + " ASC");
        return emailCursor;
    }

    @Override
    public int getItemCount() {
        return mContactsCursor.getCount();
    }
}
