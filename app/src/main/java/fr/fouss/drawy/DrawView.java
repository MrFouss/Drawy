package fr.fouss.drawy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class DrawView extends View {

    ///// FIELDS /////

    ///// GENERAL /////

    private Bitmap drawing;
    private Canvas drawingCanvas;
    private Paint paint;
    private Mode mode = Mode.BRUSH;
    private float lastX = -1;
    private float lastY = -1;

    ///// BRUSH /////

    private Path brushPath = new Path();
    private static final float TOUCH_TOLERANCE = 4;
    private boolean validPath = false;

    ///// IMAGE /////

    private Bitmap currImage = null;
    private float imageX;
    private float imageY;
    private float imageScale = 1;
    private int pointer1Id = -1;
    private boolean scaling = false;
    private ScaleGestureDetector scaleDetector;

    ///// ENUMS /////

    public enum Mode {BRUSH, IMAGE}

    ///// CONSTRUCTOR /////

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        drawing = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        drawing.eraseColor(0xffffffff);

        paint = new Paint();
        paint.setColor(0xff000000);
        paint.setStrokeWidth(20.0f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);

        drawingCanvas = new Canvas(drawing);

        imageX = width/2;
        imageY = height/2;

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    ///// GENERAL /////

    public void resetCanvas(int color) {
        drawing.eraseColor(color);
    }

    public void resetCanvas(Bitmap bitmap) {
        drawing = bitmap;
    }

    public void setBrushColor(int color) {
        paint.setColor(color);
    }

    public int getBrushColor() {
        return paint.getColor();
    }

    public void setBrushThickness(int thickness) {
        paint.setStrokeWidth(thickness);
    }

    public int getBrushThickness() {
        return (int) paint.getStrokeWidth();
    }

    public Bitmap getImage() {
        return currImage;
    }

    public void setImage(Bitmap image) {
        this.currImage = image;
        invalidate();
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.IMAGE) {
            imageScale = 1;
            imageX = drawingCanvas.getWidth()/2;
            imageY = drawingCanvas.getHeight()/2;
        }
    }

    public Mode getMode() {
        return this.mode;
    }

    ///// DRAW METHODS /////

    public void anchorImage() {
        if (mode == Mode.IMAGE) {
            drawImage(drawingCanvas);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(drawing, 0, 0, null);
        canvas.drawPath(brushPath, paint);
        drawImage(canvas);
    }

    private void drawImage(Canvas canvas) {
        Paint tmpPaint = new Paint();
        if (mode == Mode.IMAGE && currImage != null) {
            Matrix transform = new Matrix();
            transform.postTranslate(imageX -currImage.getWidth()/2,
                    imageY -currImage.getHeight()/2);
            transform.postScale(imageScale, imageScale, imageX, imageY);
            canvas.drawBitmap(currImage, transform, tmpPaint);
        }
    }

    ///// EVENTS /////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mode == Mode.BRUSH) {
            return onTouchEventBrush(event);
        } else if (mode == Mode.IMAGE) {
            return onTouchEventShapeAndImage(event);
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
                validPath = true;
            }
            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            brushPath.quadTo(lastX, lastY, (x + lastX)/2, (y + lastY)/2);
            if (!validPath) {
                drawingCanvas.drawPoint(x, y, paint);
            } else {
                drawingCanvas.drawPath(brushPath, paint);
                validPath = false;
            }
            brushPath.reset();
            lastX = x;
            lastY = y;
            invalidate();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    public boolean onTouchEventShapeAndImage(MotionEvent event) {
        int index = event.getActionIndex();
        int id = event.getPointerId(index);

        // check if in scaling mode
        if (event.getPointerCount() > 1) {
            scaling = true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // if the last finger is lifted update shape position/toggle false scaling
            if (!scaling) {
                lastX = imageX;
                lastY = imageY;
            }
            scaling = false;
            return true;
        }

        if (scaling) {
            imageX = lastX;
            imageY = lastY;
            scaleDetector.onTouchEvent(event);
            invalidate();
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            pointer1Id = id;
            float x = event.getX(pointer1Id);
            float y = event.getY(pointer1Id);
            lastX = imageX;
            lastY = imageY;
            imageX = x;
            imageY = y;
            invalidate();
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE && id == pointer1Id) {
            float x = event.getX(pointer1Id);
            float y = event.getY(pointer1Id);
            imageX = x;
            imageY = y;
            invalidate();
            return true;
        }

        return super.onTouchEvent(event);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            imageScale *= detector.getScaleFactor();
            return true;
        }
    }

}