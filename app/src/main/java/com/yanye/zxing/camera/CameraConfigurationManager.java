package com.yanye.zxing.camera;

import java.lang.reflect.Method;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.WindowManager;

import com.yanye.zxing.android.PreferencesActivity;

/**
 * Camera参数配置
 */
final class CameraConfigurationManager {



	private final Context context;
	private Point screenResolution;
	private Point cameraResolution;

	CameraConfigurationManager(Context context) {
		this.context = context;
	}

	/**
	 * Reads, one time, values from the camera that are needed by the app.
	 */
	@SuppressLint("NewApi")
	void initFromCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		@SuppressWarnings("deprecation")
		Point theScreenResolution = new Point(display.getWidth(),
				display.getHeight());
		screenResolution = theScreenResolution;
		
		/************** 竖屏更改4 ******************/
		Point screenResolutionForCamera = new Point();
		screenResolutionForCamera.x = screenResolution.x;
		screenResolutionForCamera.y = screenResolution.y;

		// preview size is always something like 480*320, other 320*480
		if(screenResolution.x < screenResolution.y) {
			screenResolutionForCamera.x = screenResolution.y;
			screenResolutionForCamera.y = screenResolution.x;
		}

		cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(
				parameters, screenResolutionForCamera);
		
	}

	void setDesiredCameraParameters(Camera camera, boolean safeMode) {
		Camera.Parameters parameters = camera.getParameters();

		if (parameters == null) {
			
			return;
		}

		

		if (safeMode) {
		
		}

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		CameraConfigurationUtils.setFocus(parameters, prefs.getBoolean(
				PreferencesActivity.KEY_AUTO_FOCUS, true), prefs.getBoolean(
				PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS, true),
				safeMode);

		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		/****************** 竖屏更改2 *********************/
		setDisplayOrientation(camera, 90);

		

		camera.setParameters(parameters);

		Camera.Parameters afterParameters = camera.getParameters();
		Camera.Size afterSize = afterParameters.getPreviewSize();
		if (afterSize != null
				&& (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
			
			cameraResolution.x = afterSize.width;
			cameraResolution.y = afterSize.height;
		}
	}

	private void setDisplayOrientation(Camera camera, int angle) {

		Method method;
		try {
			method = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
			if(method != null)
				method.invoke(camera, new Object[] { angle });
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	Point getCameraResolution() {
		return cameraResolution;
	}

	Point getScreenResolution() {
		return screenResolution;
	}

}
