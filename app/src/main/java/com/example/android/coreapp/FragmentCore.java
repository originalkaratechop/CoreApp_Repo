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
import android.support.v4.graphics.ColorUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FragmentCore extends Fragment {

    ConstraintLayout RootView;
    ImageView ImgCoreView;
    TextView textDebug1, textDebug2, textRel, textDebug3;

    TextView text_r1, text_g1, text_b1,
            text_d2, text_r2, text_g2, text_b2,
            txt1, txt2, txt3, txt4;
    ImageView core;
    ImageView v_grad1, v_grad2;

    Button btn1, btn2;

    View filler, v1, v2, v3, v4;

    private boolean touchLiftUp, touchBtn1, touchBtn2;

    int pixelImg, pixelBg;

    GradientDrawable gd1 = new GradientDrawable();
    GradientDrawable gd2 = new GradientDrawable();

    LinearLayout container1, container2, container3, container4;

    ColorManager cClass = new ColorManager();

    Matrix matrix = new Matrix();       // [1] These matrices will be used to move and zoom image
    Matrix savedMatrix = new Matrix();

    private static final int NONE = 0;          // We can be in one of these 3 states
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    int mode = NONE;

    private static final int LAB_L_MAX = 94;
    private static final int DE_MAX = 24;

    PointF start = new PointF();        // Remember some things for zooming
    PointF mid = new PointF();
    float oldDist = 1f;
    float newDist = 1f;
    String savedItemClicked;

    private Rect rect;                  // [2] Variable rect to hold the bounds of the view

    private boolean touchStayedWithinViewBounds;

    Activity a;

    SendReference SR;
    View rootView;

    public FragmentCore() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_frag_core, container, false);
        //     setRetainInstance(true);

        return rootView;
    }

    interface SendReference {
        void sendReferenceList(List<Double> reference);
        void sendReferenceDouble(double reference);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            a = (Activity) context;
        }

        try {
            SR = (SendReference) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Error in retrieving data. Please try again");
        }
    }
 //didbnt work

    public void resetZoom() {
        rootView.post(new Runnable() {
            @Override
            public void run() {
                matrix = new Matrix();
                oldDist = 1f;
                newDist = 1f;
                rootView.invalidate();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RootView = view.findViewById(R.id.view_root_core);
        ImgCoreView = view.findViewById(R.id.view_core);
        textDebug1 = view.findViewById(R.id.view_debug1);
        textDebug2 = view.findViewById(R.id.view_debug2);
        textRel = view.findViewById(R.id.view_xy_rel);
        textDebug3 = view.findViewById(R.id.view_debug3);

        text_r1 = view.findViewById(R.id.view_r1);
        text_g1 = view.findViewById(R.id.view_g1);
        text_b1 = view.findViewById(R.id.view_b1);

        text_d2 = view.findViewById(R.id.view_d2);
        text_r2 = view.findViewById(R.id.view_r2);
        text_g2 = view.findViewById(R.id.view_g2);
        text_b2 = view.findViewById(R.id.view_b2);

        btn1 = view.findViewById(R.id.view_btn1);
        btn2 = view.findViewById(R.id.view_btn2);

        v_grad1 = view.findViewById(R.id.view_gradient1);
        v_grad2 = view.findViewById(R.id.view_gradient2);

        container1 = view.findViewById(R.id.view_container1);
        container2 = view.findViewById(R.id.view_container2);
        container3 = view.findViewById(R.id.view_container3);
        container4 = view.findViewById(R.id.view_container4);

        filler = view.findViewById(R.id.view_filler1);

        txt1 = view.findViewById(R.id.txt1);
        txt2 = view.findViewById(R.id.txt2);
        txt3 = view.findViewById(R.id.txt3);
        txt4 = view.findViewById(R.id.txt4);
        v1 = view.findViewById(R.id.v1);
        v2 = view.findViewById(R.id.v2);
        v3 = view.findViewById(R.id.v3);
        v4 = view.findViewById(R.id.v4);

        touchLiftUp = false;
        touchBtn1 = false;
        touchBtn2 = false;

        btn1.setOnClickListener(mClick1);
        btn2.setOnClickListener(mClick2);

        ImgCoreView.post(new Runnable() {
            @Override
            public void run() {
                resetZoom();
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
                        newDist = spacing(event);
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
        int gValue = Color.green(px);
        int bValue = Color.blue(px);

        double[] lab = new double[3];
        ColorUtils.RGBToLAB(Color.red(px), Color.green(px), Color.blue(px), lab);

        // Log.e("RGBToLAB", lab[0] + " " + lab[1] + " "+ lab[2]) ;
        String labL = Integer.toString((int) Math.round(lab[0]));
        String labA = Integer.toString((int) Math.round(lab[1] + 87));
        String labB = Integer.toString((int) Math.round(lab[2] + 103));
        TextView lo_histSeparator;
        lo_histSeparator = getActivity().findViewById(R.id.lo_separator);
        //String l = getResources(R.string.app_name);

        if (touchBtn1) {
            // text_r1.setText(Integer.toString(rValue)); //before
            text_r1.setText(labL);
            text_g1.setText(labA);
            text_b1.setText(labB);
            lo_histSeparator.setText("Color 1: L* " + labL + " a " + labA + " b " + labB + "\n"
                    + "Color 2: L* " + text_r2.getText() + " a " + text_g2.getText() + " a " + text_b2.getText());

            btn1.setBackgroundColor(Color.rgb(rValue, gValue, bValue));
//
//            gd1.setColors(new int[]
//                    {Color.rgb(rValue, gValue, bValue), ((ColorDrawable) btn2.getBackground()).getColor()});
//            v_grad1.setImageDrawable(gd1);

            gd2.setColors(new int[]
                    {Color.argb(255, rValue, gValue, bValue), Color.argb(0, rValue, gValue, bValue)});
            v_grad2.setImageDrawable(gd2);
        }

        if (touchBtn2) {
//            // text_r2.setText(Integer.toString(rValue)); //before
            // 1-100
//            text_r2.setText(Integer.toString((int) Math.round((lab[0] + 110.0) / 2.1)));
//            text_g2.setText(Integer.toString((int) Math.round((lab[1] + 110.0) / 2.1)));
//            text_b2.setText(Integer.toString((int) Math.round((lab[2] + 110.0) / 2.1)));

            text_r2.setText(labL);
            text_g2.setText(labA);
            text_b2.setText(labB);
            lo_histSeparator.setText("Color 1: L* " + text_r1.getText() + " a " + text_g1.getText() + " a " + text_b1.getText() + "\n"
                    + "Color 2: L* " + labL + " a " + labA + " b " + labB);

            btn2.setBackgroundColor(Color.rgb(rValue, gValue, bValue));

            gd1.setColors(new int[]
                    {((ColorDrawable) btn1.getBackground()).getColor(), Color.rgb(rValue, gValue, bValue)});
            v_grad1.setImageDrawable(gd1);

            int colorCode = ((ColorDrawable) btn1.getBackground()).getColor();
            List<Integer> colorArray2 = cClass.getColorArray(colorCode);
            int c2r = colorArray2.get(0);
            int c2g = colorArray2.get(1);
            int c2b = colorArray2.get(2);

            double[] lab1 = new double[3];
            double[] lab2 = new double[3];
            ColorUtils.RGBToLAB(c2r, c2g, c2b, lab1);
            ColorUtils.RGBToLAB(rValue, gValue, bValue, lab2);

            double[] lab1_100 = new double[3];
            lab1_100[0] = LAB_L_MAX;
            lab1_100[1] = lab1[1];
            lab1_100[2] = lab1[2];
            double[] lab2_100 = new double[3];
            lab2_100[0] = LAB_L_MAX;
            lab2_100[1] = lab[1];
            lab2_100[2] = lab[2];
            double dE = cClass.calcDE(lab1_100, lab2_100);
            double dE2 = cClass.calcDE(lab1, lab2);
            text_d2.setText(Integer.toString((int) Math.round(dE2)) + " max " + Integer.toString((int) Math.round(dE)));

        }
    }

    public void colorSpread() {

        int r, g, b, max_a;
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

        double[] lab_ref = new double[3];
        double[] lab_sample = new double[3];
        double[] lab_ref_normal = new double[3];
        double dE;
        double lab_ref_L = 0;
        double[] lab_pre = new double[3];
        int c;

        if (btn1.getBackground() instanceof ColorDrawable) {
            int colorCode = ((ColorDrawable) btn1.getBackground()).getColor();
        /*    for (int j : array1) {
                array1.get(array1.indexOf(j));
                Log.e("color ", " " + j);
            }*/

            // TODO:
//            colorArray1 = cClass.getColorArray(colorCode);
//            c1r = colorArray1.get(0);
//            c1g = colorArray1.get(1);
//            c1b = colorArray1.get(2);

            ColorUtils.colorToLAB(colorCode, lab_pre);
            c = ColorUtils.LABToColor(LAB_L_MAX, lab_pre[1], lab_pre[2]);

            colorArray1 = cClass.getColorArray(c);
            c1r = colorArray1.get(0);
            c1g = colorArray1.get(1);
            c1b = colorArray1.get(2);

            ColorUtils.RGBToLAB(c1r, c1g, c1b, lab_ref);
            lab_ref_L = lab_ref_normal[0] = lab_ref[0];
            lab_ref[0] = LAB_L_MAX;
            lab_ref[1] = lab_ref_normal[1] = lab_ref[1];
            lab_ref[2] = lab_ref_normal[2] = lab_ref[2];

            List<Double> reference = new ArrayList(Arrays.asList((double)c1r,(double) c1g, (double)c1b, lab_ref[0], lab_ref[1], lab_ref[2]));
            SR.sendReferenceList(reference);
            //Log.e("send", " List " + reference);

            filler.setBackgroundColor(Color.rgb(c1r, c1g, c1b));

            gd1.setColors(new int[]
                    {Color.rgb(c1r, c1g, c1b), ((ColorDrawable) btn2.getBackground()).getColor()});
            v_grad1.setImageDrawable(gd1);
        }

        if (btn2.getBackground() instanceof ColorDrawable) {
            int colorCode = ((ColorDrawable) btn2.getBackground()).getColor();
//            colorArray2 = cClass.getColorArray(colorCode);
//            c2r = colorArray2.get(0);
//            c2g = colorArray2.get(1);
//            c2b = colorArray2.get(2);

            ColorUtils.colorToLAB(colorCode, lab_pre);
            c = ColorUtils.LABToColor(LAB_L_MAX, lab_pre[1], lab_pre[2]);

            colorArray2 = cClass.getColorArray(c);
            c2r = colorArray2.get(0);
            c2g = colorArray2.get(1);
            c2b = colorArray2.get(2);
        }

        List<Integer> cArray1 = cClass.getColorArray(((ColorDrawable) btn1.getBackground()).getColor());
        int btn_c1r = cArray1.get(0);
        int btn_c1g = cArray1.get(1);
        int btn_c1b = cArray1.get(2);
        List<Integer> cArray2 = cClass.getColorArray(((ColorDrawable) btn2.getBackground()).getColor());
        int btn_c2r = cArray2.get(0);
        int btn_c2g = cArray2.get(1);
        int btn_c2b = cArray2.get(2);

        List<Integer> mix1;
        mix1 = cClass.mixAlphaCompositingMax(c1r, c1g, c1b,
                c2r, c2g, c2b);
        max_a = mix1.get(3);
        SR.sendReferenceDouble(max_a);
        //Log.e("send", " max_a " + max_a);

        double[] lab_s1 = new double[3];
        ColorUtils.colorToLAB(((ColorDrawable) btn1.getBackground()).getColor(), lab_s1);
        double[] lab_s2 = new double[3];
        ColorUtils.colorToLAB(((ColorDrawable) btn2.getBackground()).getColor(), lab_s2);

        textDebug3.setText("1 r " + c1r + " g " + c1g + " b " + c1b + "\n" +
                "2 r " + c2r + " g " + c2g + " b " + c2b + "\n" +
                "1 l " + Math.round(lab_s1[0]) + " a " + Math.round(lab_s1[1]) + " b " + Math.round(lab_s1[2]) + "\n" +
                "2 l " + Math.round(lab_s2[0]) + " a " + Math.round(lab_s2[1]) + " b " + Math.round(lab_s2[2]));

        List<Double> ca = cClass.calcMinA1(max_a, c1r, c1g, c1b, c2r, c2g, c2b, lab_ref);
        double max_ai = ca.get(0);
        double ri = ca.get(1);
        double gi = ca.get(2);
        double bi = ca.get(3);
        txt1.setText(max_ai + " " + String.format(Locale.US, "%.2f", (double) (max_a / 100.0)));
//        mix1 = cClass.mixAlphaCompositing(dd, btn_c1r, btn_c1g, btn_c1b, btn_c2r, btn_c2g, btn_c2b);
//        r = mix1.get(0);
//        g = mix1.get(1);
//        b = mix1.get(2);
        v1.setBackgroundColor(Color.rgb((int) ri, (int) gi, (int) bi));

        ca = cClass.calcMinA2(max_a, c1r, c1g, c1b, c2r, c2g, c2b, lab_ref);
        max_ai = ca.get(0);
        ri = ca.get(1);
        gi = ca.get(2);
        bi = ca.get(3);
        txt2.setText(max_ai + " " + String.format(Locale.US, "%.2f", (double) (max_a / 100.0)));
        v2.setBackgroundColor(Color.rgb((int) ri, (int) gi, (int) bi));

        ca = cClass.calcMinA3(max_a, c1r, c1g, c1b, c2r, c2g, c2b, lab_ref);
        max_ai = ca.get(0);
        ri = ca.get(1);
        gi = ca.get(2);
        bi = ca.get(3);
        txt3.setText(max_ai + " " + String.format(Locale.US, "%.2f", (double) (max_a / 100.0)));
        v3.setBackgroundColor(Color.rgb((int) ri, (int) gi, (int) bi));

        ca = cClass.calcMinA4(max_a, c1r, c1g, c1b, c2r, c2g, c2b, lab_ref);
        max_ai = ca.get(0);
        ri = ca.get(1);
        gi = ca.get(2);
        bi = ca.get(3);
        txt4.setText(max_ai + " " + String.format(Locale.US, "%.2f", (double) (max_a / 100.0)));
        v4.setBackgroundColor(Color.rgb((int) ri, (int) gi, (int) bi));

        for (int i = 0; i < container1.getChildCount(); i++) {

            double a;
            double dr;
            double dg;
            double db;
            int alpha;
            a = i; // alpha calculation

            if (i == 0) a = i + 0.1f;
            else if (i < 9) a = i;
            else a = i - 0.1f;

            int b1 = ((ColorDrawable) btn1.getBackground()).getColor();
            int b2 = ((ColorDrawable) btn2.getBackground()).getColor();
//inverse
//            if (i == 0) a = container1.getChildCount() - (a + 1.1f);
//            else if (i < 9) a = container1.getChildCount() - (a + 1);
//            else a = container1.getChildCount() - (a + 0.9f);
//            Log.e("a ", " " + a);
            a = a / 10;
            alpha = (int) (a * 255);
            //       Log.e("alpha ", " " + (a));

//blend wiki REVERSED Alpha Compositing  short a mix=1  rd = (array2.get(0) * (a + 1 * (1 - a)) - array1.get(0) * 1 * (1 - a)) / a; //original full formula

            mix1Array = cClass.mixAlphaCompositing(a,
                    c1r, c1g, c1b,
                    c2r, c2g, c2b);
            r = mix1Array.get(0);
            g = mix1Array.get(1);
            b = mix1Array.get(2);

            ColorUtils.RGBToLAB(r, g, b, lab_sample);
            lab_sample[0] = LAB_L_MAX;
            lab_sample[1] = lab_sample[1];
            lab_sample[2] = lab_sample[2];
            dE = cClass.calcDE(lab_ref, lab_sample);

//            Log.e("ont[0] ", " DE " + dE + " " + c1r + " " + c1g + " " + c1b + " " + c2r + " " + c2g + " " + c2b);
//            if (i == 0) Log.e("container1 ", " " + r + " " + g + " " + b);
//            if (i == 9) Log.e("container9 ", " " + r + " " + g + " " + b);
            if (r > 255 || r < 0 || g > 255 || g < 0 || b > 255 || b < 0 || dE > DE_MAX) {
                //Log.e("core", "i " +i);
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

            ColorUtils.RGBToLAB(r, g, b, lab_sample);
            lab_sample[0] = LAB_L_MAX;
            lab_sample[1] = lab_sample[1];
            lab_sample[2] = lab_sample[2];
            dE = cClass.calcDE(lab_ref, lab_sample);
            //  Log.e("clr ", Math.round(r) +" "+  Math.round(g)+" "+Math.round(b));
            // Log.e("255alpha ", " " + g);
            if (r > 255 || r < 0 || g > 255 || g < 0 || b > 255 || b < 0 || dE > DE_MAX) {
                if (container2.getChildAt(i) instanceof ImageView) {
                    (container2.getChildAt(i)).setBackgroundColor(0);
                }
            } else if (container2.getChildAt(i) instanceof ImageView) {
                (container2.getChildAt(i)).setBackgroundColor(Color.argb(255, r, g, b));
            }

//blend LAB_L_MAX
            mix3Array = cClass.mixLAB(a,
                    c1r, c1g, c1b,
                    c2r, c2g, c2b);
            r = mix3Array.get(0);
            g = mix3Array.get(1);
            b = mix3Array.get(2);

            ColorUtils.RGBToLAB(r, g, b, lab_sample);
            lab_sample[0] = LAB_L_MAX;
            lab_sample[1] = lab_sample[1];
            lab_sample[2] = lab_sample[2];
            dE = cClass.calcDE(lab_ref, lab_sample);

            if (r > 255 || r < 0 || g > 255 || g < 0 || b > 255 || b < 0 || dE > DE_MAX) {
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

            ColorUtils.RGBToLAB(r, g, b, lab_sample);
            lab_sample[0] = LAB_L_MAX;
            lab_sample[1] = lab_sample[1];
            lab_sample[2] = lab_sample[2];
            dE = cClass.calcDE(lab_ref, lab_sample);
            // Log.e("255alpha ", " " + g);
            if (r > 255 || r < 0 || g > 255 || g < 0 || b > 255 || b < 0 || dE > DE_MAX) {
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