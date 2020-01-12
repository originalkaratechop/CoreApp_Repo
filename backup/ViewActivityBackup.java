package com.example.android.coreapp.backup;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.coreapp.R;

import java.util.ArrayList;
import java.util.List;

public class ViewActivityBackup extends AppCompatActivity {

    ConstraintLayout RootView;
    ImageView ImgCoreView;
    TextView textDebug1;
    TextView textDebug2;
    TextView textRel;

    TextView text_r1;
    TextView text_g1;
    TextView text_b1;
    TextView text_r2;
    TextView text_g2;
    TextView text_b2;
    ImageView core;

    ImageView v_grad1;
    ImageView v_grad2;

    Button btn1;
    Button btn2;

    private boolean touchLiftUp;
    private boolean touchBtn1;
    private boolean touchBtn2;

    int pixelImg;
    int pixelBg;

    GradientDrawable gd1 = new GradientDrawable();
    GradientDrawable gd2 = new GradientDrawable();
    List<Integer> gdAr = new ArrayList<>();
    int[] array;

    Matrix matrix = new Matrix();       // [1] These matrices will be used to move and zoom image
    Matrix savedMatrix = new Matrix();

    static final int NONE = 0;          // We can be in one of these 3 states
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    PointF start = new PointF();        // Remember some things for zooming
    PointF mid = new PointF();
    float oldDist = 1f;
    String savedItemClicked;

    private Rect rect;                  // [2] Variable rect to hold the bounds of the view

    private boolean touchStayedWithinViewBounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag_core);

        RootView = findViewById(R.id.view_root_core);
        ImgCoreView = findViewById(R.id.view_core);
        textDebug1 = findViewById(R.id.view_debug1);
        textDebug2 = findViewById(R.id.view_debug2);
        textRel = findViewById(R.id.view_xy_rel);

        text_r1 = findViewById(R.id.view_r1);
        text_g1 = findViewById(R.id.view_g1);
        text_b1 = findViewById(R.id.view_b1);

        text_r2 = findViewById(R.id.view_r2);
        text_g2 = findViewById(R.id.view_g2);
        text_b2 = findViewById(R.id.view_b2);

        btn1 = findViewById(R.id.view_btn1);
        btn2 = findViewById(R.id.view_btn2);

        v_grad1 = findViewById(R.id.view_gradient1);
        v_grad2 = findViewById(R.id.view_gradient2);

        touchLiftUp = false;
        touchBtn1 = false;
        touchBtn2 = false;

        btn1.setOnClickListener(mClick1);
        btn2.setOnClickListener(mClick2);

        ImgCoreView.post(new Runnable() {
            @Override
            public void run() {
                //getting actual values
                ImgCoreView.measure(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                matrix.postTranslate(ImgCoreView.getWidth() / 2f - ImgCoreView.getMeasuredWidth() / 2f,
                        ImgCoreView.getHeight() / 2f - ImgCoreView.getMeasuredHeight() / 2f);
                ImgCoreView.setImageMatrix(matrix);
                ImgCoreView.setOnTouchListener(mTouchMove);
            }
        });
        gd1.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        gd2.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
    }

    /**
     * button clicker listener
     */
    private View.OnClickListener mClick1 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            touchBtn1 = true;
            touchBtn2 = false;
            touchLiftUp = false;
            if (!touchLiftUp) {
                ImgCoreView.setOnTouchListener(mTouchColorPicker);
                btn1.setText("active");
            }
        }
    };
    private View.OnClickListener mClick2 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            touchBtn1 = false;
            touchBtn2 = true;
            touchLiftUp = false;
            if (!touchLiftUp) {
                ImgCoreView.setOnTouchListener(mTouchColorPicker);
                btn2.setText("active");
            }
        }
    };
    /**
     * test
     */
    private View.OnClickListener mClickTest = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            core.setScaleX(2);
            core.setScaleX(2);
        }
    };

    /**
     * ImageView color touch listener
     */
    private View.OnTouchListener mTouchColorPicker = new View.OnTouchListener() {
        int xRel;
        int yRel;
        int xAbs;
        int yAbs;
        Matrix inverse;
        Matrix outer;
        float[] innerTouchPoint;
        float[] outerTouchPoint;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v == null) return false;

            outer = new Matrix();
            ImgCoreView.getMatrix().invert(outer);
            outerTouchPoint = new float[]{event.getX(), event.getY()};
            outer.mapPoints(outerTouchPoint);
            xAbs = (int) outerTouchPoint[0];
            yAbs = (int) outerTouchPoint[1];
            Drawable background = v.getBackground();
            if (background instanceof ColorDrawable)
                pixelBg = ((ColorDrawable) background).getColor();

            inverse = new Matrix();
            ImgCoreView.getImageMatrix().invert(inverse);
            innerTouchPoint = new float[]{event.getX(), event.getY()};
            inverse.mapPoints(innerTouchPoint);
            xRel = (int) innerTouchPoint[0];
            yRel = (int) innerTouchPoint[1];

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    dumpEvent(event);
                    if (!isMotionEventInsideView(v, xRel, yRel)) {
                        touchStayedWithinViewBounds = true;
                        setPixelToBg(pixelBg);
                        textRel.setText(getString(R.string.view_out) + " " + xAbs + "," + yAbs);
                        return true;
                    } else {
                        touchStayedWithinViewBounds = false;
                        pixelImg = ((BitmapDrawable) ImgCoreView.getDrawable()).getBitmap().getPixel(xRel, yRel);
                        textRel.setText(xRel + "," + xRel + "," + xAbs + "," + yAbs);
                        setPixelToBg(pixelImg);
                        return true;
                    }

                case MotionEvent.ACTION_CANCEL:
                    Toast.makeText(v.getContext(), R.string.view_cancelled, Toast.LENGTH_SHORT).show();
                    return false;

                case MotionEvent.ACTION_UP:
                    pickerStop();
                    if (touchStayedWithinViewBounds) {
                        Toast.makeText(v.getContext(), R.string.view_out, Toast.LENGTH_SHORT).show();
                        return true;
                    } else
                        Toast.makeText(v.getContext(), R.string.view_changed, Toast.LENGTH_SHORT).show();
                    return true;

                default:
                    return false;
            }
        }
    };

    private View.OnTouchListener mTouchMove = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub

            ImageView view = (ImageView) v;
            dumpEvent(event);

            // Handle touch events here...
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    Log.d("DRAG ", "mode=DRAG");
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    Log.d("OLD DIST ", "oldDist=" + oldDist);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                        Log.d("ZOOM ", "mode=ZOOM");
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    Log.d("NONE ", "mode=NONE");
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        // ...
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY()
                                - start.y);
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        textDebug2.setText("newDist " + newDist);
                        //Log.d("NEW DIST ", "newDist=" + newDist);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / oldDist;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                    }
                    break;
            }

            view.setImageMatrix(matrix);
            return true;
        }
    };

    private void dumpEvent(MotionEvent event) {
        String names[] = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("\n[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";\n");
        }
        sb.append("]");
        textDebug1.setText(sb.toString());
        //Log.d("DUMP ", sb.toString());
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Determines whether the provided {@link MotionEvent} represents a touch event
     * that occurred within the bounds of the provided {@link View}.
     *
     * @param view the {@link View} to which the {@link MotionEvent} has been dispatched.
     * @return true if the provided {@link MotionEvent} represents a touch event
     * that occurred within the bounds of the provided {@link View}.
     */
    private boolean isMotionEventInsideImage(View view, int xRel, int yRel) {
        view.measure(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        int rect_left = (view.getWidth() - view.getMeasuredWidth()) / 2;
        int rect_right = (view.getWidth() - view.getMeasuredWidth()) / 2 + view.getMeasuredWidth();
        int rect_top = (view.getHeight() - view.getMeasuredHeight()) / 2;
        int rect_bottom = (view.getHeight() - view.getMeasuredHeight()) / 2 + view.getMeasuredHeight();
        textRel.setText("l " + rect_left + " r " + rect_right + " t " + rect_top + " b " + rect_bottom);
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

    private View.OnTouchListener mTouchTest = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.e("log ", "touch  =");
            return false;
        }
    };

    private void pickerStop() {
        touchLiftUp = !touchLiftUp;
        ImgCoreView.setOnTouchListener(mTouchMove);
        btn1.setText(R.string.view_color1);
        btn2.setText(R.string.view_color2);
    }

    private void setPixelToBg(int px) {
        int rValue = Color.red(px);
        int bValue = Color.blue(px);
        int gValue = Color.green(px);
        int[] ssa;

        if (touchBtn1) {
            text_r1.setText(Integer.toString(rValue));
            text_g1.setText(Integer.toString(gValue));
            text_b1.setText(Integer.toString(bValue));
            btn1.setBackgroundColor(Color.rgb(rValue, gValue, bValue));

            gd1.setColors(new int[]
                    {Color.rgb(rValue, gValue, bValue) ,((ColorDrawable) btn2.getBackground()).getColor()});
            v_grad1.setImageDrawable(gd1);
            gd2.setColors(new int[]
                    {Color.argb(255, rValue, gValue, bValue), Color.argb(0, rValue, gValue, bValue) });
            v_grad2.setImageDrawable(gd2);
        }
        if (touchBtn2) {
            text_r2.setText(Integer.toString(rValue));
            text_g2.setText(Integer.toString(gValue));
            text_b2.setText(Integer.toString(bValue));
            btn2.setBackgroundColor(Color.rgb(rValue, gValue, bValue));

            gd1.setColors(new int[]
                    {((ColorDrawable) btn1.getBackground()).getColor(), Color.rgb(rValue, gValue, bValue)});
            v_grad1.setImageDrawable(gd1);
        }
    }
}