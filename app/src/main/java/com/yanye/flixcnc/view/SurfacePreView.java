package com.yanye.flixcnc.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.utils.Constant;

public class SurfacePreView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Paint paint;

    private float scale;
    private int dp14, dp10, dp5, dp2;
    private int surfaceWidth, surfaceHeight;

    public static final int STATE_NO_CONNECT = 0;
    public static final int STATE_CONNECTED = 1;

    private volatile boolean isDrawing;
    private volatile int connState;
    private volatile boolean needDraw;

    private Bitmap bitmap;

    private int position[] = new int[3];
    private int workhome[] = new int[3];

    public SurfacePreView(Context context) {
        super(context);
        initView(context);

    }

    public SurfacePreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        scale = context.getResources().getDisplayMetrics().density;
        dp14 = (int)(14 * scale + 0.5f);
        dp10 = (int)(10 * scale + 0.5f);
        dp5 = (int)(5 * scale + 0.5f);
        dp2 = (int)(2 * scale + 0.5f);

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        setFocusable(false);
        setFocusableInTouchMode(false);
        setKeepScreenOn(true);
        //画笔
        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setStrokeWidth(dpToPx(1));

        connState = STATE_NO_CONNECT;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_hint);
    }

    public void updateConnState(int state) {
        needDraw = true;
        this.connState = state;
    }

    public void setCurrentPosition(int xsteps, int ysteps, int zsteps) {
        needDraw = true;
        position[0] = xsteps < 0 ? 0 : xsteps;
        position[1] = ysteps < 0 ? 0 : ysteps;
        position[2] = zsteps < 0 ? 0 : zsteps;
    }

    public void setWorkHome(int xsteps, int ysteps, int zsteps) {
        needDraw = true;
        workhome[0] = xsteps;
        workhome[1] = ysteps;
        workhome[2] = zsteps;
    }

    public int[] getPosition() {
        return position;
    }

    public int[] getWorkhome() {
        return workhome;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isDrawing = true;
        needDraw = true;
        new Thread(this).start();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDrawing = false;
        needDraw = false;
    }

    public void releaseResource() {
        isDrawing = false;
        needDraw = false;
        if(bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    private static final int GridSize = 13;
    private static final int VerticalSize = 4;

    @Override
    public void run() {
        while(isDrawing) {

            if(needDraw) {
                canvas = surfaceHolder.lockCanvas();
                if(canvas != null) {
                    if(STATE_NO_CONNECT == connState) {
                        drawMessage(canvas, "请先连接雕刻机","点击左上角无线网络图标");
                    }else if(STATE_CONNECTED == connState) {
                        drawGridMap(canvas);
                        drawPositionDot(canvas);
                    }
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
                needDraw = false;
                continue;
            }
            try {
                Thread.sleep(16L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void drawMessage(Canvas canvas, String title, String sub) {
        int centerX = (surfaceWidth - bitmap.getWidth()) / 2;
        int centerY = surfaceHeight / 4;

        int dp18 = dpToPx(18);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, centerX, centerY, paint);

        paint.setTextSize(dp18);
        paint.setColor(0xFF1296DB);
        paint.setStyle(Paint.Style.FILL);
        centerY = (centerY + bitmap.getHeight() + dp18 + dp5) ;
        centerX = (surfaceWidth - title.length() * dp18) / 2;
        canvas.drawText(title, centerX, centerY, paint);

        paint.setTextSize(dp14);
        paint.setColor(0xFF8A8A8A);
        centerY += (dp18 + dp5);
        centerX = (surfaceWidth - sub.length() * dp14) / 2;
        canvas.drawText(sub, centerX, centerY, paint);
    }

    private void drawGridMap(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        paint.setColor(0xFF8a8a8a);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(dp14);
        // 坐标网格左/右填充值
        int padding = (dp14 + dp5);
        // 网格轴总长
        int length = (surfaceHeight - padding);
        // 以垂直向(短轴)为准,(dp14 + dp5) * 2.0为上下margin
        int segment = (int)((surfaceHeight - padding * 2.0) / GridSize * 1.0);
        // 左侧XY平面背景
        for(int i = 0; i < GridSize + 1; i++) {
            paint.setColor((i == 0) ? 0xFF1296DB : 0xFF8a8a8a);
            // 水平线
            canvas.drawLine(padding, (padding + i * segment), length, (padding + i * segment), paint);
            // 垂直线
            canvas.drawLine((padding + i * segment), padding, (padding + i * segment), length, paint);
            // 0 字符单独绘制一次
            if(i == 0) {
                continue;
            }
            // 绘制文字
            if(i == 1) {
                // X轴标号
                canvas.drawText(String.valueOf(i), (dp14 + dp2 + i * segment), (padding - dp2), paint);
                // Y轴标号
                canvas.drawText(String.valueOf(i), (dp5 + dp2), (dp14 + dp10 + i * segment), paint);
            }else  {
                canvas.drawText(String.valueOf(i), (dp14 + i * segment), (padding - dp2), paint);
                canvas.drawText(String.valueOf(i), dp5, (dp14 + dp10 + i * segment) , paint);
            }
        }
        // 0 X Y
        paint.setColor(0xFF1296DB);
        canvas.drawText("0", dp10, (padding - dp2), paint);
        canvas.drawText("X", (dp5 + length), (dp14 + dp10), paint);
        canvas.drawText("Y", dp14, (dp14 + length), paint);
        // 右侧Z轴背景
        int start = surfaceHeight + padding * 2;
        segment = (int)((surfaceHeight - padding * 2.0) / VerticalSize);
        // 垂直线 Z
        paint.setColor(0xFFFF7F50);
        canvas.drawLine(start , padding, start, length, paint);
        canvas.drawText("Z", (start - dp2), (dp14 + length), paint);

        for(int i = 0; i <= VerticalSize; i++) {
            paint.setColor((i == 0) ? 0xFFFF7F50 : 0xFF8a8a8a);
            // Z轴分割线
            canvas.drawLine(start, (padding + i * segment), (start +  dp14), (padding + i * segment), paint);
            // 编号
            canvas.drawText(String.valueOf(i), (start + dp14), (dp14 + dp10 + i * segment), paint);
        }
        // 右下角单位
        paint.setColor(0xFF3CB371);
        canvas.drawText("unit[cm]", surfaceWidth - dpToPx(65), (dp14 + length), paint);
    }

    private void drawPositionDot(Canvas canvas) {
        // XY flat
        int padding = (dp14 + dp5);
        // z flat
        int start = surfaceHeight + padding * 2;
        // xyz line
        float length = (surfaceHeight - padding * 2.0F);
        float xyPercent = (length / (GridSize * 10.0F));
        float zPercent = (length / (VerticalSize * 10.0F));
        // 当前位置
        float centerX = (position[0] * 1.0F / Constant.STEPS_PER_MM) * xyPercent + padding;
        float centerY = (position[1] * 1.0F / Constant.STEPS_PER_MM) * xyPercent + padding;
        float centerZ = (position[2] * 1.0F / Constant.STEPS_PER_MM) * zPercent + padding;
        paint.setColor(0xFF3CB371);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawOval((centerX - dp5), (centerY - dp5), (centerX + dp5), (centerY+ dp5), paint);
        canvas.drawOval((start - dp5), (centerZ - dp5), (start + dp5), (centerZ + dp5), paint);
        // 工作区原点
        centerX = (workhome[0] * 1.0F / Constant.STEPS_PER_MM) * xyPercent + padding;
        centerY = (workhome[1] * 1.0F / Constant.STEPS_PER_MM) * xyPercent + padding;
        centerZ = (workhome[2] * 1.0F / Constant.STEPS_PER_MM) * zPercent + padding;
        paint.setColor(0xFFe64646);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawOval((centerX - dp5), (centerY - dp5), (centerX + dp5), (centerY + dp5), paint);
        canvas.drawOval((start - dp5), (centerZ - dp5), (start + dp5), (centerZ + dp5), paint);
    }

    /**
     * dp转换为px
     * @param dpValue dp值
     * */
    private int dpToPx(float dpValue) {
        return (int)(dpValue * scale + 0.5f);
    }
}
