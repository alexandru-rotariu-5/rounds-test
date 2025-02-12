package ai.rounds.speedmeter.ui.speed;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class SpeedMeterView extends View {

    private static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#252525");

    private static final int DEFAULT_NEEDLE_COLOR = Color.parseColor("#FF8800");
    private static final int DEFAULT_OUTER_RING_COLOR = Color.parseColor("#D3D3D3");
    private static final float DEFAULT_MAX_SPEED = 150f;
    private static final float DEFAULT_INITIAL_ANGLE = -45f;
    private static final float DEFAULT_MAX_ROTATION_ANGLE = 270f;

    private Bitmap cacheBitmap;
    private Canvas cacheCanvas;
    private float centerX;
    private float centerY;
    private float currentAngle;
    private float radius;
    private int calculatedHeight;
    private int calculatedWidth;
    private Matrix rotateMatrix;
    private Paint needlePaint;
    private Paint backgroundPaint;
    private Path needlePath;

    public SpeedMeterView(Context context) {
        super(context);
        init();
    }

    public SpeedMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpeedMeterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SpeedMeterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        needlePaint.setStrokeWidth(4);
        needlePaint.setColor(DEFAULT_NEEDLE_COLOR);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);

        needlePath = new Path();
        rotateMatrix = new Matrix();
        currentAngle = 0f;
        cacheCanvas = new Canvas();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (calculatedWidth != MeasureSpec.getSize(widthMeasureSpec) || calculatedHeight != MeasureSpec.getSize(heightMeasureSpec)) {

            calculatedWidth = MeasureSpec.getSize(widthMeasureSpec);
            calculatedHeight = MeasureSpec.getSize(heightMeasureSpec);
            radius = Math.min(calculatedWidth, calculatedHeight) / 2f;

            centerX = calculatedWidth / 2f;
            centerY = calculatedHeight / 2f;

            if (cacheBitmap != null) {
                cacheBitmap.recycle();
            }
            cacheBitmap = null;

            needlePath.reset();
            needlePath.moveTo(centerX, centerY);
            needlePath.lineTo(centerX - (radius * 0.9f), centerY);

            rotateMatrix.setRotate(DEFAULT_INITIAL_ANGLE, centerX, centerY);
            needlePath.transform(rotateMatrix);
        }

        setMeasuredDimension(calculatedWidth, calculatedHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (cacheBitmap == null) {
            cacheBitmap = Bitmap.createBitmap(calculatedWidth, calculatedHeight, Bitmap.Config.ARGB_8888);
            cacheCanvas.setBitmap(cacheBitmap);

            backgroundPaint.setColor(DEFAULT_OUTER_RING_COLOR);
            cacheCanvas.drawCircle(centerX, centerY, radius, backgroundPaint);

            backgroundPaint.setColor(DEFAULT_BACKGROUND_COLOR);
            cacheCanvas.drawCircle(centerX, centerY, radius - 12, backgroundPaint);

            cacheCanvas.drawCircle(centerX, centerY, 10, needlePaint);
        }

        super.onDraw(canvas);

        canvas.drawBitmap(cacheBitmap, 0, 0, backgroundPaint);

        canvas.drawPath(needlePath, needlePaint);
    }

    /**
     * Sets the current speed and animates the needle
     *
     * @param speed Speed to be set
     */
    public void updateSpeed(float speed) {
        speed = Math.min(speed, DEFAULT_MAX_SPEED);

        final float rotation = (speed * DEFAULT_MAX_ROTATION_ANGLE / DEFAULT_MAX_SPEED) - currentAngle;

        currentAngle += rotation;

        ValueAnimator angleAnim = ValueAnimator.ofFloat(0, rotation);
        angleAnim.setDuration(600);
        angleAnim.setInterpolator(new OvershootInterpolator());
        angleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            private float lastAnimatedValue = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rotateMatrix.setRotate(((float) animation.getAnimatedValue() - lastAnimatedValue), centerX, centerY);
                lastAnimatedValue = (float) animation.getAnimatedValue();
                needlePath.transform(rotateMatrix);
                invalidate();
            }
        });

        angleAnim.start();
    }
}
