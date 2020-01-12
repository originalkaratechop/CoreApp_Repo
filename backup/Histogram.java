package com.example.android.coreapp;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

class Histogram extends View {

    public Histogram(Context context, Bitmap bi) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (loaded) {
            canvas.drawColor(Color.GRAY);

            // size - 1 = colors on xAxis, WIDTH_XML = pixels in xml layout (wrong to use)
            float xInterval = (float) ((double)  getWidth()  / ((double) SIZE-1));

            Log.e("HI", "H : " + getHeight()
                    + ", W : " + getWidth()
                    + ", xInt : " + xInterval
                    + ", lineTo : " + xInterval * offset);

            for (int i = 0; i < NUMBER_OF_COLOURS; i++) {

                Paint wallpaint;
                //   Log.e("HISTO", NUMBER_OF_COLOURS + " id " + i);

                wallpaint = new Paint();
                if (isColored) {
                    if (i == RED) {
                        wallpaint.setColor(Color.RED);
                    } else if (i == GREEN) {
                        wallpaint.setColor(Color.GREEN);
                    } else if (i == BLUE) {
                        wallpaint.setColor(Color.BLUE);
                    }
                } else {
                    wallpaint.setColor(Color.WHITE);
                }

                wallpaint.setStyle(Paint.Style.STROKE);


                Path wallpath = new Path();
                wallpath.reset();
                wallpath.moveTo(0, getHeight());
                for (int j = 0; j < SIZE; j++) {
                    int value = (int) (((double) colourBins[i][j] / (double) maxY) * (getHeight()));
                    wallpath.lineTo(j * xInterval * offset, getHeight() - value);
                    if (j > SIZE - 3) Log.e("HISTO"," j " + j * xInterval * offset);

                }
                //  wallpath.lineTo(SIZE * offset, getHeight());
                canvas.drawPath(wallpath, wallpaint);
            }
        }
    }
}
