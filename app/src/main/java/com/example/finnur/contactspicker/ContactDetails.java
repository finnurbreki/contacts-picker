// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import java.util.List;

/**
 * A class to keep track of the metadata associated with a contact.
 */
public class ContactDetails implements Comparable<ContactDetails> {
    // The display name for this contact.
    private String mDisplayName;

    // The list of emails registered for this contact.
    private List<String> mEmails;

    /**
     * The PickerBitmap constructor.
     * @param displayName The display name of this contact.
     * @param emails The emails registered for this contact.
     */
    public ContactDetails(String displayName, List<String> emails) {
        mDisplayName = displayName;
        mEmails = emails;
    }

    /**
     * Accessor for the display name.
     * @return The full display name.
     */
    public String getDisplayName() { return mDisplayName; }

    /**
     * Accessor for the abbreviated display name (first letter of first name and first letter of
     * last name).
     * @return The display name, abbreviated to two characters.
     */
    public String getDisplayNameAbbreviation() {
        // Display the two letter abbreviation of the display name.
        String displayChars = "";
        if (mDisplayName.length() > 0) {
            displayChars += mDisplayName.charAt(0);
            String[] parts = mDisplayName.split(" ");
            if (parts.length > 1) {
                displayChars += parts[parts.length -1].charAt(0);
            }
        }

        return displayChars;
    }

    /**
     * Accessor for the list of emails (as strings separated by newline).
     * @return A string containing all the emails registered for this contact.
     */
    public String getEmailsAsString() {
        String emails = "";
        int count = 0;
        for (String email : mEmails) {
            if (count++ > 0) {
                emails += "\n";
            }
            emails += email;
        }

        return emails;
    }

    /**
     * A comparison function for PickerBitmaps (results in a last-modified first sort).
     * @param other The PickerBitmap to compare it to.
     * @return 0, 1, or -1, depending on which is bigger.
     */
    @Override
    public int compareTo(ContactDetails other) {
        return other.mDisplayName.compareTo(mDisplayName);
    }
}
