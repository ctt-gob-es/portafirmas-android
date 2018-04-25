package es.gob.afirma.android.gcm;

/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;
import static es.gob.afirma.android.gcm.GCMConstants.DEFAULT_INTENT_SERVICE_CLASS_NAME;
import android.content.Intent;

/**
 * {@link BroadcastReceiver} that receives GCM messages and delivers them to
 * an application-specific {@link GCMBaseIntentService} subclass.
 * <p>
 * By default, the {@link GCMBaseIntentService} class belongs to the application
 * main package and is named
 * {@link GCMConstants#DEFAULT_INTENT_SERVICE_CLASS_NAME}. To use a new class,
 * the {@link #getGCMIntentServiceClassName(Context)} must be overridden.
 */
public class GCMBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GCMBroadcastReceiver";

    @Override
    public final void onReceive(Context context, Intent intent) {

        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GCMIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
    }

    /**
     * Gets the class name of the intent service that will handle GCM messages.
     */
    protected String getGCMIntentServiceClassName(Context context) {
        String className = context.getPackageName() +
                DEFAULT_INTENT_SERVICE_CLASS_NAME;
        return className;
    }

}