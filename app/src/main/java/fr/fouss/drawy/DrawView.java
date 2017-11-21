package fr.fouss.drawy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {
    Bitmap drawing;
    Canvas canvas;
    Paint paint;

    float lastX, lastY;

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
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(drawing, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d("DrawView", "onTouchEvent: drawing type : "
//                + event.getAction()
//                + " ; position : "
//                + event.getX()
//                + ";"
//                + event.getY());
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
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            canvas.drawPoint(x, y, paint);
            invalidate();
        }
        return super.onTouchEvent(event);
    }
}