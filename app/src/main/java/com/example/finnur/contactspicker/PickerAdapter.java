// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import android.content.ContentResolver;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.chromium.base.VisibleForTesting;
//import org.chromium.base.task.AsyncTask;
//import org.chromium.chrome.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A data adapter for the Contacts Picker.
 */
public class PickerAdapter extends Adapter<RecyclerView.ViewHolder>
        implements ContactsFetcherWorkerTask.ContactsRetrievedCallback {

    /**
     * A ViewHolder for the top-most view in the RecyclerView. The view it contains has a
     * checkbox and some multi-line text that goes with it, so clicks on either text line
     * should be treated as clicks for the checkbox (hence the onclick forwarding).
     */
    class TopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TopView mItemView;

        public TopViewHolder(TopView itemView) {
            super(itemView);
            mItemView = itemView;
            mItemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // TODO(finnur): Make the explanation text non-clickable.
            mItemView.toggle();
        }
    }

    // The category view to use to show the contacts.
    private PickerCategoryView mCategoryView;

    // The view at the top of the RecyclerView (disclaimer and select all functionality).
    private TopView mTopView;

    // The content resolver to query data from.
    private ContentResolver mContentResolver;

    // The full list of all registered contacts on the device.
    private ArrayList<ContactDetails> mContactDetails;

    // The async worker task to use for fetching the contact details.
    private ContactsFetcherWorkerTask mWorkerTask;

    // Whether the user has switched to search mode.
    private boolean mSearchMode;

    // A list of search result indices into the larger data set.
    private ArrayList<Integer> mSearchResults;

    // A list of contacts to use for testing (instead of querying Android).
    private static ArrayList<ContactDetails> sTestContacts;

    /**
     * The PickerAdapter constructor.
     * @param categoryView The category view to use to show the contacts.
     * @param contentResolver The content resolver to use to fetch the data.
     */
    public PickerAdapter(PickerCategoryView categoryView, ContentResolver contentResolver) {
        mCategoryView = categoryView;
        mContentResolver = contentResolver;

        if (getAllContacts() == null && sTestContacts == null) {
            mWorkerTask = new ContactsFetcherWorkerTask(mContentResolver, this);
            mWorkerTask.execute();
        } else {
            mContactDetails = sTestContacts;
            notifyDataSetChanged();
        }
    }

    /**
     * Set whether the user has switched to search mode.
     * @param searchMode True when we are in search mode.
     */
    public void setSearchMode(boolean searchMode) {
        mSearchMode = searchMode;
        notifyDataSetChanged();
    }

    /**
     * Sets the search query (filter) for the contact list. Filtering is by display name.
     * @param query The search term to use.
     */
    public void setSearchString(String query) {
        if (query.equals("")) {
            if (mSearchResults == null) return;
            mSearchResults.clear();
            mSearchResults = null;
        } else {
            mSearchResults = new ArrayList<Integer>();
            Integer count = 0;
            String query_lower = query.toLowerCase(Locale.getDefault());
            for (ContactDetails contact : mContactDetails) {
                if (contact.getDisplayName().toLowerCase(Locale.getDefault()).contains(query_lower)
                        || contact.getContactDetailsAsString(/*longVersion=*/ true)
                                   .toLowerCase(Locale.getDefault())
                                   .contains(query_lower)) {
                    mSearchResults.add(count);
                }
                count++;
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Fetches all known contacts.
     * @return The contact list as an array.
     */
    public ArrayList<ContactDetails> getAllContacts() {
        return mContactDetails;
    }

    // ContactsFetcherWorkerTask.ContactsRetrievedCallback:

    @Override
    public void contactsRetrieved(ArrayList<ContactDetails> contacts) {
        mContactDetails = contacts;
        if (mTopView != null) mTopView.updateContactCount(mContactDetails.size());
        notifyDataSetChanged();
    }

    // RecyclerView.Adapter:

    @Override
    public int getItemViewType(int position) {
        if (mSearchMode) return 1;
        if (position == 0) return 0;
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0: {
                mTopView = (TopView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.top_view, parent, false);
                mTopView.setCategoryView(mCategoryView);
                mCategoryView.setTopView(mTopView);
                if (mContactDetails != null) mTopView.updateContactCount(mContactDetails.size());
                return new TopViewHolder(mTopView);
            }
            case 1: {
                ContactView itemView = (ContactView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.contact_view, parent, false);
                itemView.setCategoryView(mCategoryView);
                return new ContactViewHolder(itemView, mCategoryView, mContentResolver);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                int i = 0;
                return;
            case 1:
                ContactViewHolder contactHolder = (ContactViewHolder) holder;
                ContactDetails contact;
                if (!mSearchMode || mSearchResults == null) {
                    // Subtract one because the first view is the Select All checkbox.
                    contact = mContactDetails.get(position - (mSearchMode ? 0 : 1));
                } else {
                    Integer index = mSearchResults.get(position);
                    contact = mContactDetails.get(index);
                }

                contactHolder.setContactDetails(contact);
        }
    }

    @Override
    public int getItemCount() {
        if (mSearchResults != null) return mSearchResults.size();
        if (mContactDetails == null) return 0;
        // Add one entry to account for the Select All checkbox, when not searching.
        return mContactDetails.size() + (mSearchMode ? 0 : 1);
    }

    /** Sets a list of contacts to use as data for the dialog. For testing use only. */
    @VisibleForTesting
    public static void setTestContacts(ArrayList<ContactDetails> contacts) {
        sTestContacts = contacts;
    }
}
