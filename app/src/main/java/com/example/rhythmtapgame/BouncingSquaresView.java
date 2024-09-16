package com.example.rhythmtapgame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BouncingSquaresView extends View {
    private static final int SQUARE_SIZE = 200;
    private static final int SPEED = 5;
    private int navigationMenuHeight = 0;
    private final List<Square> squares = new ArrayList<>();
    private final Paint paint = new Paint();
    private boolean initialized = false;
    private SoundPool soundPool;
    private List<Integer> bounceSound;
    private int id = 1;
    private SharedPreferences sharedPreferences;
    private boolean isPaused = false;

    public BouncingSquaresView(Context context) {
        super(context);
        initSoundPool(context);
        loadSettings(context);
    }

    public BouncingSquaresView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initSoundPool(context);
        loadSettings(context);
    }

    public void pauseBouncing() {
        isPaused = true;
        soundPool.autoPause(); // Pauses all sounds currently playing in the SoundPool
        invalidate(); // Stops the view from redrawing
    }

    public void resumeBouncing() {
        isPaused = false;
        soundPool.autoResume(); // Resumes all paused sounds in the SoundPool
        invalidate(); // Forces the view to redraw
    }

    private void loadSettings(Context context) {
        sharedPreferences = context.getSharedPreferences("GameSettings", Context.MODE_PRIVATE);
    }


    @SuppressLint("DiscouragedApi")
    private void initSoundPool(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        bounceSound = new ArrayList<>();

        for (int i = 1; i < 28; i++) {
            String name = "tile_" + i;
            int trackRedId = getResources().getIdentifier(name, "raw", context.getPackageName());
            if (trackRedId != 0) { // Ensure the resource exists
                Log.e(context.getPackageName(), "Bounce Track Id: " + trackRedId);
                int soundId = soundPool.load(context, trackRedId, 1);
                Log.e(context.getPackageName(), "Loaded sound ID: " + soundId);
                bounceSound.add(soundId);
            } else {
                Log.e(context.getPackageName(), "Sound file not found: " + name);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        View navigationMenu = ((MainActivity) getContext()).findViewById(R.id.navigationMenu);
        if (navigationMenu != null) {
            navigationMenu.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    navigationMenuHeight = navigationMenu.getHeight();
                    navigationMenu.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (!initialized) {
                        initSquares(w, h);
                        initialized = true;
                    }
                }
            });
        } else {
            if (!initialized) {
                initSquares(w, h);
                initialized = true;
            }
        }
    }

    private void initSquares(int width, int height) {
        Random random = new Random();
        int index = random.nextInt((5) + 1);
        int x = random.nextInt(width - SQUARE_SIZE);
        int y = random.nextInt(height - SQUARE_SIZE - navigationMenuHeight);
        int dx = random.nextBoolean() ? SPEED : -SPEED;
        int dy = random.nextBoolean() ? SPEED : -SPEED;
        squares.add(new Square(x, y, dx, dy, getColor(index)));
    }


    private int getColor(int index) {
        List<Integer> colors = Arrays.asList(Color.rgb(76, 73, 221),
                Color.rgb(217, 50, 50), Color.rgb(89, 185, 55),
                Color.rgb(85, 229, 186), Color.rgb(125, 96, 206),
                Color.rgb(249, 218, 102));

        return colors.get(index);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (!isPaused) {
            for (Square square : squares) {
                paint.setColor(square.color);
                canvas.drawRect(square.x, square.y, square.x + SQUARE_SIZE, square.y + SQUARE_SIZE, paint);
                updateSquarePosition(square);
            }
        }
        if (!isPaused) {
            invalidate();
        }
    }

    private void updateSquarePosition(Square square) {
        boolean bounced = false;

        square.x += square.dx;
        square.y += square.dy;

        if (square.x <= 0 || square.x + SQUARE_SIZE >= getWidth()) {
            square.dx *= -1;
            bounced = true;
        }
        if (square.y <= 0 || square.y + SQUARE_SIZE >= getHeight() - navigationMenuHeight) {
            square.dy *= -1;
            bounced = true;
        }

        if (bounced) {
            playBounceSound();
            id++;
            checkColor(square);
        }
    }

    private void checkColor(Square square) {
        Random random = new Random();
        int index = random.nextInt((5) + 1);

        int currColor = square.color;
        int newColor = getColor(index);

        if (currColor == newColor) {
            square.color = getColor(random.nextInt((5) + 1));
            checkColor(square);
        } else square.color = newColor;

    }

    private void playBounceSound() {
        boolean isSoundEffectsOn = sharedPreferences.getBoolean("isSoundEffectsOn", true);
        int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);

        if (id < 27) {
            if (!bounceSound.isEmpty() && isSoundEffectsOn) {
                int soundId = bounceSound.get(id);
                int result = soundPool.play(soundId, soundEffectsVolume / 100f, soundEffectsVolume / 100f, 0, 0, 1);
                if (result == 0) {
                    Log.e("SoundPool", "Failed to play sound with ID: " + soundId);
                }
            } else {
                Log.e("SoundPool", "No sounds loaded.");
            }
        } else
            id = 1;
        int soundId = bounceSound.get(id);
        soundPool.play(soundId, soundEffectsVolume / 100f, soundEffectsVolume / 100f, 0, 0, 1);
    }

    public void updateVolume(float volume) {
        for (Integer ids : bounceSound) {
            soundPool.setVolume(ids, volume, volume);
        }
    }

    private static class Square {
        int x, y, dx, dy, color;

        Square(int x, int y, int dx, int dy, int color) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.color = color;
        }
    }
}
