// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;

import org.chromium.ui.ContactsPickerListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ContactsPickerDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactsPickerListener listener = new ContactsPickerListener() {
                    @Override
                    public void onContactsPickerUserAction(
                            @ContactsPickerAction int action, String contactsJson, List<Contact> contacts) {
                        switch (action) {
                            case ContactsPickerAction.CONTACTS_SELECTED:
                                if (contactsJson != null) {
                                    Log.e("***** ", "**** Contact JSON: " + contactsJson);
                                }
                                break;
                            case ContactsPickerAction.CANCEL:
                                Log.e("***** ", "**** Cancelled");
                                break;
                        }
                    }
                };

                mDialog = new ContactsPickerDialog(getWindow().getContext(), listener,
                        true, true, true, true, "example.com");
                mDialog.getWindow().getAttributes().windowAnimations = R.style.PickerDialogAnimation;
                // This removes the padding around the dialog.
                mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                mDialog.show();
            }
        });

        fab.callOnClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
