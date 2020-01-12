package com.example.android.coreapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class FragmentCore extends Fragment {


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

    LinearLayout container1;
    LinearLayout container2;
    LinearLayout container3;
    LinearLayout container4;

    ColorManager cClass = new ColorManager();

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

    Activity a;

    public FragmentCore() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_frag_core, container, false);
   //     setRetainInstance(true);

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            a=(Activity) context;
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RootView = getActivity().findViewById(R.id.view_root_core);
        ImgCoreView = getActivity().findViewById(R.id.view_core);
        textDebug1 = getActivity().findViewById(R.id.view_debug1);
        textDebug2 = getActivity().findViewById(R.id.view_debug2);
        textRel = getActivity().findViewById(R.id.view_xy_rel);

        text_r1 = getActivity().findViewById(R.id.view_r1);
        text_g1 = getActivity().findViewById(R.id.view_g1);
        text_b1 = getActivity().findViewById(R.id.view_b1);

        text_r2 = getActivity().findViewById(R.id.view_r2);
        text_g2 = getActivity().findViewById(R.id.view_g2);
        text_b2 = getActivity().findViewById(R.id.view_b2);

        btn1 = getActivity().findViewById(R.id.view_btn1);
        btn2 = getActivity().findViewById(R.id.view_btn2);

        v_grad1 = getActivity().findViewById(R.id.view_gradient1);
        v_grad2 = getActivity().findViewById(R.id.view_gradient2);

        container1 = getActivity().findViewById(R.id.view_container1);
        container2 = getActivity().findViewById(R.id.view_container2);
        container3 = getActivity().findViewById(R.id.view_container3);
        container4 = getActivity().findViewById(R.id.view_container4);

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
                //center image
                matrix.postTranslate(ImgCoreView.getWidth() / 2f - ImgCoreView.getMeasuredWidth() / 2f,
                        ImgCoreView.getHeight() / 2f - ImgCoreView.getMeasuredHeight() / 2f);
                ImgCoreView.setImageMatrix(matrix);
                ImgCoreView.setOnTouchListener(mTouchMove);
            }
        });

        gd1.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        gd2.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);

        int iCount = 10;
        ImageView imgVw1[] = new ImageView[iCount];
        ImageView imgVw2[] = new ImageView[iCount];
        ImageView imgVw3[] = new ImageView[iCount];
        ImageView imgVw4[] = new ImageView[iCount];

        for (int i = 0; i < iCount; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 100);
            params.weight = 1;
            imgVw1[i] = new ImageView(getActivity());
            imgVw1[i].setLayoutParams(params);
            imgVw2[i] = new ImageView(getActivity());
            imgVw2[i].setLayoutParams(params);
            imgVw3[i] = new ImageView(getActivity());
            imgVw3[i].setLayoutParams(params);
            imgVw4[i] = new ImageView(getActivity());
            imgVw4[i].setLayoutParams(params);
            container1.addView(imgVw1[i]);
            container2.addView(imgVw2[i]);
            container3.addView(imgVw3[i]);
            container4.addView(imgVw4[i]);
            //   int buttonId = getResources().getIdentifier("opt" + (qRand * 5 + i + 1), "string", getPackageName());
            //   buttons[i].setText(buttonId);
        }

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

            ImageView view = (ImageView) v;

            //get rid of swipe
            view.getParent().requestDisallowInterceptTouchEvent(true);

            outer = new Matrix();
            view.getMatrix().invert(outer);
            outerTouchPoint = new float[]{event.getX(), event.getY()};
            outer.mapPoints(outerTouchPoint);
            xAbs = (int) outerTouchPoint[0];
            yAbs = (int) outerTouchPoint[1];
            Drawable background = v.getBackground();
            if (background instanceof ColorDrawable)
                pixelBg = ((ColorDrawable) background).getColor();

            inverse = new Matrix();
            view.getImageMatrix().invert(inverse);
            innerTouchPoint = new float[]{event.getX(), event.getY()};
            inverse.mapPoints(innerTouchPoint);
            xRel = (int) innerTouchPoint[0];
            yRel = (int) innerTouchPoint[1];

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    dumpEvent(event);
                    colorSpread();
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
                    pickerStop();
                    Toast.makeText(v.getContext(), R.string.view_cancelled, Toast.LENGTH_SHORT).show();
                    return false;

                case MotionEvent.ACTION_UP:
                    colorSpread();
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

            //get rid of swipe
            view.getParent().requestDisallowInterceptTouchEvent(true);

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

        //Log.e("log ", "x check = " + rect_left + " " + xRel + " y check = " + rect_top + " " + yRel);
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

        if (touchBtn1) {
            text_r1.setText(Integer.toString(rValue));
            text_g1.setText(Integer.toString(gValue));
            text_b1.setText(Integer.toString(bValue));
            btn1.setBackgroundColor(Color.rgb(rValue, gValue, bValue));

            gd1.setColors(new int[]
                    {Color.rgb(rValue, gValue, bValue), ((ColorDrawable) btn2.getBackground()).getColor()});
            v_grad1.setImageDrawable(gd1);

            gd2.setColors(new int[]
                    {Color.argb(255, rValue, gValue, bValue), Color.argb(0, rValue, gValue, bValue)});
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

    public void colorSpread() {

        int r, g, b;
        r = g = b = 0;
        int c1r, c1g, c1b;
        c1r = c1g = c1b = 0;
        int c2r, c2g, c2b;
        c2r = c2g = c2b = 0;
        List<Integer> colorArray1;
        List<Integer> colorArray2;
        List<Integer> mix1Array;
        List<Integer> mix2Array;
        List<Integer> mix3Array;
        List<Integer> mix4Array;

        if (btn1.getBackground() instanceof ColorDrawable) {
            int colorCode = ((ColorDrawable) btn1.getBackground()).getColor();
        /*    for (int j : array1) {
                array1.get(array1.indexOf(j));
                Log.e("color ", " " + j);
            }*/

            // TODO:
            colorArray1 = cClass.getColorArray(colorCode);
            c1r = colorArray1.get(0);
            c1g = colorArray1.get(1);
            c1b = colorArray1.get(2);
        }
        if (btn2.getBackground() instanceof ColorDrawable) {
            int colorCode = ((ColorDrawable) btn2.getBackground()).getColor();
            colorArray2 = cClass.getColorArray(colorCode);
            c2r = colorArray2.get(0);
            c2g = colorArray2.get(1);
            c2b = colorArray2.get(2);
        }

        for (int i = 0; i < container1.getChildCount(); i++) {

            double a;
            double dr;
            double dg;
            double db;
            int alpha;
            a = i; // alpha calculation

            int b1 = ((ColorDrawable) btn1.getBackground()).getColor();
            int b2 = ((ColorDrawable) btn2.getBackground()).getColor();

            if (i == 1) a = container1.getChildCount() - (a + 1.1f);
            else if (i < 9) a = container1.getChildCount() - (a + 1);
            else a = container1.getChildCount() - (a + 0.9f);
            a = a / 10;
            alpha = (int) (a * 255);
            //       Log.e("alpha ", " " + (a));

    /*  1      Palette   rgb(51,181,229)
        2    rgb(65,186,207)
        3    rgb(80,192,185)
        4    rgb(94,197,162)
        5    rgb(109,203,140)
        6    rgb(123,208,118)
        7    rgb(138,214,96)
        8    rgb(152,219,73)
        9    rgb(167,225,51)
        10    rgb(181,230,29)  */

//blend wiki REVERSED Alpha Compositing  short a mix=1  rd = (array2.get(0) * (a + 1 * (1 - a)) - array1.get(0) * 1 * (1 - a)) / a; //original full formula

            mix1Array = cClass.mixAlphaCompositing(a,
                    c1r, c1g, c1b,
                    c2r, c2g, c2b);
            r = mix1Array.get(0);
            g = mix1Array.get(1);
            b = mix1Array.get(2);
            if (r > 255 || r < 0 || g > 255 || g < 0 || b > 255 || b < 0) {
                if (container1.getChildAt(i) instanceof ImageView) {
                    (container1.getChildAt(i)).setBackgroundColor(0);
                }
            } else if (container1.getChildAt(i) instanceof ImageView) {
                (container1.getChildAt(i)).setBackgroundColor(Color.argb(255, r, g, b));
            }

//blend wiki REVERSED Gamma (cie 1931)

            mix2Array = cClass.mixInverseGamma(a,
                    c1r, c1g, c1b,
                    c2r, c2g, c2b);
            r = mix2Array.get(0);
            g = mix2Array.get(1);
            b = mix2Array.get(2);
            //  Log.e("clr ", Math.round(r) +" "+  Math.round(g)+" "+Math.round(b));
            // Log.e("255alpha ", " " + g);
            if (r > 255 || r < 0 || g > 255 || g < 0 || b > 255 || b < 0) {
                if (container2.getChildAt(i) instanceof ImageView) {
                    (container2.getChildAt(i)).setBackgroundColor(0);
                }
            } else if (container2.getChildAt(i) instanceof ImageView) {
                (container2.getChildAt(i)).setBackgroundColor(Color.argb(255, r, g, b));
            }

//blend LAB
            mix3Array = cClass.mixLAB(a,
                    c1r, c1g, c1b,
                    c2r, c2g, c2b);
            r = mix3Array.get(0);
            g = mix3Array.get(1);
            b = mix3Array.get(2);

            if (r > 255 || r < 0 || g > 255 || g < 0 || b > 255 || b < 0) {
                if (container3.getChildAt(i) instanceof ImageView) {
                    (container3.getChildAt(i)).setBackgroundColor(0);
                }
            } else if (i > 1 && (r == 0 && g == 0 && b == 0) || (r == 255 && g == 255 && b == 255)) {
                int cc1 = ((ColorDrawable) container3.getChildAt(i - 1).getBackground()).getColor();
                int cc2 = ((ColorDrawable) container3.getChildAt(i).getBackground()).getColor();
                if (Color.red(cc1) == 0 && Color.green(cc1) == 0 && Color.blue(cc1) == 0
                        && Color.red(cc2) == 0 && Color.green(cc2) == 0 && Color.blue(cc2) == 0) {
                    if (container3.getChildAt(i) instanceof ImageView) {
                        (container3.getChildAt(i)).setBackgroundColor(0);
                    }
                }
            } else if (container3.getChildAt(i) instanceof ImageView) {
                (container3.getChildAt(i)).setBackgroundColor(Color.argb(255, r, g, b));
                int cc = ((ColorDrawable) container3.getChildAt(i).getBackground()).getColor();
                //  Log.e("LABtest ", Color.red(cc) +" "+  Color.green(cc)+" "+Color.blue(cc));
            }

//blend wiki REVERSED experimental Gamma ( 0.43 gamma for grays)

            mix4Array = cClass.mixExperimentalGamma(a,
                    c1r, c1g, c1b,
                    c2r, c2g, c2b);
            r = mix4Array.get(0);
            g = mix4Array.get(1);
            b = mix4Array.get(2);
            // Log.e("255alpha ", " " + g);
            if (r > 255 || r < 0 || g > 255 || g < 0 || b > 255 || b < 0) {
                if (container4.getChildAt(i) instanceof ImageView) {
                    (container4.getChildAt(i)).setBackgroundColor(0);
                }
            } else if (container4.getChildAt(i) instanceof ImageView) {
                (container4.getChildAt(i)).setBackgroundColor(Color.argb(255, r, g, b));
            }
 /*           a = a * 255;
            int alpha = (int) a;
            Log.e("a ", " " + alpha);
            if (container1.getChildAt(i) instanceof ImageView) {
                (container1.getChildAt(i)).setBackgroundColor(Color.argb(alpha, r, g, b));
            }*/
            //    (c mixed btn2 *(a + 1 * (1 - a)) - c1 btn1 * 1 * (1 - a))/a = (c2 x );
            //  Log.e("a ", "" + a);

        }
    }
}