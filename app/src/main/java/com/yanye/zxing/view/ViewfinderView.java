package com.yanye.zxing.view;

import com.google.zxing.ResultPoint;
import com.yanye.flixcnc.R;
import com.yanye.zxing.camera.CameraManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public final class ViewfinderView extends View {

	private static final long ANIMATION_DELAY = 80L;
	private static final int CURRENT_POINT_OPACITY = 0xA0;
	private static final int MAX_RESULT_POINTS = 10;
	private static final int POINT_SIZE = 6;

	private CameraManager cameraManager;
	private Paint paint;
	private final int maskColor; // 取景框外的背景颜色
	private final int resultPointColor; // 特征点的颜色
	private final int statusColor; // 提示文字颜色
	private List<ResultPoint> possibleResultPoints;

	private int scanLineTop = 0;
	private static final int SCAN_VELOCITY = 10;

	// 扫描线图片
	private Bitmap scanLineImage;

	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		maskColor = ContextCompat.getColor(context, R.color.viewfinder_mask);
		resultPointColor = ContextCompat.getColor(context, R.color.possible_result_points);
		statusColor = ContextCompat.getColor(context, R.color.status_text);

		possibleResultPoints = new ArrayList<>(MAX_RESULT_POINTS);

		scanLineImage = BitmapFactory.decodeResource(context.getResources(), R.mipmap.scan_light);
	}

	public void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (cameraManager == null) {
			return;
		}

		// frame为取景框
		Rect frame = cameraManager.getFramingRect();
		Rect previewFrame = cameraManager.getFramingRectInPreview();
		if (frame == null || previewFrame == null) {
			return;
		}

		int width = getWidth();
		int height = getHeight();

		// 绘制取景框外的暗灰色的表面，分四个矩形绘制
		paint.setColor(maskColor);

		canvas.drawRect(0, 0, width, frame.top, paint);// Rect_1
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint); // Rect_2
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint); // Rect_3
		canvas.drawRect(0, frame.bottom + 1, width, height, paint); // Rect_4


		drawFrameBounds(canvas, frame);
		drawStatusText(canvas, frame, width);
		drawScanLight(canvas, frame);

		int frameLeft = frame.left;
		int frameTop = frame.top;
		float scaleX = frame.width() / (float) previewFrame.width();
		float scaleY = frame.height() / (float) previewFrame.height();

		// 绘制扫描线周围的特征点
		paint.setAlpha(CURRENT_POINT_OPACITY);
		paint.setColor(resultPointColor);
		for(ResultPoint point : possibleResultPoints) {
			canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
					frameTop + (int) (point.getY() * scaleY), POINT_SIZE, paint);
		}

		// Request another update at the animation interval, but only
		// repaint the laser line,
		// not the entire viewfinder mask.
		postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE,
				frame.top - POINT_SIZE, frame.right + POINT_SIZE,
				frame.bottom + POINT_SIZE);

	}

	/**
	 * 绘制取景框边框
	 */
	private void drawFrameBounds(Canvas canvas, Rect frame) {
		final int corWidth = 15;
		final int corLength = 45;

		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(2);
		paint.setStyle(Paint.Style.STROKE);
		// 绘制白色扫描框线
		canvas.drawRect(frame, paint);
		// 更换笔刷
		paint.setColor(Color.parseColor("#74B5AA"));
		paint.setStyle(Paint.Style.FILL);
		// 左上角
		canvas.drawRect(frame.left - corWidth, frame.top, frame.left, frame.top
				+ corLength, paint);
		canvas.drawRect(frame.left - corWidth, frame.top - corWidth, frame.left
				+ corLength, frame.top, paint);
		// 右上角
		canvas.drawRect(frame.right, frame.top, frame.right + corWidth,
				frame.top + corLength, paint);
		canvas.drawRect(frame.right - corLength, frame.top - corWidth,
				frame.right + corWidth, frame.top, paint);
		// 左下角
		canvas.drawRect(frame.left - corWidth, frame.bottom - corLength,
				frame.left, frame.bottom, paint);
		canvas.drawRect(frame.left - corWidth, frame.bottom, frame.left
				+ corLength, frame.bottom + corWidth, paint);
		// 右下角
		canvas.drawRect(frame.right, frame.bottom - corLength, frame.right
				+ corWidth, frame.bottom, paint);
		canvas.drawRect(frame.right - corLength, frame.bottom, frame.right
				+ corWidth, frame.bottom + corWidth, paint);
	}

	/**
	 * 绘制提示文字
	 */
	private void drawStatusText(Canvas canvas, Rect frame, int width) {
		final String statusText1 = getResources().getString(R.string.finder_view_hint1);
		final String statusText2 = getResources().getString(R.string.finder_view_hint2);
		final int statusTextSize = 45;
		final int statusPaddingTop = 180;

		paint.setColor(statusColor);
		paint.setTextSize(statusTextSize);

		int textWidth1 = (int) paint.measureText(statusText1);
		canvas.drawText(statusText1, (width - textWidth1) / 2, (frame.top - statusPaddingTop), paint);

		int textWidth2 = (int) paint.measureText(statusText2);
		canvas.drawText(statusText2, (width - textWidth2) / 2, (frame.top - statusPaddingTop + 60), paint);
	}

	/**
	 * 绘制移动扫描线图片
	 */
	private void drawScanLight(Canvas canvas, Rect frame) {
		if(scanLineTop == 0) {
			scanLineTop = frame.top;
		}
		scanLineTop = (scanLineTop < frame.bottom) ? (scanLineTop + SCAN_VELOCITY) : frame.top;

		Rect scanRect = new Rect(frame.left, scanLineTop, frame.right, (scanLineTop + scanLineImage.getHeight()));
		canvas.drawBitmap(scanLineImage, null, scanRect, paint);
	}

	public void addPossibleResultPoint(ResultPoint point) {
		if(possibleResultPoints.size() < MAX_RESULT_POINTS) {
			possibleResultPoints.add(point);
		}else {
			possibleResultPoints.remove(0);
			possibleResultPoints.add(point);
		}
	}

}
