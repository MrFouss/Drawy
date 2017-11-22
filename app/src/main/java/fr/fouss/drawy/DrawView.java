package fr.fouss.drawy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class DrawView extends View {
    Bitmap drawing;
    Canvas canvas;
    Paint paint;
    Mode mode;

    float lastX, lastY;

    Shape currShape;
    float shapeScaleX, shapeScaleY;
    float shapeX, shapeY;

    public enum Mode {BRUSH, SHAPE};
    public enum Shape {CIRCLE, SQUARE};

    private ScaleGestureDetector scaleDetector;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void setBrushColor(int color) {
        paint.setColor(color);
    }

    public void setBrushThickness(float thickness) {
        paint.setStrokeWidth(thickness);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void anchorShape() {
        if (mode == Mode.SHAPE) {
            drawShape(canvas);
        }
    }

    private void initView(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        drawing = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        drawing.eraseColor(context.getResources().getColor(R.color.canvasDefaultColor));

        paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.paintDefaultColor));
        TypedValue value = new TypedValue();
        context.getResources().getValue(R.dimen.paintDefaultThickness, value, false);
        paint.setStrokeWidth(value.getFloat());
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        canvas = new Canvas(drawing);

        lastX = -1;
        lastY = -1;

        mode = Mode.SHAPE;

        currShape = Shape.SQUARE;
        shapeScaleX = 1;
        shapeScaleY = 1;
        shapeX = -1;
        shapeY = -1;

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(drawing, 0, 0, null);
        drawShape(canvas);
    }

    private void drawShape(Canvas canvas) {
        if (mode == Mode.SHAPE) {
            canvas.setMatrix(null);
            canvas.scale(shapeScaleX, shapeScaleY, shapeX, shapeY);
            canvas.translate(shapeX, shapeY);
            switch (currShape) {
                case CIRCLE:
                    canvas.drawCircle(0, 0, 150, paint);
                case SQUARE:
                    canvas.drawRect(-150, -150, 150, 150, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d("DrawView", "onTouchEvent: drawing type : "
//                + event.getAction()
//                + " ; position : "
//                + event.getX()
//                + ";"
//                + event.getY());
        if (mode == Mode.BRUSH) {
            return onTouchEventBrush(event);
        } else if (mode == Mode.SHAPE) {
            return onTouchEventShape(event);
        } else {
            return super.onTouchEvent(event);
        }
    }

    public boolean onTouchEventBrush(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            lastX = x;
            lastY = y;
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();
            canvas.drawLine(lastX, lastY, x, y, paint);
            lastX = x;
            lastY = y;
            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            canvas.drawPoint(x, y, paint);
            invalidate();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    public boolean onTouchEventShape(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE
                || event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            shapeX = x;
            shapeY = y;
            invalidate();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (detector.getPreviousSpanX() == 0) {
                shapeScaleX *= 1;
            } else {
                shapeScaleX *= detector.getCurrentSpanX()/detector.getPreviousSpanX();
            }
            if (detector.getPreviousSpanY() == 0) {
                shapeScaleY *= 1;
            } else {
                shapeScaleY *= detector.getCurrentSpanY()/detector.getPreviousSpanY();
            }
            return true;
        }
    }

}