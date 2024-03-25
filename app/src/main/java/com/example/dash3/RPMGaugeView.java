package com.example.dash3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class RPMGaugeView extends View{
    private static final int MAX_RPM = 4000;
    private static final int MIN_RPM = 0;
    private int currentRPM = MainActivity.RPM;
    private static int Max_time;           //time of the race will be set from firebase
    private static final int Min_time = 0;
    private int currentValue = 0;
    private Boolean enduro = MainActivity.enduro;
    private Paint backgroundPaint;
    private Paint indicatorPaint;
    private RectF indicatorRect;

    public RPMGaugeView(Context context) {
        super(context);
        init();
    }

    public RPMGaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.GRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);

        indicatorPaint = new Paint();
        indicatorPaint.setColor(Color.GREEN);
        indicatorPaint.setStyle(Paint.Style.FILL);

        indicatorRect = new RectF();
    }

    public void setCurrentValue(int value) {
        if (enduro) {
            if (value <= 0) {
                currentValue = 0;  // Ensure the countdown doesn't go below 0
            } else {
                currentValue = value; // Decrement the countdown value
            }
        } else {
            if (value < MIN_RPM) {
                currentValue = MIN_RPM;
            } else if (value > MAX_RPM) {
                currentValue = MAX_RPM;
            } else {
                currentValue = value;
            }
        }
        invalidate(); // Request a redraw of the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        enduro = MainActivity.enduro;
        Max_time = MainActivity.raceTime;

        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Define colors and paint styles
        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStyle(Paint.Style.FILL);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.GRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);

        Paint rpmPaint = new Paint();

        if(!enduro){                                    //sets colours for rpm guage
            if(currentValue<1300){
                rpmPaint.setColor(Color.YELLOW);
                rpmPaint.setStyle(Paint.Style.FILL);
            }else if (currentValue<1600) {
                rpmPaint.setColor(Color.GREEN);
                rpmPaint.setStyle(Paint.Style.FILL);
            }else if (currentValue<3000) {
                rpmPaint.setColor(Color.YELLOW);
                rpmPaint.setStyle(Paint.Style.FILL);
            }else if (currentValue<3250) {
                rpmPaint.setColor(Color.GREEN);
                rpmPaint.setStyle(Paint.Style.FILL);
            }else if (currentValue<3400) {
                rpmPaint.setColor(Color.YELLOW);
                rpmPaint.setStyle(Paint.Style.FILL);
            }else{
                rpmPaint.setColor(Color.RED);
                rpmPaint.setStyle(Paint.Style.FILL);
            }
        }else{                                              //sets colours for race timer
            if(currentValue< (Max_time/4)){
                Log.d("MaxTime", String.valueOf(Max_time));
                rpmPaint.setColor(Color.RED);
                rpmPaint.setStyle(Paint.Style.FILL);
            } else if (currentValue< (Max_time/2)) {
                rpmPaint.setColor(Color.YELLOW);
                rpmPaint.setStyle(Paint.Style.FILL);
            }else{
                Log.d("MaxTime", String.valueOf(Max_time));
                rpmPaint.setColor(Color.GREEN);
                rpmPaint.setStyle(Paint.Style.FILL);
            }
        }

        // Draw the background
        canvas.drawRect(0, 0, width, height, backgroundPaint);

        // Calculate the interval between lines
        float lineInterval = width / 4;

        //number of lines to draw
        int numLines  = 4;
        // Calculate the indicator width based on currentValue and MAX_RPM or Max_time
        float indicatorWidth;
        if (enduro) {
            indicatorWidth = Math.min((float) currentValue / Max_time * width, width);
            Log.d("MaxTime", String.valueOf(Max_time));
        } else {
            indicatorWidth = Math.min((float) currentValue / MAX_RPM * width, width);
        }
        // Draw the indicator
        indicatorRect.set(0, 0, indicatorWidth, height);
        canvas.drawRect(indicatorRect, rpmPaint);

        // Draw a border around the RPM bar
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4); // Adjust the border width as needed

        RectF borderRect = new RectF(0, 0, width, height);
        canvas.drawRect(borderRect, borderPaint);
        // Draw lines at specified intervals
        for (int i = 1; i < 6; i++) {
            float x = i * lineInterval;
            canvas.drawLine(x, 0, x, height, linePaint);

        } }

    public int getRPM(){
        return currentRPM;
    }
}