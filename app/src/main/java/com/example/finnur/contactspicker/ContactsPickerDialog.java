// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import android.content.Context;
import android.support.v7.app.AlertDialog;

//import org.chromium.base.ActivityState;
//import org.chromium.base.ApplicationStatus;
//import org.chromium.base.ApplicationStatus.ActivityStateListener;
import org.chromium.base.VisibleForTesting;
// import org.chromium.chrome.R;
import org.chromium.ui.ContactsPickerListener;
//import org.chromium.ui.base.WindowAndroid;

import java.util.List;

/**
 * UI for the contacts picker that shows on the Android platform as a result of
 * &lt;input type=file accept=contacts &gt; form element.
 */
public class ContactsPickerDialog extends AlertDialog {
    // The category we're showing contacts for.
    private PickerCategoryView mCategoryView;

    /**
     * The ContactsPickerDialog constructor.
     * @param context The context to use.
     * @param listener The listener object that gets notified when an action is taken.
     * @param mimeTypes A list of mime types to show in the dialog.
     */
    public ContactsPickerDialog(Context context, ContactsPickerListener listener,
                                List<String> mimeTypes) {
        super(context, R.style.FullscreenWhite);

        // Initialize the main content view.
        mCategoryView = new PickerCategoryView(context);
        mCategoryView.initialize(this, listener, mimeTypes);
        setView(mCategoryView);
    }
}
