package ru.futoke.arproject.renderer;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.HINT;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import ru.futoke.arproject.ar.libgdx.Engine;
import ru.futoke.arproject.ar.vuforia.AppSession;
import ru.futoke.arproject.ar.vuforia.SessionControl;
import ru.futoke.arproject.ar.vuforia.VuforiaException;
import ru.futoke.arproject.ar.vuforia.VuforiaRenderer;


public class ArActivity extends AndroidApplication implements SessionControl {

    private static final String TAG = "ArActivity";

    private AppSession session;

    private DataSet posterDataSet;
    private Engine mEngine;

    VuforiaRenderer mRenderer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        Log.d(TAG, "onCreate");

        session = new AppSession(this);
        session.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mRenderer = new VuforiaRenderer(session);

        FrameLayout container = (FrameLayout) findViewById(R.id.ar_container);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = true;
        config.useCompass = true;
        config.useGyroscope = true;

        mEngine = new Engine(mRenderer);
        View glView = initializeForView(mEngine, config);

        container.addView(glView);

//        if (isConnectingToInternet()) {
//            new DownloadTask(this, "http://ar-project-futoke.c9users.io/load_model_file/2c85456c-190f-4d56-b6db-47480d8d3aff");
//        } else {
//            Toast.makeText(
//                    this,
//                    R.string.no_internet,
//                    Toast.LENGTH_SHORT
//            ).show();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // We do not resume AR here if splash screen is visible.
        try {
            session.resumeAR();
        } catch (VuforiaException e) {
            Toast.makeText(
                    this,
                    "Unable to start augmented reality.",
                    Toast.LENGTH_LONG
            ).show();
            Log.e(TAG, e.getString());
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        try {
            session.pauseAR();
        } catch (VuforiaException e) {
            Toast.makeText(
                    this,
                    "Unable to stop augmented reality.",
                    Toast.LENGTH_LONG
            ).show();
            Log.e(TAG, e.getString());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        session.onConfigurationChanged();
    }


    @Override
    public void onInitARDone(VuforiaException exception) {
        if (exception == null) {
            mRenderer.mIsActive = true;

            try {
                session.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (VuforiaException e) {
                Log.e(TAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (!result) Log.e(TAG, "Unable to enable continuous autofocus");

            try {
                mEngine.resume();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Unable to start augmented reality.", Toast.LENGTH_LONG).show();
            Log.e(TAG, exception.getString());
            finish();
        }

    }


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        try {
            session.stopAR();
        } catch (VuforiaException e) {
            Log.e(TAG, e.getString());
        }

        System.gc();
    }


    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        // Initialize the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker tracker = trackerManager.initTracker(ObjectTracker.getClassType());

        if (tracker == null) {
            Log.d(TAG, "Failed to initialize ImageTracker.");
            result = false;
        }

        return result;
    }


    @Override
    public boolean doLoadTrackersData() {
        // Get the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker imageTracker = (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());

        if (imageTracker == null) {
            Log.d(TAG, "Failed to load tracking data set because the ImageTracker has not been initialized.");
            return false;
        }

        // Create the data sets:
        posterDataSet = imageTracker.createDataSet();
        if (posterDataSet == null) {
            Log.d(TAG, "Failed to create a new tracking data.");
            return false;
        }

        // Load the data sets:
        if (!posterDataSet.load("StonesAndChips.xml", STORAGE_TYPE.STORAGE_APPRESOURCE)) {
            Log.d(TAG, "Failed to load data set.");
            return false;
        }

        // Activate the data set:
        if (!imageTracker.activateDataSet(posterDataSet)) {
            Log.d(TAG, "Failed to activate data set.");
            return false;
        }

        Log.d(TAG, "Successfully loaded and activated data set.");
        return true;
    }


    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker imageTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (imageTracker != null) {
            imageTracker.start();
            Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 1);
        } else
            result = false;

        return result;
    }


    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker imageTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (imageTracker != null)
            imageTracker.stop();
        else
            result = false;

        return result;
    }


    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        // Get the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker imageTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
        if (imageTracker == null) {
            Log.d(TAG, "Failed to destroy the tracking data set because the ImageTracker has not been initialized.");
            return false;
        }

        if (posterDataSet != null) {
            if (imageTracker.getActiveDataSet(0) == posterDataSet &&
                !imageTracker.deactivateDataSet(posterDataSet)) {
                Log.d(TAG, "Failed to destroy the tracking data set StonesAndChips because the data set could not be deactivated.");
                result = false;
            } else if (!imageTracker.destroyDataSet(posterDataSet)) {
                Log.d(TAG, "Failed to destroy the tracking data set StonesAndChips.");
                result = false;
            }

            posterDataSet = null;
        }

        return result;
    }

    @Override
    public boolean doDeinitTrackers() {

        // Deinit the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        trackerManager.deinitTracker(ObjectTracker.getClassType());

        return true;
    }

    @Override
    public void onQCARUpdate(State state) {
    }
    // Check if internet is present or not.
    private boolean isConnectingToInternet() {
        ConnectivityManager connectivityManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
