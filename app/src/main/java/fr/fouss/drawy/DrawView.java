package fr.fouss.drawy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class DrawView extends View {
    private Bitmap drawing;
    private Canvas canvas;
    private Paint paint;
    private Mode mode;

    private float lastX, lastY;
    private Path brushPath;

    private Shape currShape;
    private float shapeScaleX, shapeScaleY, shapeScale;
    private float shapeX, shapeY;
    private boolean pointer1Down, pointer2Down;
    private int pointer1Id, pointer2Id;
    private boolean scaling;
    private int fingerNbr;

    public enum Mode {BRUSH, SHAPE};
    public enum Shape {CIRCLE, SQUARE};

    private static final float TOUCH_TOLERANCE = 4;

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
        if (mode == Mode.SHAPE) {
            shapeX = canvas.getWidth()/2;
            shapeY = canvas.getHeight()/2;
        }
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
        paint.setStyle(Paint.Style.STROKE);

        canvas = new Canvas(drawing);

        lastX = -1;
        lastY = -1;

        mode = Mode.BRUSH;
        setMode(Mode.SHAPE);

        currShape = Shape.CIRCLE;
        shapeScaleX = 1;
        shapeScaleY = 1;
        shapeX = width/2;
        shapeY = height/2;
        shapeScale = 1;

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        brushPath = new Path();

        pointer1Down = false;
        pointer2Down = false;

        pointer1Id = -1;
        pointer2Id = -1;

        scaling = false;
        fingerNbr = 0;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(drawing, 0, 0, null);
        canvas.drawPath(brushPath, paint);
        drawShape(canvas);
    }

    private void drawShape(Canvas canvas) {
        if (mode == Mode.SHAPE) {
            switch (currShape) {
                case CIRCLE:
                    canvas.drawCircle(shapeX, shapeY, 150*shapeScale, paint);
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
            brushPath.moveTo(x, y);
            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();
            float dx = Math.abs(x - lastX);
            float dy = Math.abs(y - lastY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                brushPath.quadTo(lastX, lastY, (x + lastX)/2, (y + lastY)/2);
                lastX = x;
                lastY = y;
            }
            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            float dx = Math.abs(x - lastX);
            float dy = Math.abs(y - lastY);
            brushPath.quadTo(lastX, lastY, (x + lastX)/2, (y + lastY)/2);
            canvas.drawPath(brushPath, paint);
            canvas.drawPoint(x, y, paint);
            brushPath.reset();
            lastX = x;
            lastY = y;
            invalidate();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    public boolean onTouchEventShape(MotionEvent event) {
        int index = event.getActionIndex();
        int id = event.getPointerId(index);
        int actionMasked = event.getActionMasked();
//
//        switch (actionMasked) {
//            case MotionEvent.ACTION_DOWN: {
//
//                break;
//            }
//            case MotionEvent.ACTION_POINTER_DOWN: {
//                // TODO use data
//                break;
//            }
//            case MotionEvent.ACTION_MOVE: { // a pointer was moved
//                // TODO use data
//                break;
//            }
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_POINTER_UP:
//            case MotionEvent.ACTION_CANCEL: {
//                // TODO use data
//                break;
//            }
//        }
//        if (pointer1Id == -1) {
//            pointer1Id = id;
//        } else if (pointer2Id == -1) {
//            pointer2Id = id;
//        }
//
//        if (id == pointer1Id) {
//
//        } else if (id == pointer2Id) {
//
//        }


        Log.i("DrawView", "onTouchEventShape: " + event.getActionMasked());
//
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN
                || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            fingerNbr++;
        }

        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP
                || event.getActionMasked() == MotionEvent.ACTION_UP) {
            fingerNbr--;
        }

        if (fingerNbr > 1) {
            scaling = true;
        } else if (fingerNbr == 0) {
            scaling = false;
        }

        if (scaling) {
            shapeX = lastX;
            shapeY = lastY;
            scaleDetector.onTouchEvent(event);
            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            shapeX = x;
            shapeY = y;
            lastX = x;
            lastY = y;
            pointer1Id = id;
            invalidate();
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (id == pointer1Id && !scaling) {
                float x = event.getX();
                float y = event.getY();
                shapeX = x;
                shapeY = y;
                invalidate();
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
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
            shapeScale *= detector.getScaleFactor();
            return true;
        }
    }

}