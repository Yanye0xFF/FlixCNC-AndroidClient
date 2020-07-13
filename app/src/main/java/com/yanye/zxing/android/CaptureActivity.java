package com.yanye.zxing.android;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.yanye.flixcnc.R;
import com.yanye.flixcnc.activity.BaseActivity;
import com.yanye.zxing.camera.CameraManager;
import com.yanye.zxing.view.ViewfinderView;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;


public final class CaptureActivity extends BaseActivity implements SurfaceHolder.Callback {

	private Parameters params;
    private Camera camera;
	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;

	private InactivityTimer inactivityTimer;

	private BeepManager beepManager;

	private boolean isLightOpen = false;
	private boolean hasSurface = false;

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_capture);

		inactivityTimer = new InactivityTimer(this);

		AudioManager audioService = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		if(audioService.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
			beepManager = BeepManager.getInstance();
			if(!beepManager.hasResource()) {
                try {
                    setVolumeControlStream(AudioManager.STREAM_MUSIC);
                    AssetFileDescriptor descriptor = getResources().openRawResourceFd(R.raw.weixin_beep);
                    beepManager.setDataSource(descriptor);
                    descriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}

		ImageButton btnBack = findViewById(R.id.ib_back);
		btnBack.setOnClickListener((View view) -> finish());

		ImageView btnFlashlight = findViewById(R.id.ib_flashlight);
		btnFlashlight.setOnClickListener((View v) -> {
			if(isLightOpen) {
				params.setFlashMode(Parameters.FLASH_MODE_OFF);
				camera.setParameters(params); // 关掉亮灯
				isLightOpen = false;
			}else {
				camera = CameraManager.getCamera();
				params = camera.getParameters();
				params.setFlashMode(Parameters.FLASH_MODE_TORCH);
				camera.setParameters(params);
				camera.startPreview(); // 开始亮灯
				isLightOpen = true;
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		cameraManager = new CameraManager(getApplication());

		viewfinderView = findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		handler = null;

		SurfaceView surfaceView = findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if(hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
		}

		inactivityTimer.onResume();

		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();
		if(barcode != null) {
			if(beepManager != null) {
				beepManager.playBeepSound();
			}
			Intent intent = getIntent();
			intent.putExtra("result", rawResult.getText());
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if(surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if(cameraManager.isOpen()) {
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			if(handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}
		}catch(IOException e) {
			displayFrameworkBugMessageAndExit();
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.init_camera_error));
		builder.setPositiveButton(R.string.button_exit, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}
}
