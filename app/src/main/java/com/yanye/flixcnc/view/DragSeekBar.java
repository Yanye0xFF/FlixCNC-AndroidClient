package com.yanye.flixcnc.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yanye.flixcnc.R;

public class DragSeekBar extends View {

    private Paint paint;
    private int seekBgColor, seekPbColor;
    private int seekBallLowColor, seekBallHighColor;

    private int viewWidth, viewHeight;

    private float segments;
    private float dp20, dp10, dp2;

    private int totalValue, lowValue, highValue;

    public DragSeekBar(Context context) {
        this(context, null);
    }

    public DragSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float scale = context.getResources().getDisplayMetrics().density;
        // 横向左/右侧填充20dp
        dp20 = (20 * scale);
        // 圆点半径10dp
        dp10 = (10 * scale);
        // 水平线径2dp
        dp2 = (2 * scale);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);

        seekBgColor = 0xFFe6e6e6;
        seekPbColor = ContextCompat.getColor(context, R.color.colorAccent);
        seekBallLowColor = ContextCompat.getColor(context, R.color.colorPrimary);
        seekBallHighColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);

        //setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DragSeekBar, defStyleAttr, R.style.drag_seek_default);
        int indexCount = typedArray.getIndexCount();

        for (int i = 0; i < indexCount; i++) {
            int attr = typedArray.getIndex(i);
            switch(attr) {
                case R.styleable.DragSeekBar_seek_low:
                    lowValue = typedArray.getInt(attr, 0);
                    break;
                case R.styleable.DragSeekBar_seek_high:
                    highValue = typedArray.getInt(attr, 0);
                    break;
                case R.styleable.DragSeekBar_seek_total:
                    totalValue = typedArray.getInt(attr, 50);
                    highValue = (highValue <= 0) ? (totalValue / 2) : highValue;
                    break;
                default:
                    break;
            }
        }
        typedArray.recycle();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        viewHeight = height;
        viewWidth = width;
        segments = ((width - dp20 * 2) / totalValue * 1.0F);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerY = (viewHeight - dp2) / 2.0F;

        paint.setColor(seekBgColor);
        paint.setStrokeWidth(dp2);
        paint.setStyle(Paint.Style.STROKE);
        // 背景线
        canvas.drawLine(dp20, centerY, viewWidth - dp20, centerY, paint);

        paint.setStyle(Paint.Style.FILL);
        // 低值点
        paint.setColor(seekBallLowColor);
        canvas.drawCircle(dp20 + (lowValue * segments), centerY, dp10, paint);
        // 高值点
        paint.setColor(seekBallHighColor);
        canvas.drawCircle(dp20 + (highValue * segments), centerY, dp10, paint);
        // 点间线
        paint.setColor(seekPbColor);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine((dp20 + dp10 + lowValue * segments), centerY, (dp10 + highValue * segments), centerY, paint);
    }

    public void setTotalValue(int total) {
        this.totalValue = total;
        invalidate();
    }

    public void setProgressValue(int lowValue, int highValue) {
        this.lowValue = lowValue;
        this.highValue = highValue;
        invalidate();
    }

    public void setDragFinishListener(DragFinishListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private int hoverType;
    private DragFinishListener listener;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performClick();

                if((x <= lowValue * segments + dp20 + dp10) && (x >= 0)) {
                    lowValue = (int)((x - dp20) / segments + 0.5F);
                    hoverType = 1;
                    break;
                }
                if((x >= highValue * segments + dp10) && (x <= viewWidth - dp20)) {
                    highValue = (int)((x - dp20) / segments + 0.5F);
                    hoverType = 2;
                    break;
                }

                hoverType = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if(hoverType == 1) {
                    if((x >= dp20) && (x < highValue * segments)) {
                        lowValue = (int)((x - dp20) / segments + 0.5F);
                    }
                }else if(hoverType == 2){
                    if((x > lowValue * segments + dp20 * 2) && (x <= viewWidth - dp20)) {
                        highValue = (int)((x - dp20) / segments + 0.5F);
                    }
                }
                if(listener != null) {
                    listener.onDragChanged(lowValue, highValue);
                }
               break;
            case MotionEvent.ACTION_UP:
                if(listener != null) {
                    listener.onDragFinished(lowValue, highValue);
                }
                break;
        }
        invalidate();
        return true;
    }

    public interface DragFinishListener {
        void onDragChanged(int lowValue, int highValue);
        void onDragFinished(int lowValue, int highValue);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        invalidate();
    }
}
