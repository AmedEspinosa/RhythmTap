package com.example.rhythmtapgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomProgressBar extends ProgressBar {
    private Paint paint;
    private RectF rectF;
    private Paint backgroundPaint;
    private int[] gradientColors = new int[]{
            0xFF59B937,
            0xFF77EBC8,
            0xFFFF0000,
            0xFF403DC0,
            0xFFFA00FF,
            0xFF7D60CE
    };

    public CustomProgressBar(@NonNull Context context) {
        super(context);
        init();
    }

    public CustomProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomProgressBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);  // Initialize background paint
        backgroundPaint.setColor(0x80000000);  // Set the background color (customize this as needed)
        rectF = new RectF();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        float progressRatio = (float) getProgress() / getMax();
        int width = getWidth();
        int height = getHeight();
        int progressWidth = (int) (width * progressRatio);

        rectF.set(0, 0, width, height);
        canvas.drawRoundRect(rectF, height / 2f, height / 2f, backgroundPaint);

        rectF.set(0, 0, progressWidth, height);
        LinearGradient shader = new LinearGradient(
                0, 0, width, 0,
                gradientColors,
                null,
                Shader.TileMode.CLAMP
        );
        paint.setShader(shader);
        canvas.drawRoundRect(rectF, height / 2f, height / 2f, paint);
    }
}
