package com.example.dash3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class RPMGaugeView extends View{
    private static final int MAX_RPM = 4000;
    private static final int MIN_RPM = 0;
    private DatabaseReference rpmRef;
    private int currentRPM = MainActivity.RPM;

    public RPMGaugeView(Context context) {
        super(context);
        init();
    }

    public RPMGaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initializeFirebase();
    }

    private void init() {
        currentRPM = 0;
    }
    private void initializeFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        rpmRef = database.getReference("RPM"); // Replace with your actual path
    }
    public void setCurrentRPM(int rpm) {
        if (rpm < MIN_RPM) {
            currentRPM = MIN_RPM;
        } else if (rpm > MAX_RPM) {
            currentRPM = MAX_RPM;
        } else {
            currentRPM = rpm;
        }
        invalidate();     }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Define colors and paint styles
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.GRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);

        Paint rpmPaint = new Paint();
        if(currentRPM<1300){
            rpmPaint.setColor(Color.YELLOW);
            rpmPaint.setStyle(Paint.Style.FILL);
        }else if (currentRPM<1600) {
            rpmPaint.setColor(Color.GREEN);
            rpmPaint.setStyle(Paint.Style.FILL);
        }else if (currentRPM<3000) {
            rpmPaint.setColor(Color.YELLOW);
            rpmPaint.setStyle(Paint.Style.FILL);
        }else if (currentRPM<3250) {
            rpmPaint.setColor(Color.GREEN);
            rpmPaint.setStyle(Paint.Style.FILL);
        }else if (currentRPM<3400) {
            rpmPaint.setColor(Color.YELLOW);
            rpmPaint.setStyle(Paint.Style.FILL);
        }else{
            rpmPaint.setColor(Color.RED);
            rpmPaint.setStyle(Paint.Style.FILL);
        }

        // Draw the background
        canvas.drawRect(0, 0, width, height, backgroundPaint);

        // Define paint for the lines
        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStyle(Paint.Style.FILL);
        //number of lines to draw
        int numLines  = 6;
        // Calculate the interval between lines
        float lineInterval = width / numLines;

        // Calculate the width of the RPM indicator
        float indicatorWidth = (float) currentRPM / MAX_RPM * width;

        // Draw the RPM indicator
        canvas.drawRect(0, 0, indicatorWidth, height, rpmPaint);

        // Draw a border around the RPM bar
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4); // Adjust the border width as needed

        RectF borderRect = new RectF(0, 0, width, height);
        canvas.drawRect(borderRect, borderPaint);
        // Draw lines at specified intervals
        for (int i = 1; i < numLines; i++) {
            float x = i * lineInterval;
            canvas.drawLine(x, 0, x, height, linePaint);
        } }

    public int getRPM(){
        return currentRPM;
    }
}