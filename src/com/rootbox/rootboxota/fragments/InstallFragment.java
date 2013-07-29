/*
 * Copyright (C) 2013 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use mContext file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rootbox.rootboxota.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.rootbox.rootboxota.IOUtils;
import com.rootbox.rootboxota.R;
import com.rootbox.rootboxota.Utils;
import com.rootbox.rootboxota.activities.RequestFileActivity;
import com.rootbox.rootboxota.activities.RequestFileActivity.RequestFileCallback;

public class InstallFragment extends android.preference.PreferenceFragment
        implements RequestFileCallback {

    private static List<File> sFiles = new ArrayList<File>();

    public static void clearFiles() {
        sFiles.clear();
    }

    public static void addFile(File file) {
        if (sFiles.indexOf(file) >= 0) {
            sFiles.remove(file);
        }
        sFiles.add(file);
    }

    public static String[] getFiles() {
        List<String> files = new ArrayList<String>();
        for (File file : sFiles) {
            files.add(file.getAbsolutePath());
        }
        return files.toArray(new String[files.size()]);
    }

    private static Context mContext;
    private static OnPreferenceClickListener mListener;
    private static PreferenceCategory mLocalRoot;
    private static PreferenceCategory mExtrasRoot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        mListener = new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                showRemoveDialog(preference);
                return false;
            }
        };

        RequestFileActivity.setRequestFileCallback(this);

        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(mContext);

        mLocalRoot = new PreferenceCategory(mContext);
        mLocalRoot.setTitle(R.string.local);
        root.addPreference(mLocalRoot);

        mExtrasRoot = new PreferenceCategory(mContext);
        mExtrasRoot.setTitle(R.string.extras);
        root.addPreference(mExtrasRoot);

        setPreferenceScreen(root);

        update();
    }

    @Override
    public void fileRequested(String filePath) {
        addFile(new File(filePath));
        update();
    }

    public static void update() {
        mLocalRoot.removeAll();
        mExtrasRoot.removeAll();
        for (File file : sFiles) {
            Preference pref = new Preference(mContext);
            pref.setTitle(file.getName());
            pref.setSummary(getSummary(file, true));
            pref.setIcon(R.drawable.ic_download);
            pref.getExtras().putString("filePath", file.getAbsolutePath());
            pref.setOnPreferenceClickListener(mListener);
            if (IOUtils.isRom(file.getName()) || IOUtils.isGapps(file.getName())) {
                mLocalRoot.addPreference(pref);
            } else {
                mExtrasRoot.addPreference(pref);
            }
        }

        if(mLocalRoot.getPreferenceCount() == 0) {
            Preference pref0 = new Preference(mContext);
            pref0.setSummary(R.string.no_files_added);
            pref0.setIcon(R.drawable.ic_info);
            pref0.setEnabled(false);
            pref0.setSelectable(false);
            mLocalRoot.addPreference(pref0);
        }

        if(mExtrasRoot.getPreferenceCount() == 0) {
            Preference pref1 = new Preference(mContext);
            pref1.setSummary(R.string.no_files_added_extra);
            pref1.setIcon(R.drawable.ic_info);
            pref1.setEnabled(false);
            pref1.setSelectable(false);
            mExtrasRoot.addPreference(pref1);
        }
    }

    private static String getSummary(File file, boolean isDownloaded) {
        if (isDownloaded) {
            String name = file.getName();
            if(IOUtils.isRom(name)) {
                return Utils.getReadableVersionRom(name)+ " - " + IOUtils.humanReadableByteCount(file.length(), false);
            }
            else if(IOUtils.isGapps(name)) {
                return Utils.getReadableVersion(name) + " - " + IOUtils.humanReadableByteCount(file.length(), false);
            }
            return IOUtils.humanReadableByteCount(file.length(), false);
        } else {
            String path = file.getAbsolutePath();
            return path.substring(0, path.lastIndexOf("/"));
        }
    }

    private void showRemoveDialog(final Preference preference) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(R.string.remove_file_title);
        alert.setMessage(R.string.remove_file_summary);
        alert.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                File file = new File(preference.getExtras().getString("filePath"));
                sFiles.remove(file);
                update();
            }
        });
        alert.show();
    }
}
