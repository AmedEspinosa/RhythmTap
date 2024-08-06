package com.example.rhythmtapgame;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;

import android.text.TextPaint;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rhythmtapgame.R;

public class PauseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pause_menu);

        TextView title = findViewById(R.id.pauseTitle);

        title.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (title.getWidth() > 0 && title.getHeight() > 0) {
                    TextPaint paint = title.getPaint();
                    float width = paint.measureText(title.getText().toString());

                    LinearGradient shader = new LinearGradient(
                            0, 0, width, 0,
                            new int[]{
                                    Color.parseColor("#D93232"),  // Red color at 0%
                                    Color.parseColor("#9504AC"),  // Purple color at 34%
                                    Color.parseColor("#FFFFFF")   // White color at 100%
                            },
                            new float[]{0f, 0.34f, 1f},
                            Shader.TileMode.CLAMP);

                    paint.setShader(shader);
                    title.setTextColor(Color.WHITE); // Set a base color
                    title.invalidate(); // Force redraw
                }
            }
        });
    }
}