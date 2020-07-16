package com.excubivue.cubivue_scanners.scanner.vision;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.excubivue.cubivue_scanners.R;
import com.excubivue.cubivue_scanners.scanner.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

@SuppressLint("MissingPermission")
public class MaterialBarcodeScannerActivity extends AppCompatActivity {

    private static final int RC_HANDLE_GMS = 9001;

    private static final String TAG = "MaterialBarcodeScanner";
    private static int ACTIVITY_RESTART_COUNT = 0;
    final Handler handler = new Handler();

    Runnable mRunnable = null;
    private MaterialBarcodeScanner mMaterialBarcodeScanner;
    private MaterialBarcodeScannerBuilder mMaterialBarcodeScannerBuilder;
    private BarcodeDetector barcodeDetector;

    private CameraSourcePreview mCameraSourcePreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

    private SoundPoolPlayer mSoundPoolPlayer;
    /**
     * true if no further barcode should be detected or given as a result
     */
    private boolean mDetectionConsumed = false;
    private boolean mFlashOn = false;

    private Boolean restartingAct = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (getWindow() != null) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            Log.i(TAG, "onCreate: Barcode scanner could not go into fullscreen mode!");
        }
        setContentView(R.layout.barcode_capture);

        if (MaterialBarcodeScannerActivity.ACTIVITY_RESTART_COUNT >= 2) {
            MaterialBarcodeScannerActivity.ACTIVITY_RESTART_COUNT = 0;
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMaterialBarcodeScanner(MaterialBarcodeScanner materialBarcodeScanner) {
        this.mMaterialBarcodeScanner = materialBarcodeScanner;
        mMaterialBarcodeScannerBuilder = mMaterialBarcodeScanner.getMaterialBarcodeScannerBuilder();
        barcodeDetector = mMaterialBarcodeScanner.getMaterialBarcodeScannerBuilder().getBarcodeDetector();
        startCameraSource();
        setupLayout();
    }

    public void restartActivity() {
        Log.i(TAG, "restartActivity: Restarting Scanner..");
        if (!restartingAct) {
            restartingAct = true;
            MaterialBarcodeScannerActivity.ACTIVITY_RESTART_COUNT = MaterialBarcodeScannerActivity.ACTIVITY_RESTART_COUNT + 1;

            if (mRunnable != null)
                handler.removeCallbacks(mRunnable);
            Intent returnIntent = new Intent();
            int resultCode = Activity.RESULT_OK;
            if (MaterialBarcodeScannerActivity.ACTIVITY_RESTART_COUNT >= 2) {
                resultCode = Activity.RESULT_CANCELED;
                MaterialBarcodeScannerActivity.ACTIVITY_RESTART_COUNT = 0;
            }
            if (getParent() == null) {
                setResult(resultCode, returnIntent);
            } else {
                getParent().setResult(resultCode, returnIntent);
            }

            finish();
        }
    }

    private void setupLayout() {
        final TextView topTextView = (TextView) findViewById(R.id.topText);

        String topText = mMaterialBarcodeScannerBuilder.getText();
        if (!mMaterialBarcodeScannerBuilder.getText().equals("")) {
            topTextView.setText(topText);
        }
        //setupButtons();
        setupCenterTracker();
        //blinkFlashLight();
    }

    private void setupCenterTracker() {
        if (mMaterialBarcodeScannerBuilder.getScannerMode() == MaterialBarcodeScanner.SCANNER_MODE_CENTER) {
            final ImageView centerTracker = (ImageView) findViewById(R.id.barcode_square);
            centerTracker.setImageResource(mMaterialBarcodeScannerBuilder.getTrackerResourceID());
            mGraphicOverlay.setVisibility(View.INVISIBLE);
        }
    }

    private void updateCenterTrackerForDetectedState() {
        if (mMaterialBarcodeScannerBuilder.getScannerMode() == MaterialBarcodeScanner.SCANNER_MODE_CENTER) {
            final ImageView centerTracker = (ImageView) findViewById(R.id.barcode_square);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    centerTracker.setImageResource(mMaterialBarcodeScannerBuilder.getTrackerDetectedResourceID());
                }
            });
        }
    }

    private void setupButtons() {
        final LinearLayout flashOnButton = (LinearLayout) findViewById(R.id.flashIconButton);
        final ImageView flashToggleIcon = (ImageView) findViewById(R.id.flashIcon);

        flashOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlashOn) {
                    flashToggleIcon.setBackgroundResource(R.drawable.ic_flash_on_white_24dp);
                    disableTorch();
                } else {
                    flashToggleIcon.setBackgroundResource(R.drawable.ic_flash_off_white_24dp);
                    enableTorch();
                }
                mFlashOn ^= true;
            }
        });
        if (mMaterialBarcodeScannerBuilder.isFlashEnabledByDefault()) {
            flashToggleIcon.setBackgroundResource(R.drawable.ic_flash_off_white_24dp);
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        mSoundPoolPlayer = new SoundPoolPlayer(this);
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dialog.show();
        }
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.graphicOverlay);

        BarcodeGraphicTracker.NewDetectionListener listener = barcode -> {
            if (!mDetectionConsumed) {
                mDetectionConsumed = true;

                EventBus.getDefault().postSticky(barcode);
                updateCenterTrackerForDetectedState();
                if (mMaterialBarcodeScannerBuilder.isBleepEnabled()) {
                    mSoundPoolPlayer.playShortResource(R.raw.bleep);
                }
                mGraphicOverlay.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(Activity.RESULT_CANCELED);
                        MaterialBarcodeScannerActivity.ACTIVITY_RESTART_COUNT = 0;
                        finish();
                    }
                }, 50);
            }
        };
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, listener, mMaterialBarcodeScannerBuilder.getTrackerColor());
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());
        CameraSource mCameraSource = mMaterialBarcodeScannerBuilder.getCameraSource();
        if (mCameraSource != null) {
            try {
                mCameraSource.setCameraActivity(this);
                mCameraSourcePreview = (CameraSourcePreview) findViewById(R.id.preview);
                mCameraSourcePreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.i(TAG, "startCameraSource: Unable to start camera source: " + Utils.getInstance().getStackTrace(e));
                mCameraSource.release();
                mCameraSource = null;
            }
        } else {
            Log.i(TAG, "startCameraSource: Null Camera supplied");
        }
    }

    private void enableTorch() throws SecurityException {
        mMaterialBarcodeScannerBuilder.getCameraSource().setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        try {
            mMaterialBarcodeScannerBuilder.getCameraSource().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disableTorch() throws SecurityException {
        mMaterialBarcodeScannerBuilder.getCameraSource().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        try {
            mMaterialBarcodeScannerBuilder.getCameraSource().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        if (mRunnable != null)
            handler.removeCallbacks(mRunnable);
        if (mCameraSourcePreview != null) {
            mCameraSourcePreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: Scanner Closed!");
        if (mRunnable != null)
            handler.removeCallbacks(mRunnable);
        if (isFinishing()) {
            clean();
        }
    }

    private void clean() {
        EventBus.getDefault().removeStickyEvent(MaterialBarcodeScanner.class);
        if (mCameraSourcePreview != null) {
            mCameraSourcePreview.release();
            mCameraSourcePreview = null;
        }
        if (mSoundPoolPlayer != null) {
            mSoundPoolPlayer.release();
            mSoundPoolPlayer = null;
        }
    }

    private void blinkFlashLight() {
        final int delay = 5000; //milliseconds

        mRunnable = new Runnable() {
            public void run() {
                if (mFlashOn) {
                    disableTorch();
                } else {
                    enableTorch();
                }
                mFlashOn ^= true;

                handler.postDelayed(this, delay);
            }
        };
        handler.postDelayed(mRunnable, delay);
    }
}
