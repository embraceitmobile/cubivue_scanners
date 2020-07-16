package com.excubivue.cubivue_scanners.scanner.vision;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.excubivue.cubivue_scanners.R;


import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MaterialBarcodeScanner {

    private String TAG = "MaterialBarcodeScanner";

    /**
     * Request codes
     */
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    public static final int ACTIVITY_RESULT_CODE = 2002;


    /**
     * Scanner modes
     */
    public static final int SCANNER_MODE_FREE = 1;
    public static final int SCANNER_MODE_CENTER = 2;


    private final MaterialBarcodeScannerBuilder mMaterialBarcodeScannerBuilder;

    private OnResultListener onResultListener;

    MaterialBarcodeScanner(MaterialBarcodeScannerBuilder materialBarcodeScannerBuilder) {
        this.mMaterialBarcodeScannerBuilder = materialBarcodeScannerBuilder;
    }

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBarcodeScannerResult(Barcode barcode) {
        onResultListener.onResult(barcode);
        EventBus.getDefault().removeStickyEvent(barcode);
        EventBus.getDefault().unregister(this);
        mMaterialBarcodeScannerBuilder.clean();
    }

    /**
     * Start a scan for a barcode
     * <p>
     * This opens a new activity with the parameters provided by the MaterialBarcodeScannerBuilder
     */
    public void startScan() {
        EventBus.getDefault().register(this);

        if (mMaterialBarcodeScannerBuilder.getActivity() == null) {
            Log.i(TAG, "startScan: Could not start scan: Activity reference lost (please rebuild the MaterialBarcodeScanner before calling startScan)");
            throw new RuntimeException("Could not start scan: Activity reference lost (please rebuild the MaterialBarcodeScanner before calling startScan)");
        }

        int mCameraPermission = ActivityCompat.checkSelfPermission(mMaterialBarcodeScannerBuilder.getActivity(), Manifest.permission.CAMERA);
        if (mCameraPermission != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {

            //Open activity
            EventBus.getDefault().postSticky(this);
            Intent intent = new Intent(mMaterialBarcodeScannerBuilder.getActivity(), MaterialBarcodeScannerActivity.class);
            mMaterialBarcodeScannerBuilder.getActivity().startActivityForResult(intent, ACTIVITY_RESULT_CODE);
        }
    }

    private void requestCameraPermission() {
        final String[] mPermissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(mMaterialBarcodeScannerBuilder.getActivity(), Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(mMaterialBarcodeScannerBuilder.getActivity(), mPermissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        View.OnClickListener listener = view -> ActivityCompat.requestPermissions(mMaterialBarcodeScannerBuilder.getActivity(), mPermissions, RC_HANDLE_CAMERA_PERM);

        Snackbar.make(mMaterialBarcodeScannerBuilder.mRootView, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, listener)
                .show();
    }


    public MaterialBarcodeScannerBuilder getMaterialBarcodeScannerBuilder() {
        return mMaterialBarcodeScannerBuilder;
    }

    /**
     * Interface definition for a callback to be invoked when a view is clicked.
     */
    public interface OnResultListener {
        void onResult(Barcode barcode);
    }

}
