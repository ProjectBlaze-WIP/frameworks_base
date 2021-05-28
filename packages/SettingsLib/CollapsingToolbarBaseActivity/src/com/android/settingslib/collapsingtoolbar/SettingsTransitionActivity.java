/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.android.settingslib.collapsingtoolbar;

import static com.android.settingslib.transition.SettingsTransitionHelper.EXTRA_PAGE_TRANSITION_TYPE;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.core.os.BuildCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.settingslib.transition.SettingsTransitionHelper;
import com.android.settingslib.transition.SettingsTransitionHelper.TransitionType;

/**
 * A base Activity for Settings-specific page transition. Activities extending it will get
 * Settings transition applied.
 */
public abstract class SettingsTransitionActivity extends FragmentActivity {
    private static final String TAG = "SettingsTransitionActivity";

    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (isSettingsTransitionEnabled()) {
            getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
            SettingsTransitionHelper.applyForwardTransition(this);
            SettingsTransitionHelper.applyBackwardTransition(this);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public void setActionBar(@Nullable Toolbar toolbar) {
        super.setActionBar(toolbar);

        mToolbar = toolbar;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        final int transitionType = intent.getIntExtra(EXTRA_PAGE_TRANSITION_TYPE,
                TransitionType.TRANSITION_SHARED_AXIS);
        if (!isSettingsTransitionEnabled() ||
                transitionType == TransitionType.TRANSITION_NONE) {
            super.startActivityForResult(intent, requestCode, options);
            return;
        }

        super.startActivityForResult(intent, requestCode,
                createActivityOptionsBundleForTransition(options));
    }

    protected boolean isSettingsTransitionEnabled() {
        return BuildCompat.isAtLeastS();
    }

    @Nullable
    private Bundle createActivityOptionsBundleForTransition(@Nullable Bundle options) {
        if (mToolbar == null) {
            Log.w(TAG, "setActionBar(Toolbar) is not called. Cannot apply settings transition!");
            return options;
        }
        final Bundle transitionOptions = ActivityOptions.makeSceneTransitionAnimation(this,
                mToolbar, "shared_element_view").toBundle();
        if (options == null) {
            return transitionOptions;
        }
        final Bundle mergedOptions = new Bundle(options);
        mergedOptions.putAll(transitionOptions);
        return mergedOptions;
    }
}
