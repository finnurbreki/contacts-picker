// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.chromium.chrome.browser.widget.RoundedIconGenerator;

/**
 * A Contact List fragment for showing details about registered contacts in a {@link ListView}.
 */
public class ContactListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {
    private static final int ICON_SIZE_DP = 32;
    private static final int ICON_CORNER_RADIUS_DP = 20;
    private static final int ICON_TEXT_SIZE_DP = 12;
    private static final int ICON_DEFAULT_BACKGROUND_COLOR = 0xFF323232;

    /*
     * Defines an array that contains column names to move from
     * the Cursor to the ListView.
     */
    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                ContactsContract.Contacts.DISPLAY_NAME
    };

    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id".
     */
    private final static int[] TO_IDS = {
            android.R.id.text1
    };

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION = {
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.LOOKUP_KEY,
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                ContactsContract.Contacts.DISPLAY_NAME
    };

    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
    // The column index for the LOOKUP_KEY column
    private static final int LOOKUP_KEY_INDEX = 1;
    // The column index for the DISPLAY_NAME column
    private static final int DISPLAY_NAME_INDEX = 2;

    // Defines the text expression
    @SuppressLint("InlinedApi")
    private static final String SELECTION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" :
                    ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
    // Defines a variable for the search string.
    private String mSearchString = "";
    // Defines the array to hold values that replace the ?.
    private String[] mSelectionArgs = { mSearchString };

    // The contacts list view.
    ListView mContactsList;

    // An adapter that binds the result Cursor to the ListView
    private SimpleCursorAdapter mCursorAdapter;

    // A helpler class to draw the icon for each contact.
    private RoundedIconGenerator mIconGenerator;

    public ContactListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.contacts_list_fragment,
                container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Gets the ListView from the View list of the parent activity
        mContactsList =
                (ListView) getActivity().findViewById(android.R.id.list);

        // Set the item click listener to be the current fragment.
        mContactsList.setOnItemClickListener(this);

        // Gets a CursorAdapter
        mCursorAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.contact_list_item,
                null,
                FROM_COLUMNS, TO_IDS,
                0);

        mIconGenerator = new RoundedIconGenerator(getActivity().getResources(), ICON_SIZE_DP,
                ICON_SIZE_DP, ICON_CORNER_RADIUS_DP, ICON_DEFAULT_BACKGROUND_COLOR,
                ICON_TEXT_SIZE_DP);

        mCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == android.R.id.text1) {
                    // While drawing the text, we can draw the image for each contact.
                    String displayName = cursor.getString(DISPLAY_NAME_INDEX);
                    String displayChars = "";
                    if (displayName.length() > 0) {
                        displayChars += displayName.charAt(0);
                        String[] parts = displayName.split(" ");
                        if (parts.length > 1) {
                            displayChars += parts[parts.length -1].charAt(0);
                        }
                    }
                    LinearLayout layout = (LinearLayout) view.getParent();
                    Bitmap icon = mIconGenerator.generateIconForText(displayChars, 2);
                    ImageView image = (ImageView) layout.findViewById(R.id.image);
                    image.setImageBitmap(icon);
                    // Fall-through to the return statement below is on purpose (to draw the text).
                }
                return false;
            }
        });

        // Sets the adapter for the ListView
        mContactsList.setAdapter(mCursorAdapter);

        // Initializes the loader
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        /*
         * Makes search string into pattern and
         * stores it in the selection array
         */
        mSelectionArgs[0] = "%" + mSearchString + "%";
        // Starts the query
        return new CursorLoader(
                getActivity(),
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION,
                SELECTION,
                mSelectionArgs,
                null
        );
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(
            AdapterView<?> parent, View item, int position, long rowID) {
        // Define variables for the contact the user selects
        long contactId;
        String contactKey;
        Uri contactUri;

        Cursor cursor = mCursorAdapter.getCursor();
        cursor.moveToPosition(position);
        contactId = cursor.getLong(CONTACT_ID_INDEX);
        contactKey = cursor.getString(LOOKUP_KEY_INDEX);
        contactUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);
        // TODO(finnur): Do something with contactUri.
    }
}
