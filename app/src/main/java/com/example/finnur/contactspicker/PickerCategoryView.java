// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

// import org.chromium.chrome.R;
// import org.chromium.chrome.browser.ChromeActivity;
import org.chromium.chrome.browser.widget.RoundedIconGenerator;
import org.chromium.chrome.browser.widget.selection.SelectableListLayout;
import org.chromium.chrome.browser.widget.selection.SelectableListToolbar;
import org.chromium.chrome.browser.widget.selection.SelectionDelegate;
import org.chromium.ui.ContactsPickerListener;
import org.chromium.ui.UiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class for keeping track of common data associated with showing contact details in
 * the contacts picker, for example the RecyclerView.
 */
public class PickerCategoryView extends RelativeLayout implements View.OnClickListener,
        SelectionDelegate.SelectionObserver<ContactDetails>,
        SelectableListToolbar.SearchDelegate {
    // Constants for the RoundedIconGenerator.
    private static final int ICON_SIZE_DP = 32;
    private static final int ICON_CORNER_RADIUS_DP = 20;
    private static final int ICON_TEXT_SIZE_DP = 12;
    private static final int ICON_DEFAULT_BACKGROUND_COLOR = 0xFF323232;

    // The dialog that owns us.
    private ContactsPickerDialog mDialog;

    // The view containing the RecyclerView and the toolbar, etc.
    private SelectableListLayout<ContactDetails> mSelectableListLayout;

    // Our activity.
    private /*ChromeActivity*/ Activity mActivity;

    // The callback to notify the listener of decisions reached in the picker.
    private ContactsPickerListener mListener;

    // The toolbar located at the top of the dialog.
    private ContactsPickerToolbar mToolbar;

    // The RecyclerView showing the images.
    private RecyclerView mRecyclerView;

    // The {@link PickerAdapter} for the RecyclerView.
    private PickerAdapter mPickerAdapter;

    // The layout manager for the RecyclerView.
    private LinearLayoutManager mLayoutManager;

    // A helper class to draw the icon for each contact.
    private RoundedIconGenerator mIconGenerator;

    // The {@link SelectionDelegate} keeping track of which contacts are selected.
    private SelectionDelegate<ContactDetails> mSelectionDelegate;

    // The search icon.
    private ImageView mSearchButton;

    // The Done text button that confirms the selection choice.
    private Button mDoneButton;

    // The action button in the bottom right corner.
    private FloatingActionButton mActionButton;

    // The action button has two modes, Select All and Undo. This keeps track of which mode is
    // active.
    private boolean mSelectAllMode = true;

    // The state to restore to if Undo is used.
    List<ContactDetails> mPreviousSelection = null;

    // The MIME types requested.
    private List<String> mMimeTypes;

    @SuppressWarnings("unchecked") // mSelectableListLayout
    public PickerCategoryView(Context context) {
        super(context);
        mActivity = /*(ChromeActivity)*/ (Activity) context;

        mSelectionDelegate = new SelectionDelegate<ContactDetails>();
        mSelectionDelegate.addObserver(this);

        mIconGenerator = new RoundedIconGenerator(getActivity().getResources(), ICON_SIZE_DP,
                ICON_SIZE_DP, ICON_CORNER_RADIUS_DP, ICON_DEFAULT_BACKGROUND_COLOR,
                ICON_TEXT_SIZE_DP);

        View root = LayoutInflater.from(context).inflate(R.layout.contacts_picker_dialog, this);
        mSelectableListLayout =
                (SelectableListLayout<ContactDetails>) root.findViewById(R.id.selectable_list);

        mPickerAdapter = new PickerAdapter(this);
        mRecyclerView = mSelectableListLayout.initializeRecyclerView(mPickerAdapter);
        mToolbar = (ContactsPickerToolbar) mSelectableListLayout.initializeToolbar(
                R.layout.contacts_picker_toolbar, mSelectionDelegate,
                R.string.contacts_picker_select_contacts, null, 0,
                0, R.color.default_primary_color,
                null, false, false);
        mToolbar.setNavigationOnClickListener(this);
        mToolbar.initializeSearchView(this, R.string.contacts_picker_search, 0);

        mSearchButton = (ImageView) mToolbar.findViewById(R.id.search);
        mSearchButton.setOnClickListener(this);
        mDoneButton = (Button) mToolbar.findViewById(R.id.done);
        mDoneButton.setOnClickListener(this);

        mLayoutManager = new LinearLayoutManager(mActivity);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mActionButton = (FloatingActionButton) root.findViewById(R.id.action);
        mActionButton.setOnClickListener(this);
    }

    /**
     * Initializes the PickerCategoryView object.
     * @param dialog The dialog showing us.
     * @param listener The listener who should be notified of actions.
     * @param mimeTypes A list of mime types to show in the dialog.
     */
    public void initialize(ContactsPickerDialog dialog, ContactsPickerListener listener,
                           List<String> mimeTypes) {
        mDialog = dialog;
        mListener = listener;
        mMimeTypes = new ArrayList<>(mimeTypes);

        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                executeAction(ContactsPickerListener.ContactsPickerAction.CANCEL, null);
            }
        });

        mPickerAdapter.notifyDataSetChanged();
    }

    private void onStartSearch() {
        mDoneButton.setVisibility(GONE);

        // Showing the search clears current selection. Save it, so we can restore it after the
        // search has completed.
        mPreviousSelection = mSelectionDelegate.getSelectedItems();
        mDoneButton.setEnabled(false);
        mSearchButton.setVisibility(GONE);
        mToolbar.showSearchView();
    }

    // SelectableListToolbar.SearchDelegate:

    @Override
    public void onEndSearch() {
        mPickerAdapter.setSearchString("");
        mToolbar.showCloseButton();
        mToolbar.setNavigationOnClickListener(this);
        mDoneButton.setEnabled(true);
        mDoneButton.setVisibility(VISIBLE);
        mSearchButton.setVisibility(VISIBLE);

        // Hiding the search view clears the selection. Save it first and restore to the old
        // selection, with the new item added during search.
        HashSet<ContactDetails> selection = new HashSet<>();
        for (ContactDetails item : mSelectionDelegate.getSelectedItems()) {
            selection.add(item);
        }
        mToolbar.hideSearchView();
        for (ContactDetails item : mPreviousSelection) {
            selection.add(item);
        }

        // Asynchronously toggle the selection, to let the current action run its course (the number
        // roll view will otherwise show the wrong number).
        new AsyncTask<HashSet<ContactDetails>, Void, HashSet<ContactDetails>>() {
            @Override
            protected HashSet<ContactDetails> doInBackground(HashSet<ContactDetails>... params) {
              return params[0];
            }

            @Override
            protected void onPostExecute(HashSet<ContactDetails> result) {
                mSelectionDelegate.toggleSelectionForItems(result);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, selection);
    }

    @Override
    public void onSearchTextChanged(String query) {
        mPickerAdapter.setSearchString(query);
    }

    // SelectionDelegate.SelectionObserver:

    public void onSelectionStateChange(List<ContactDetails> selectedItems) {
        // Once a selection is made, drop out of search mode. Note: This function is also called when
        // entering search mode (with selectedItems then being 0 in size).
        if (mToolbar.isSearching() && selectedItems.size() > 0) {
            mToolbar.hideSearchView();
        }

        // If all items have been selected, only show the Undo button if there's a meaningful
        // state to revert to (one might not exist if they were all selected manually).
        // TODO(finnur): Add automatic test that exercises the visibility of the action button,
        //               including when all items are selected manually (special case).
        mActionButton.setVisibility(!mToolbar.isSearching() &&
                selectedItems.size() != mPickerAdapter.getItemCount() ||
                mPreviousSelection != null ? VISIBLE : GONE);
    }

    // OnClickListener:

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.done:
                notifyContactsSelected();
                break;
            case R.id.search:
                onStartSearch();
                break;
            case R.id.action:
                if (mSelectAllMode) {
                    mPreviousSelection = mSelectionDelegate.getSelectedItems();
                    mSelectionDelegate.toggleSelectionForItems(mPickerAdapter.getAllContacts());
                    mActionButton.setImageResource(R.drawable.ic_undo);
                } else {
                    Set<ContactDetails> previousSelection =
                            new HashSet<ContactDetails>(mPreviousSelection);
                    mSelectionDelegate.toggleSelectionForItems(previousSelection);
                    mActionButton.setImageResource(R.drawable.ic_select_all);
                    mPreviousSelection = null;
                }
                mSelectAllMode = !mSelectAllMode;
                break;
            default:
                executeAction(ContactsPickerListener.ContactsPickerAction.CANCEL, null);
                break;
        }
    }

    // Simple accessors:

    public Activity getActivity() { return mActivity; }

    public SelectionDelegate<ContactDetails> getSelectionDelegate() {
        return mSelectionDelegate;
    }

    public RoundedIconGenerator getIconGenerator() {
        return mIconGenerator;
    }

    /**
     * Notifies any listeners that one or more contacts have been selected.
     */
    private void notifyContactsSelected() {
        List<ContactDetails> selectedFiles = mSelectionDelegate.getSelectedItems();
        Collections.sort(selectedFiles);
        String[] contacts = new String[selectedFiles.size()];
        int i = 0;
        for (ContactDetails contactDetails : selectedFiles) {
            contacts[i++] = contactDetails.getDisplayName();
        }

        executeAction(ContactsPickerListener.ContactsPickerAction.CONTACTS_SELECTED, contacts);
    }

    /**
     * Report back what the user selected in the dialog, report UMA and clean up.
     * @param action The action taken.
     * @param contacts The contacts that were selected (if any).
     */
    private void executeAction(ContactsPickerListener.ContactsPickerAction action, String[] contacts) {
        mListener.onContactsPickerUserAction(action, contacts);
        mDialog.dismiss();
        UiUtils.onContactsPickerDismissed();
    }
}
