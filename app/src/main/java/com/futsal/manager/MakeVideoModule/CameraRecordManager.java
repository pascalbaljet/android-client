package com.futsal.manager.MakeVideoModule;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.futsal.manager.BluetoothModule.BluetoothCommunication;
import com.futsal.manager.DefineManager;
import com.futsal.manager.LogModule.LogManager;
import com.futsal.manager.OpenCVModule.CalculateBallDetect;
import com.futsal.manager.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.JavaCameraView;

/**
 * Created by stories2 on 2017. 3. 26..
 */

public class CameraRecordManager extends Activity{

    CameraRecordProcess cameraRecordProcess;
    JavaCameraView opencvCameraView;
    ToggleButton toogleRecordVideo;
    SurfaceView surfaceRecordVideo;
    SurfaceHolder surfaceHolderRecordVideo;
    CalculateBallDetect calculateBallDetect;
    BaseLoaderCallback opencvBaseLoaderCallback;
    boolean isVideoRecording;
    Button btnVideoUpload, btnSendA, btnSendB;
    BluetoothCommunication bluetoothCommunication;
    Intent passedData;
    VideoUploadProcess videoUploadProcess;
    Activity cameraRecordManager;
    BluetoothDeviceControlProcesser bluetoothDeviceControlProcesser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_record_manager);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraRecordProcess = new CameraRecordProcess(this);
        calculateBallDetect = new CalculateBallDetect(getApplicationContext());

        opencvCameraView = (JavaCameraView) findViewById(R.id.opencvCameraView);
        toogleRecordVideo = (ToggleButton) findViewById(R.id.toogleRecordVideo);
        surfaceRecordVideo = (SurfaceView) findViewById(R.id.surfaceRecordVideo);
        btnVideoUpload = (Button) findViewById(R.id.btnVideoUpload);
        btnSendA = (Button) findViewById(R.id.btnSendA);
        btnSendB = (Button)findViewById(R.id.btnSendB);

        surfaceHolderRecordVideo = surfaceRecordVideo.getHolder();
        surfaceHolderRecordVideo.addCallback(cameraRecordProcess);
        surfaceHolderRecordVideo.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        opencvCameraView.setVisibility(SurfaceView.VISIBLE);
        opencvCameraView.setCvCameraViewListener(cameraRecordProcess);

        cameraRecordProcess.SetJavaCameraView(opencvCameraView);
        cameraRecordProcess.SetCalculateBallDetect(calculateBallDetect);
        cameraRecordProcess.SetSurfaceHolderRecordVideo(surfaceHolderRecordVideo);

        isVideoRecording = false;
        cameraRecordManager = this;
        passedData = getIntent();
        bluetoothCommunication = (BluetoothCommunication)passedData.getExtras().getSerializable("bluetoothDeviceData");
        bluetoothCommunication.SetBluetoothAdapter(BluetoothAdapter.getDefaultAdapter());

        LogManager.PrintLog("CameraRecordManager", "onCreate", "ble adapter: " + bluetoothCommunication.GetBluetoothAdapter() +
                                " ble address: " + bluetoothCommunication.GetSelectedDeviceAddress(), DefineManager.LOG_LEVEL_INFO);
        bluetoothCommunication.ConnectToTargetBluetoothDevice(BluetoothAdapter.getDefaultAdapter(), bluetoothCommunication.GetSelectedDeviceAddress());
        //bluetoothDeviceControlProcesser = new BluetoothDeviceControlProcesser(bluetoothCommunication);

        toogleRecordVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isRecording) {
                isVideoRecording = !isVideoRecording;
                if(isRecording) {
                    //StartRecordVideo();
                    cameraRecordProcess.StartRecordMedia();
                    LogManager.PrintLog("CameraRecordManager", "onCheckedChanged", "Toggle Changed to Start Record Video", DefineManager.LOG_LEVEL_INFO);
                    //Log.d(videoRecordBasedOnOpencvTag, "Start");
                    //StorePictureToStorage(MatToBitmap(eachCameraFrameImage), "testImage");
                }
                else {
                    //StopRecordVideo();
                    cameraRecordProcess.StopRecordMedia();
                    LogManager.PrintLog("CameraRecordManager", "onCheckedChanged", "Toggle Changed to Stop Record Video", DefineManager.LOG_LEVEL_INFO);
                    //Log.d(videoRecordBasedOnOpencvTag, "Stopped");
                }
            }
        });

        opencvBaseLoaderCallback = new BaseLoaderCallback(getApplicationContext()) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        opencvCameraView.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }

            @Override
            public void onPackageInstall(int operation, InstallCallbackInterface callback) {
                super.onPackageInstall(operation, callback);
            }
        };

        btnVideoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Upload Process", Snackbar.LENGTH_SHORT).show();
                LogManager.PrintLog("CameraRecordManager", "onClick", "Upload button clicked", DefineManager.LOG_LEVEL_INFO);
                if(!isVideoRecording) {
                    videoUploadProcess = new VideoUploadProcess(cameraRecordManager);
                }
            }
        });

        btnSendA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMove();
            }
        });

        btnSendB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendStop();
            }
        });
    }

    void SendMove() {
        try {
            bluetoothCommunication.SetOrder("A");
            bluetoothCommunication.TryToCommunication(1);
            LogManager.PrintLog("CameraRecordManager", "SendMove", "Move Order Sended", DefineManager.LOG_LEVEL_INFO);
        }
        catch (Exception err) {
            LogManager.PrintLog("CameraRecordManager", "SendMove", "Error: " + err.getMessage(), DefineManager.LOG_LEVEL_ERROR);
        }
    }

    void SendStop() {
        try {
            bluetoothCommunication.SetOrder("B");
            bluetoothCommunication.TryToCommunication(1);
            LogManager.PrintLog("CameraRecordManager", "SendStop", "Stop Order Sended", DefineManager.LOG_LEVEL_INFO);
        }
        catch (Exception err) {
            LogManager.PrintLog("CameraRecordManager", "SendStop", "Error: " + err.getMessage(), DefineManager.LOG_LEVEL_ERROR);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        DisableCameraView(opencvCameraView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DisableCameraView(opencvCameraView);
        bluetoothCommunication.CloseConnection();
        if(bluetoothDeviceControlProcesser != null) {
            bluetoothDeviceControlProcesser.StopProcess();
        }
    }

    public void DisableCameraView(JavaCameraView targetCameraView) {
        if(targetCameraView != null) {
            targetCameraView.disableView();
        }
    }
}
