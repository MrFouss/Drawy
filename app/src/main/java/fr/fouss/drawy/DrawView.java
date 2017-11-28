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
    private int pointer2Id = -1;
    private Vector2D initPointer1 = new Vector2D();
    private Vector2D currPointer1 = new Vector2D();
    private Vector2D initPointer2 = new Vector2D();
    private Vector2D currPointer2 = new Vector2D();
    private float initAngle = 0;
    private float currAngle = 0;
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


    public Bitmap getDrawing() {
        return drawing;
    }

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
            initAngle = 0;
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
            transform.postRotate((float)Math.toDegrees(currAngle + initAngle), imageX, imageY);
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
            // retrieve 2nd finger id and position
            if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN && pointer2Id == -1) {
                pointer2Id = id;
                initPointer2.x = event.getX(pointer2Id);
                initPointer2.y = event.getY(pointer2Id);
                currPointer2.x = event.getX(pointer2Id);
                currPointer2.y = event.getY(pointer2Id);
            } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                // retrieve movements
                currPointer1.x = event.getX(pointer1Id);
                currPointer1.y = event.getY(pointer1Id);
                currPointer2.x = event.getX(pointer2Id);
                currPointer2.y = event.getY(pointer2Id);
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // if the last finger is lifted update shape position/toggle false scaling
            if (!scaling) {
                lastX = imageX;
                lastY = imageY;
            }
            initAngle += currAngle;
            currAngle = 0;
            scaling = false;
            return true;
        }


        if (scaling) {
            currAngle = Vector2D.getSignedAngleBetween(
                    Vector2D.getDiff(initPointer2, initPointer1),
                    Vector2D.getDiff(currPointer2, currPointer1)
            );
            imageX = lastX;
            imageY = lastY;
            scaleDetector.onTouchEvent(event);
            invalidate();
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            currAngle = 0;
            pointer1Id = id;
            pointer2Id = -1;
            float x = event.getX(pointer1Id);
            float y = event.getY(pointer1Id);
            initPointer1.x = event.getX(pointer1Id);
            initPointer1.y = event.getY(pointer1Id);
            currPointer1.x = event.getX(pointer1Id);
            currPointer1.y = event.getY(pointer1Id);
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

    private static class Vector2D {
        private float x;
        private float y;

        private Vector2D(float x, float y) {
            this.x = x;
            this.y = y;
        }

        private Vector2D() {
            this.x = 0;
            this.y = 0;
        }

        // a - b
        private static Vector2D getDiff(Vector2D a, Vector2D b) {
            return new Vector2D(a.x - b.x, a.y - b.y);
        }

        private float getLength() {
            return (float)Math.sqrt(x*x + y*y);
        }

        private static Vector2D getNormalized(Vector2D v) {
            float l = v.getLength();
            if (l == 0)
                return new Vector2D();
            else
                return new Vector2D(v.x / l, v.y / l);
        }

        private static float getSignedAngleBetween(Vector2D a, Vector2D b) {
            Vector2D na = getNormalized(a);
            Vector2D nb = getNormalized(b);

            return (float)(Math.atan2(nb.y, nb.x) - Math.atan2(na.y, na.x));
        }

        @Override
        public String toString() {
            return "(" + x + ";" + y + ")";
        }
    }

}