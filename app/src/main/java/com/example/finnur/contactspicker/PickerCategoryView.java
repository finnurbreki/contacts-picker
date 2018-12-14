// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import android.app.Activity;  // Android Studio only.
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;  // Android Studio only.
import android.os.AsyncTask;  // Android Studio only.
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonWriter;
import android.util.LruCache;  // Android Studio only.
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.chromium.base.ApiCompatibilityUtils;
import org.chromium.base.VisibleForTesting;
// import org.chromium.chrome.R;
// import org.chromium.chrome.browser.BitmapCache;
// import org.chromium.chrome.browser.ChromeActivity;
import org.chromium.chrome.browser.util.ConversionUtils;
import org.chromium.chrome.browser.widget.RoundedIconGenerator;
import org.chromium.chrome.browser.widget.selection.SelectableListLayout;
import org.chromium.chrome.browser.widget.selection.SelectableListToolbar;
import org.chromium.chrome.browser.widget.selection.SelectionDelegate;
import org.chromium.ui.ContactsPickerListener;
import org.chromium.ui.UiUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class for keeping track of common data associated with showing contact details in
 * the contacts picker, for example the RecyclerView.
 */
public class PickerCategoryView
        extends RelativeLayout implements View.OnClickListener, RecyclerView.RecyclerListener,
                                          SelectionDelegate.SelectionObserver<ContactDetails>,
                                          SelectableListToolbar.SearchDelegate {
    // Constants for the RoundedIconGenerator.
    private static final int ICON_SIZE_DP = 28;
    private static final int ICON_CORNER_RADIUS_DP = 20;
    private static final int ICON_TEXT_SIZE_DP = 12;

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

    private TopView mTopView;

    // The {@link PickerAdapter} for the RecyclerView.
    private PickerAdapter mPickerAdapter;

    // The layout manager for the RecyclerView.
    private LinearLayoutManager mLayoutManager;

    // A helper class to draw the icon for each contact.
    private RoundedIconGenerator mIconGenerator;

    // The {@link SelectionDelegate} keeping track of which contacts are selected.
    private SelectionDelegate<ContactDetails> mSelectionDelegate;

    // Android Studio uses the LRU Cache directly, Chrome uses the BitmapCache.
    // A cache for contact images, lazily created.
    private LruCache<String, Bitmap> mBitmapCache;

    // The search icon.
    private ImageView mSearchButton;

    // Keeps track of the set of last selected contacts in the UI.
    private Set<ContactDetails> mPreviousSelection;

    // The Done text button that confirms the selection choice.
    private Button mDoneButton;

    // Whether the picker is in multi-selection mode.
    private boolean mMultiSelectionAllowed;

    // The MIME types requested.
    private List<String> mMimeTypes;

    /**
     * @param multiSelectionAllowed Whether the contacts picker should allow multiple items to be
     * selected.
     */
    @SuppressWarnings("unchecked") // mSelectableListLayout
    public PickerCategoryView(Context context, boolean multiSelectionAllowed) {
        super(context);

        mActivity = (Activity) context;
        mMultiSelectionAllowed = multiSelectionAllowed;

        mSelectionDelegate = new SelectionDelegate<ContactDetails>();
        if (!multiSelectionAllowed) mSelectionDelegate.setSingleSelectionMode();
        mSelectionDelegate.addObserver(this);

        Resources resources = context.getResources();
        int iconColor =
                ApiCompatibilityUtils.getColor(resources, R.color.default_favicon_background_color);
        mIconGenerator = new RoundedIconGenerator(resources, ICON_SIZE_DP, ICON_SIZE_DP,
                ICON_CORNER_RADIUS_DP, iconColor, ICON_TEXT_SIZE_DP);

        View root = LayoutInflater.from(context).inflate(R.layout.contacts_picker_dialog, this);
        mSelectableListLayout =
                (SelectableListLayout<ContactDetails>) root.findViewById(R.id.selectable_list);

        mPickerAdapter = new PickerAdapter(this, context.getContentResolver());
        mRecyclerView = mSelectableListLayout.initializeRecyclerView(mPickerAdapter);
        int titleId = multiSelectionAllowed ? R.string.contacts_picker_select_contacts
                                            : R.string.contacts_picker_select_contact;
        mToolbar = (ContactsPickerToolbar) mSelectableListLayout.initializeToolbar(
                R.layout.contacts_picker_toolbar, mSelectionDelegate, titleId, null, 0, 0,
                R.color.modern_primary_color, null, false, false);
        mToolbar.setNavigationOnClickListener(this);
        mToolbar.initializeSearchView(this, R.string.contacts_picker_search, 0);

        mSearchButton = (ImageView) mToolbar.findViewById(R.id.search);
        mSearchButton.setOnClickListener(this);
        mDoneButton = (Button) mToolbar.findViewById(R.id.done);
        mDoneButton.setOnClickListener(this);

        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Each image (on a Pixel 2 phone) is about 30-40K. Calculate a proportional amount of the
        // available memory, but cap it at 5MB.
        final long maxMemory = ConversionUtils.bytesToKilobytes(Runtime.getRuntime().maxMemory());
        int iconCacheSizeKb = (int) (maxMemory / 8); // 1/8th of the available memory.

        // Android Studio project only:
        mBitmapCache = new LruCache<String, Bitmap>(iconCacheSizeKb) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return (int) ConversionUtils.bytesToKilobytes(bitmap.getByteCount());
            }
        };
    }

    /**
     * Initializes the PickerCategoryView object.
     * @param dialog The dialog showing us.
     * @param listener The listener who should be notified of actions.
     * @param mimeTypes A list of mime types to show in the dialog.
     */
    public void initialize(
            ContactsPickerDialog dialog, ContactsPickerListener listener, List<String> mimeTypes) {
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
        mPreviousSelection = new HashSet<ContactDetails>(mSelectionDelegate.getSelectedItems());
        mSearchButton.setVisibility(GONE);
        mPickerAdapter.setSearchMode(true);
        mToolbar.showSearchView();
    }

    // SelectableListToolbar.SearchDelegate:

    @Override
    public void onEndSearch() {
        mPickerAdapter.setSearchString("");
        mPickerAdapter.setSearchMode(false);
        mToolbar.showCloseButton();
        mToolbar.setNavigationOnClickListener(this);
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
                mSelectionDelegate.setSelectedItems(result);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, selection);
    }

    @Override
    public void onSearchTextChanged(String query) {
        mPickerAdapter.setSearchString(query);
    }

    // SelectionDelegate.SelectionObserver:

    @Override
    public void onSelectionStateChange(List<ContactDetails> selectedItems) {
        // Once a selection is made, drop out of search mode. Note: This function is also called
        // when entering search mode (with selectedItems then being 0 in size).
        if (mToolbar.isSearching() && selectedItems.size() > 0) {
            mToolbar.hideSearchView();
        }

        boolean allSelected = selectedItems.size() == mPickerAdapter.getItemCount() - 1;
        mTopView.syncCheckbox(allSelected);
    }

    // RecyclerView.RecyclerListener:

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        ContactViewHolder bitmapHolder = (ContactViewHolder) holder;
        bitmapHolder.cancelIconRetrieval();
    }

    public void toggleSelectAll(boolean allSelected) {
        if (allSelected) {
            mPreviousSelection = mSelectionDelegate.getSelectedItems();
            mSelectionDelegate.setSelectedItems(
                    new HashSet<ContactDetails>(mPickerAdapter.getAllContacts()));
            mListener.onContactsPickerUserAction(
                    ContactsPickerListener.ContactsPickerAction.SELECT_ALL, null);
        } else {
            mSelectionDelegate.setSelectedItems(new HashSet<ContactDetails>());
            mPreviousSelection = null;
            mListener.onContactsPickerUserAction(
                    ContactsPickerListener.ContactsPickerAction.UNDO_SELECT_ALL, null);
        }
    }

    // OnClickListener:

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.done) {
            notifyContactsSelected();
        } else if (id == R.id.search) {
            onStartSearch();
        } else {
            executeAction(ContactsPickerListener.ContactsPickerAction.CANCEL, null);
        }
    }

    // Simple getters and setters:

    public SelectionDelegate<ContactDetails> getSelectionDelegate() {
        return mSelectionDelegate;
    }

    public RoundedIconGenerator getIconGenerator() {
        return mIconGenerator;
    }

    public /*BitmapCache*/ LruCache<String, Bitmap> getIconCache() {
        return mBitmapCache;
    }

    public void setTopView(TopView topView) {
        mTopView = topView;
    }

    /**
     * Notifies any listeners that one or more contacts have been selected.
     */
    private void notifyContactsSelected() {
        List<ContactDetails> selectedContacts = mSelectionDelegate.getSelectedItemsAsList();
        Collections.sort(selectedContacts);

        StringWriter out = new StringWriter();
        final JsonWriter writer = new JsonWriter(out);

        try {
            writer.beginArray();
            for (ContactDetails contactDetails : selectedContacts) {
                contactDetails.appendJson(writer);
            }
            writer.endArray();
            executeAction(
                    ContactsPickerListener.ContactsPickerAction.CONTACTS_SELECTED, out.toString());
        } catch (IOException e) {
            assert false;
            executeAction(ContactsPickerListener.ContactsPickerAction.CANCEL, null);
        }
    }

    /**
     * Report back what the user selected in the dialog, report UMA and clean up.
     * @param action The action taken.
     * @param contacts The contacts that were selected (if any).
     */
    private void executeAction(
            ContactsPickerListener.ContactsPickerAction action, String contacts) {
        mListener.onContactsPickerUserAction(action, contacts);
        mDialog.dismiss();
        UiUtils.onContactsPickerDismissed();
    }

    @VisibleForTesting
    public SelectionDelegate<ContactDetails> getSelectionDelegateForTesting() {
        return mSelectionDelegate;
    }
}
