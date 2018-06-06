// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.ui;

/**
 * The callback used to indicate what action the user took in the picker.
 */
public interface ContactsPickerListener {
    /**
     * The action the user took in the picker.
     */
    enum Action {
        CANCEL,
        CONTACTS_SELECTED,
    }

    /**
     * Called when the user has selected an action. For possible actions see above.
     *
     * @param contacts The contacts that were selected.
     */
    void onPickerUserAction(Action action, String[] contacts);
}
