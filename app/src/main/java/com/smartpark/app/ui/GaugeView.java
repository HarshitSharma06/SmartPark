package com.smartpark.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;


public class GaugeView extends View {

    private Paint bgPaint;
    private Paint progressPaint;
    private Paint textPercentPaint;
    private Paint textLabelPaint;
    private RectF arcBounds;

    private int percent = 0;

    public GaugeView(Context context) {
        super(context);
        init();
    }

    public GaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(24f);
        bgPaint.setColor(Color.parseColor("#E0E0E0"));
        bgPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(24f);
        progressPaint.setColor(Color.parseColor("#2E7D32"));
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPercentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPercentPaint.setTextAlign(Paint.Align.CENTER);
        textPercentPaint.setTextSize(54f);
        textPercentPaint.setFakeBoldText(true);
        textPercentPaint.setColor(Color.parseColor("#1565C0"));

        textLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textLabelPaint.setTextAlign(Paint.Align.CENTER);
        textLabelPaint.setTextSize(28f);
        textLabelPaint.setColor(Color.parseColor("#757575"));

        arcBounds = new RectF();
    }

    public void setPercent(int percent) {
        this.percent = Math.max(0, Math.min(100, percent));

        // Color coding: Green >= 80, Amber 50-79, Red < 50
        if (this.percent >= 80) {
            progressPaint.setColor(Color.parseColor("#2E7D32"));
        } else if (this.percent >= 50) {
            progressPaint.setColor(Color.parseColor("#F29900"));
        } else {
            progressPaint.setColor(Color.parseColor("#C62828"));
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float padding = 36f;

        float size = Math.min(width, height) - (padding * 2);
        float left = (width - size) / 2f;
        float top = (height - size) / 2f;

        arcBounds.set(left, top, left + size, top + size);

        // Draw 270-degree background arc
        canvas.drawArc(arcBounds, 135f, 270f, false, bgPaint);

        // Draw progress arc
        float sweepAngle = (percent / 100f) * 270f;
        canvas.drawArc(arcBounds, 135f, sweepAngle, false, progressPaint);

        // Draw center text
        float centerX = width / 2f;
        float centerY = height / 2f;

        canvas.drawText(percent + "%", centerX, centerY + 10f, textPercentPaint);
        canvas.drawText("Chance", centerX, centerY + 45f, textLabelPaint);
    }
}
