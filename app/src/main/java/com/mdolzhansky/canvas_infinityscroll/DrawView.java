package com.mdolzhansky.canvas_infinityscroll;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mdolzhansky.canvas_infinityscroll.model.Cell;

/**
 *  Created by mmdj on 29.04.2017.
 */

class DrawView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder surfaceHolder;
    private int screenWidth;
    private int screenHeight;

    private DrawThread drawThread;
    private Paint paint;
    private Rect rect;
    private Cell cell;

    private int CELL_WIDTH =250;
    private int CELL_HEIGHT = 250;
    private int START_POSITION_X = 0;
    private int START_POSITION_Y = 0;
    int nCellsHorizontal = 0;
    int nCellsVertical = 0;

    private float mPosX = 0, mPosY = 0;
    private float mScaleFactor = 1.f;  // indicate the scalling
    private float mLastTouchX;
    private float mLastTouchY;
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
    GestureDetector mTapListener;
    ScaleGestureDetector mScaleDetector;


    /* **********Start constructors***************************/
    public DrawView(Context context) {
        super(context);
        ini(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ini(context);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ini(context);
    }

    public void ini(Context context) {
        Log.e("SurfaceViewFun", "ini");
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        iniTouchHandling(context);
    }

    public void iniTouchHandling(Context context) {
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mTapListener = new GestureDetector(context, new TapListener());
    }
    /* **********End constructors***************** */


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(getHolder());
        drawThread.setRunning(true);
        drawThread.start();
        setFocusable(true);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private class DrawThread extends Thread {

        private boolean running = false;
        private final SurfaceHolder surfaceHolder;
        Canvas canvas;

        boolean done;
        long startTime;

        float duration = 5 * 1000;
        float arcSweep;


        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;

            //brush:
            paint = new Paint();
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
            paint.setTextSize(64);
            paint.setTextAlign(Paint.Align.CENTER);


            rect = new Rect();


        }


        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {

            while (running) {
                canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    if (canvas == null)
                        continue;

                        /*  *************from here drawing: ************ */

                    //background
                    canvas.drawColor(Color.BLACK);

                    //Rectangulars:
                    setRect();
                    startTime = System.currentTimeMillis();
                    arcSweep = 0;
                    done = false;
                   // while (!done) {  // draw until done
                       // updateArcSweep();
                        //drawCurrentArc();
                    //}

                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);

                    }
                }
            }

        }

        public void setRect() {



            synchronized (surfaceHolder) {

                float cellRealWidth = CELL_WIDTH * mScaleFactor;
                float cellRealHeight = CELL_HEIGHT * mScaleFactor;

                nCellsHorizontal = (int) (((screenWidth * 3) / cellRealWidth) + 2);
                nCellsVertical = (int) (((screenHeight * 3) / cellRealHeight) + 2);

                int startX = (int) (START_POSITION_X - screenWidth + mPosX);
                int startY = (int) (START_POSITION_Y - screenHeight + mPosY);

                for (int i = 0; i <= nCellsHorizontal; i++) {
                    for (int j = 0; j <= nCellsVertical; j++) {

                        rect.set((int) (startX + (cellRealWidth * i))                //left
                                , (int) (startY + (cellRealHeight * j))              //top
                                , (int) (startX + (cellRealWidth * (i + 1)))         //right
                                , (int) (startY + (cellRealHeight * (j + 1))));      //bottom

                      //  cell = new Cell(i - (screenWidth / CELL_WIDTH) - 1, -1 * (j - (screenHeight * 2 / CELL_HEIGHT)) - 2, rect);
                        canvas.drawRect(rect, paint);

                      //  canvas.drawText(cell.toString(), rect.centerX(), rect.centerY(), paint);
                        //TODO setTextPositionInCell()
                               /* // Подсчитаем размер текста
                                mPaint.getTextBounds(text, 0, text.length(), mTextBoundRect);
                                //mTextWidth = textBounds.width();
                                // Используем measureText для измерения ширины
                                mTextWidth = mPaint.measureText(text);
                                mTextHeight = mTextBoundRect.height();

                                canvas.drawText(text,
                                        centerX - (mTextWidth / 2f),
                                        centerY + (mTextHeight /2f),
                                        mPaint
                                );*/
                    }
                }

                canvas.drawText(nCellsHorizontal + "x" + nCellsVertical, screenWidth - 250, screenHeight - 300, paint);
            }
        }

      /*  void updateArcSweep() {
            long currentTime = System.currentTimeMillis();
            arcSweep = (float) ((currentTime - startTime) / duration * 360.0);
            if (currentTime - startTime > duration) {
                done = true;
                arcSweep = 360;
            }
        }

        void drawCurrentArc() {
            canvas = surfaceHolder.lockCanvas(null); // must lock before drawing
            canvas.drawColor(Color.BLACK);    // clear the screen
        //    canvas.drawArc(rect, 0, arcSweep, true, drawingPaint);  // draw new stuff
            surfaceHolder.unlockCanvasAndPost(canvas);  // must unlock when done
        }*/


    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // let our gesture detectors process the events
        mScaleDetector.onTouchEvent(ev);
        mTapListener.onTouchEvent(ev);
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

// Remember where we started
                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                if (!mScaleDetector.isInProgress()) {
// Calculate the distance moved
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

// Move the object
                    mPosX += dx;
                    mPosY += dy;

// Remember this touch position for the next move event
                    mLastTouchX = x;
                    mLastTouchY = y;

                    drawThread.setRect();
                  //  if (drawThread.done) drawThread.drawCurrentArc();

                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
// Extract the index of the pointer that left the touch sensor
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
// This was our active pointer going up. Choose a new
// active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }// end switch statement
        return true;// end onTouch
    }


    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {


        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
// Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

//invalidate(); // use this for a regular canvas (i.e., not a SurfaceView)
            drawThread.setRect();
           // if (drawThread.done) drawThread.drawCurrentArc();

            return true;
        }
    }


    public class TapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.e("SurfaceViewFun", "double tap " + e.getX() + " " + e.getY());
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.e("SurfaceViewFun", "got long press at location x=" + e.getX() + " y=" + e.getY());
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.e("SurfaceViewFun", "fling: started at (" + e1.getX() + " ," + e1.getY()
                    + "). Ended at (" + e2.getX() + " ," + e2.getY() + "). With velocity ("
                    + velocityX + " ," + velocityY + ")");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.e("SurfaceViewFun", "scroll: started at (" + e1.getX() + " ," + e1.getY()
                    + "). Ended at (" + e2.getX() + " ," + e2.getY()
                    + "). With total distance (" + velocityX + " ," + velocityY + ")");

            if (e1.getX() < e2.getX()) {
                Log.d("Gesture ", "Left to Right swipe: " + e1.getX() + " - " + e2.getX());
                Log.d("Speed ", String.valueOf(velocityX) + " pixels/second");
            }
            if (e1.getX() > e2.getX()) {
                Log.d("Gesture ", "Right to Left swipe: " + e1.getX() + " - " + e2.getX());
                Log.d("Speed ", String.valueOf(velocityX) + " pixels/second");
            }
            if (e1.getY() < e2.getY()) {
                Log.d("Gesture ", "Up to Down swipe: " + e1.getX() + " - " + e2.getX());
                Log.d("Speed ", String.valueOf(velocityY) + " pixels/second");
            }
            if (e1.getY() > e2.getY()) {
                Log.d("Gesture ", "Down to Up swipe: " + e1.getX() + " - " + e2.getX());
                Log.d("Speed ", String.valueOf(velocityY) + " pixels/second");
            }

            return true;
        }
    }
}



