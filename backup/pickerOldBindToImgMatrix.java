package com.example.android.coreapp.backup;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

class pickerOldBindToImgMatrix {

    /*
    private View.OnTouchListener mTouchColorPicker = new View.OnTouchListener() {
        int xRel;
        int yRel;
        int xAbs;
        int yAbs;
        int pixel;
        Matrix inverse;
        Matrix outer;
        float[] innerTouchPoint;
        float[] outerTouchPoint;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v == null) return false;

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    dumpEvent(event);

                    outer = new Matrix();
                    ImgCoreView.getMatrix().invert(outer);
                    outerTouchPoint = new float[]{event.getX(), event.getY()};
                    outer.mapPoints(outerTouchPoint);
                    xAbs = (int) outerTouchPoint[0];
                    yAbs = (int) outerTouchPoint[1];
                    text_r2.setText(xAbs + "," + yAbs);
                    Drawable background = v.getBackground();
                    if (background instanceof ColorDrawable)
                        pixel = ((ColorDrawable) background).getColor();
                    int rValue = Color.red(pixel);
                    int bValue = Color.blue(pixel);
                    int gValue = Color.green(pixel);

                    if (touchBtn1) {
                        text_r1.setText(Integer.toString(rValue));
                        text_g1.setText(Integer.toString(gValue));
                        text_b1.setText(Integer.toString(bValue));
                        btn1.setBackgroundColor(Color.rgb(rValue, gValue, bValue));
                    }
                    if (touchBtn2) {
                        text_r2.setText(Integer.toString(rValue));
                        text_g2.setText(Integer.toString(gValue));
                        text_b2.setText(Integer.toString(bValue));
                        btn2.setBackgroundColor(Color.rgb(rValue, gValue, bValue));
                    }

                    inverse = new Matrix();
                    ImgCoreView.getImageMatrix().invert(inverse);
                    innerTouchPoint = new float[]{event.getX(), event.getY()};
                    inverse.mapPoints(innerTouchPoint);
                    xRel = (int) innerTouchPoint[0];
                    yRel = (int) innerTouchPoint[1];
                    if (!isMotionEventInsideView(v, xAbs, yAbs)) {
                        pickerStop();
                        touchStayedWithinViewBounds = true;
                        if (touchStayedWithinViewBounds = true) {
                            Toast.makeText(v.getContext(), "Out of bounds", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    } else {
                        touchStayedWithinViewBounds = false;
                        pixel = ((BitmapDrawable) ImgCoreView.getDrawable()).getBitmap().getPixel(xAbs, yAbs);
                        textRel.setText(xAbs + "," + yAbs);
                        //then do what you want with the pixel data, e.g
                        rValue = Color.red(pixel);
                        bValue = Color.blue(pixel);
                        gValue = Color.green(pixel);

                        if (touchBtn1) {
                            text_r1.setText(Integer.toString(rValue));
                            text_g1.setText(Integer.toString(gValue));
                            text_b1.setText(Integer.toString(bValue));
                            btn1.setBackgroundColor(Color.rgb(rValue, gValue, bValue));
                        }
                        if (touchBtn2) {
                            text_r2.setText(Integer.toString(rValue));
                            text_g2.setText(Integer.toString(gValue));
                            text_b2.setText(Integer.toString(bValue));
                            btn2.setBackgroundColor(Color.rgb(rValue, gValue, bValue));
                        }
                        return true;
                    }

                case MotionEvent.ACTION_CANCEL:
                    return false;

                case MotionEvent.ACTION_UP:
                    pickerStop();
                    if (touchStayedWithinViewBounds) {
                        Toast.makeText(v.getContext(), "color was changed", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return true;

                default:
                    return false;
            }
        }
    };



    private boolean isMotionEventInsideView(View view, int xRel, int yRel) {
        view.measure(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        int rect_left = (view.getWidth() - view.getMeasuredWidth()) / 2;
        int rect_right = (view.getWidth() - view.getMeasuredWidth()) / 2 + view.getMeasuredWidth();
        int rect_top = (view.getHeight() - view.getMeasuredHeight()) / 2;
        int rect_bottom = (view.getHeight() - view.getMeasuredHeight()) / 2 + view.getMeasuredHeight();
        //textRel.setText("l "+ rect_left + " r "+ rect_right + " t "+ rect_top + " b "+ rect_bottom );
        Rect viewRect = new Rect(
                rect_left,
                rect_top,
                rect_right,
                rect_bottom
        );

        Log.e("log ", "x check = " + rect_left + " " + xRel + " y check = " + rect_top + " " + yRel);
        return viewRect.contains(
                rect_left + xRel,
                rect_top + yRel
        );
    }

    */

    /**
     DEBUG ONLY
     *
     @Override public boolean onTouchEvent(MotionEvent event) {
     if (event.getAction() == MotionEvent.ACTION_UP) {  //ACTION UP
     touchLiftUp = true;
     //    Log.v("MC", "Up ");
     }
     if (event.getAction() == 0) { //ACTION DOWN
     touchLiftUp = false;
     //   Log.v("MC", "Down" );
     }
     int xAbs = (int) event.getX();
     int yAbs = (int) event.getY();
     Log.v("MC", event.getAction() + " ");
     textAbs.setText("Debug: " + xAbs + "," + yAbs);
     return super.onTouchEvent(event);
     }*/
}
