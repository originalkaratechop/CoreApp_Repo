package com.example.android.coreapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

public class FragmentHistogram extends Fragment {

    Bitmap coreBitmap = null;
    Bitmap bMap1 = null;
    Bitmap bMap2 = null;

    boolean isColored, isCut,
            isBuggedL, isBuggedA, isBuggedB, isBuggedDE;

    LinearLayout view_hist_variables, view_hist_comp, view_hist_var_updated, view_hist_log, view_root_hist,
            lo_switchers, lo_histComp, view_root_redraw, view_redraw_filt1, view_redraw_filt2;
    TextView lo_histSeparator;
    ImageView view_core_img, view_hist_img;
    FloatingActionButton view_fab;
    ImageView btn_hide, btnRedrawOn, btnRedrawOff;
    Switch switchComp, switchCut, switchUpd, switchRedraw;
    boolean isHidden;
    RangeSeekBar view_seekL, view_seekA, view_seekB, view_seekDE;
    TextView view_seek_txtL, view_seek_txtA, view_seek_txtB, view_seek_txtDE;
    private ProgressBar spinner;
    TextView txt_load;

    boolean isNotNew;

    private int seek_minL, seek_maxL, seek_minA, seek_maxA, seek_minB, seek_maxB, seek_minDE, seek_maxDE;

    private int COLOR_MAX = 256;
    private int WIDTH_XML = 257;
    private int LIGHT_MAX = 100; //L [0, 100]
    private int A_MAX = 186; //A [-86.185, 98.254]
    private int B_MAX = 203; //B [-107.863, 94.482]
    private int DE_MAX = 120;
    private int FILL_MAX = 2;

    private int NUMBER_OF_COMPS = 3;

    private int NUMBER_OF_VARIABLES = 8;
    public final int RED = 0;
    public final int HSL = 1;
    public final int LAB_L = 2;
    public final int LAB_A1 = 3;
    public final int LAB_B2 = 4;
    public final int DE = 5;
    public final int DIFF_FILL = 6;
    public final int DE_FILL = 7;

    public double[][] compareBins, logBins, updatedBins; // all were int[][]
    public double[] maxHist, maxHistUpd;
    public int[] filterArray1, filterArray2;
    private volatile boolean isLoaded = false;

    float offset = 1;

    Activity a;

    public FragmentHistogram() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_frag_histogram, container, false);
        //     setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_hist_variables = view.findViewById(R.id.hist_variables);
        view_hist_comp = view.findViewById(R.id.hist_comp);
        view_hist_var_updated = view.findViewById(R.id.hist_var_updated);
        view_hist_log = view.findViewById(R.id.hist_log);
        view_root_hist = view.findViewById(R.id.view_root_hist);
        lo_histComp = view.findViewById(R.id.lo_hist_comp);
        lo_histSeparator = view.findViewById(R.id.lo_separator);

        view_core_img = getActivity().findViewById(R.id.view_core);
        view_hist_img = view.findViewById(R.id.hist_img);

        view_root_redraw = view.findViewById(R.id.view_root_redraw);
        view_redraw_filt1 = view.findViewById(R.id.hist_redraw_filt1);
        view_redraw_filt2 = view.findViewById(R.id.hist_redraw_filt2);

        view_fab = view.findViewById(R.id.hist_fab);

        view_seekL = view.findViewById(R.id.seekBarL);
        view_seekA = view.findViewById(R.id.seekBarA);
        view_seekB = view.findViewById(R.id.seekBarB);
        view_seekDE = view.findViewById(R.id.seekBarDE);
        view_seek_txtL = view.findViewById(R.id.seekTxtL);
        view_seek_txtA = view.findViewById(R.id.seekTxtA);
        view_seek_txtB = view.findViewById(R.id.seekTxtB);
        view_seek_txtDE = view.findViewById(R.id.seekTxtDE);
        resetHist();

        view_seekL.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser) {
                if (!isBuggedL) {
                    seek_minL = (int) min;
                    seek_maxL = (int) max;
                }
                view_seek_txtL.setText((int) min + "/" + (int) max);
                isBuggedL = false;
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                isBuggedL = false;
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                view_fab.setVisibility(View.VISIBLE);
            }
        });

        view_seekA.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser) {
                if (!isBuggedA) {
                    seek_minA = (int) min;
                    seek_maxA = (int) max;
                }
                view_seek_txtA.setText((int) min + "/" + (int) max);
                isBuggedA = false;
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                isBuggedA = false;
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                view_fab.setVisibility(View.VISIBLE);
            }
        });

        view_seekB.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser) {
                int mod_minB = (int) (min - 103);
                int mod_maxB = (int) (max - 103);

                if (!isBuggedB) {
                    seek_minB = (int) min;
                    seek_maxB = (int) max;
                }
                if (!isCut)
                    view_seek_txtB.setText((int) min + "/" + (int) max);
                else
                    view_seek_txtB.setText(mod_minB + "/" + mod_maxB);
                isBuggedB = false;
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                isBuggedB = false;
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                view_fab.setVisibility(View.VISIBLE);
            }
        });

        view_seekDE.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser) {
                if (!isBuggedDE) {
                    seek_minDE = (int) min;
                    seek_maxDE = (int) max;
                }
                view_seek_txtDE.setText((int) min + "/" + (int) max);
                isBuggedDE = false;
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                isBuggedDE = false;
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                view_fab.setVisibility(View.VISIBLE);
            }
        });

        view_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                starter(view_core_img);
            }
        });

        switchComp = view.findViewById(R.id.switchComp);
        switchCut = view.findViewById(R.id.switchCut);
        switchUpd = view.findViewById(R.id.switchUpd);
        btnRedrawOn = view.findViewById(R.id.btn_Redraw_on);
        btnRedrawOff = view.findViewById(R.id.btn_Redraw_off);
        lo_switchers = view.findViewById(R.id.switchers_container);

        switchComp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchComp.isChecked()) {
                    lo_histComp.setVisibility(View.VISIBLE);
                    lo_histSeparator.setVisibility(View.GONE);
                } else {
                    lo_histComp.setVisibility(View.GONE);
                    lo_histSeparator.setVisibility(View.VISIBLE);
                }
            }
        });

        switchCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCut = !isCut;
                view_fab.setVisibility(View.GONE);
                isBuggedL = isBuggedA = isBuggedB = true;
                setSeekMinMax();
            }
        });

        switchUpd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchUpd.isChecked()) {
                    view_hist_var_updated.setVisibility(View.VISIBLE);
                    view_hist_variables.setVisibility(View.GONE);
                } else {
                    view_hist_var_updated.setVisibility(View.GONE);
                    view_hist_variables.setVisibility(View.VISIBLE);
                }
            }
        });

        btnRedrawOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view_root_redraw.setVisibility(View.VISIBLE);
            }
        });

        btnRedrawOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view_root_redraw.setVisibility(View.GONE);
            }
        });

        view_root_hist.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (v == null) return false;
                //get rid of swipe
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
        });

        btn_hide = view.findViewById(R.id.btn_hide);
        btn_hide.setImageDrawable(ContextCompat.getDrawable(getActivity(), (R.drawable.ic_keyboard_arrow_up)));
        isHidden = false;
        btn_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isHidden) {
                    btn_hide.setImageDrawable(ContextCompat.getDrawable(getActivity(), (R.drawable.ic_keyboard_arrow_down)));
                    isHidden = true;
                    view_root_hist.setVisibility(View.VISIBLE);
                    lo_switchers.setVisibility(View.VISIBLE);
                } else {
                    btn_hide.setImageDrawable(ContextCompat.getDrawable(getActivity(), (R.drawable.ic_keyboard_arrow_up)));
                    isHidden = false;
                    view_root_hist.setVisibility(View.GONE);
                    lo_switchers.setVisibility(View.GONE);
                }
            }
        });

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (metrics.densityDpi == metrics.DENSITY_LOW)
            offset = 0.75f;
        else if (metrics.densityDpi == metrics.DENSITY_MEDIUM)
            offset = 1f;
        else if (metrics.densityDpi == metrics.DENSITY_TV)
            offset = 1.33f;
        else if (metrics.densityDpi == metrics.DENSITY_HIGH)
            offset = 1.5f;
        else if (metrics.densityDpi == metrics.DENSITY_XHIGH)
            offset = 2f;

        isLoaded = false;
        isNotNew = false;
        isColored = true;
        isCut = false;
        isBuggedL = isBuggedA = isBuggedB = isBuggedDE = true;
        starter(view_core_img);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }

    public void starter(ImageView v) {
        Drawable d = v.getDrawable();
        view_core_img = getActivity().findViewById(R.id.view_core);
        coreBitmap = ((BitmapDrawable) d).getBitmap();
        if (coreBitmap != null) {
            try {
                new AsyncCoreLoad().execute();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    class AsyncCoreLoad extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinner = getActivity().findViewById(R.id.progressBar);
            spinner.setVisibility(View.VISIBLE);
            txt_load = getActivity().findViewById(R.id.progressTxt);
            txt_load.setVisibility(View.VISIBLE);
            view_root_redraw.setVisibility(View.GONE);

            view_root_hist.setVisibility(View.GONE);
            view_fab.setVisibility(View.GONE);
            btn_hide.setVisibility(View.GONE);
            lo_switchers.setVisibility(View.GONE);
            if (isNotNew) {
                view_hist_variables.removeAllViews();
                view_hist_var_updated.removeAllViews();
                view_hist_comp.removeAllViews();
                view_hist_log.removeAllViews();
                view_redraw_filt1.removeAllViews();
                view_redraw_filt2.removeAllViews();

                view_root_hist.setVisibility(View.GONE);
                view_hist_log.setVisibility(View.GONE);
            }
            isNotNew = true;
        }

        @Override
        protected Object doInBackground(Object... params) {
            load(coreBitmap);
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            spinner.setVisibility(View.GONE);
            txt_load.setVisibility(View.GONE);

            view_root_hist.setVisibility(View.VISIBLE);
            view_hist_log.setVisibility(View.VISIBLE);

            //view_redraw_img.setImageBitmap(bMap1);
            btn_hide.setVisibility(View.VISIBLE);
            btn_hide.setImageDrawable(ContextCompat.getDrawable(getActivity(), (R.drawable.ic_keyboard_arrow_down)));
            isHidden = false;
            lo_switchers.setVisibility(View.VISIBLE);
            if (coreBitmap != null) {
                view_hist_variables.addView(new VariablesHistogram(getActivity(), coreBitmap));
                view_hist_var_updated.addView(new UpdatedHistogram(getActivity(), coreBitmap));
                view_hist_comp.addView(new CompareHistogram(getActivity(), coreBitmap));
                view_hist_log.addView(new LightnessLog(getActivity(), coreBitmap));
                view_redraw_filt1.addView(new LogFileterd1(getActivity(), coreBitmap));
                view_redraw_filt2.addView(new LogFileterd2(getActivity(), coreBitmap));
            }
        }
    }

    public void load(Bitmap bitmap) {
        compareBins = new double[NUMBER_OF_VARIABLES][];
        logBins = new double[NUMBER_OF_VARIABLES][];
        updatedBins = new double[NUMBER_OF_VARIABLES][];

        Button bt = getActivity().findViewById(R.id.view_btn1);
        int clr = ((ColorDrawable) bt.getBackground()).getColor();
        double[] lab_r = new double[3]; // _r = reference
        ColorUtils.RGBToLAB(Color.red(clr), Color.green(clr), Color.blue(clr), lab_r);
        //double c_R = Math.sqrt(Math.pow(lab_r[1], 2) + Math.pow(lab_r[2], 2)); //chroma
        //double h_R = Math.atan(lab_r[2] / lab_r[1]); //hue


        for (int i = 0; i < NUMBER_OF_VARIABLES; i++) {
            switch (i) {
                case RED:
                    compareBins[i] = new double[COLOR_MAX];
                    updatedBins[i] = new double[COLOR_MAX];
                    break;
                case HSL:
                case LAB_L:
                case LAB_A1:
                    compareBins[i] = new double[A_MAX];
                    updatedBins[i] = new double[A_MAX];
                    break;
                case LAB_B2:
                    compareBins[i] = new double[B_MAX];
                    updatedBins[i] = new double[B_MAX];
                    break;
                case DE:
                    compareBins[i] = new double[DE_MAX];
                    updatedBins[i] = new double[DE_MAX];
                    break;
                case DIFF_FILL:
                case DE_FILL:
                    compareBins[i] = new double[FILL_MAX];
                    updatedBins[i] = new double[FILL_MAX];
                    break;
            }
            logBins[i] = new double[bitmap.getHeight()];
            //(x*iWidth)+y iWidth bitmap.getWidth();
        }

        if (bitmap != null) {
            // Reset all the bins
            for (int i = 0; i < NUMBER_OF_VARIABLES; i++) {
                for (int y = 0; y < bitmap.getHeight(); y++) {
                    logBins[i][y] = 0;
                }
            }

            for (int i = 0; i < NUMBER_OF_VARIABLES; i++) {
                switch (i) {
                    case RED:
                        for (int x = 0; x < COLOR_MAX; x++) {
                            compareBins[i][x] = 0;
                            updatedBins[i][x] = 0;
                        }
                        break;
                    case LAB_L:
                    case HSL:
                        for (int x = 0; x < LIGHT_MAX; x++) {
                            compareBins[i][x] = 0;
                            updatedBins[i][x] = 0;
                        }
                        break;
                    case LAB_A1:
                        for (int x = 0; x < A_MAX; x++) {
                            compareBins[i][x] = 0;
                            updatedBins[i][x] = 0;
                        }
                        break;
                    case LAB_B2:
                        for (int x = 0; x < B_MAX; x++) {
                            compareBins[i][x] = 0;
                            updatedBins[i][x] = 0;
                        }
                        break;
                    case DE:
                        for (int x = 0; x < DE_MAX; x++) {
                            compareBins[i][x] = 0;
                            updatedBins[i][x] = 0;
                        }
                        break;
                    case DIFF_FILL:
                    case DE_FILL:
                        for (int x = 0; x < FILL_MAX; x++) {
                            compareBins[i][x] = 0;
                            updatedBins[i][x] = 0;
                        }
                        break;
                }
            }

            // bitmap.getPixels(filterArray1, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            filterArray1 = new int[bitmap.getHeight() * bitmap.getWidth()];
            filterArray2 = new int[bitmap.getHeight() * bitmap.getWidth()];
            for (int y = 0; y < bitmap.getHeight(); y++) {

                double px_totalL, px_totalA, px_totalB, px_total_hsl, px_total_red, px_total3, px_totalDE;
                double sumL, sum1A, sumB, sum_hsl, sum_red, sumDE, sum3, sumD;
                sumDE = sumL = sum1A = sumB = sum_hsl = sum_red = sum3 = sumD = 0;
                double avg_labL, avg_labA, avg_labB, avg_hsl, avg_di_fill, avg_red, avg_de_fill, avg_labD;
                double w = bitmap.getWidth();

                for (int x = 0; x < bitmap.getWidth(); x++) {
                    int pixel = bitmap.getPixel(x, y);
                    compareBins[RED][Color.red(pixel)]++;

                    double[] lab = new double[3];
                    ColorUtils.RGBToLAB(Color.red(pixel), Color.green(pixel), Color.blue(pixel), lab);
                    float[] hsl = new float[3];
                    ColorUtils.RGBToHSL(Color.red(pixel), Color.green(pixel), Color.blue(pixel), hsl);

// Deprecated linear method to prove it  wrong
//                    double sr = Color.red(pixel) / 255.0;
//                    sr = sr < 0.04045 ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
//                    double sg = Color.green(pixel) / 255.0;
//                    sg = sg < 0.04045 ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
//                    double sb = Color.blue(pixel) / 255.0;
//                    sb = sb < 0.04045 ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);
//
//                    int olde_lin = (int) Math.round(100 * ((sr * 0.299) + (sg * 0.587) + (sb * 0.114)));
//                    int olde = (int) Math.round(((Color.red(pixel) * 299) + (Color.green(pixel) * 587) + (Color.blue(pixel) * 114)) / 1000 / 256.0 * 100);
//                    cutoffBins[OLDE][olde]++;
//                    //    double olde = 100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722);
////                    Log.e("xx", Color.red(pixel) + " " + Color.green(pixel) + " " + Color.blue(pixel) + " "
////                            + (100 * ((sr * 0.299) + (sg * 0.587) + (sb * 0.114))) + " lab " + olde_lin + " " + Math.round(olde));

                    compareBins[LAB_L][(int) Math.round(lab[0])]++;
                    compareBins[HSL][Math.round(hsl[2] * 100)]++;

                    double labA1 = (lab[1] + 87);
                    double labB2 = (lab[2] + 108);
                    //int lab1 = (int) Math.round((lab[1] + 110) / 2.1);
                    compareBins[LAB_A1][(int) Math.round(labA1)]++;
                    compareBins[LAB_B2][(int) Math.round(labB2)]++;

                    double dE = calcDE(lab_r, lab);
                    sumDE = sumDE + dE;
                    //logBins[DE][y] = sumDE / w;
                    compareBins[DE][(int) dE]++;

                    updatedBins[DE][(int) Math.round((dE >= seek_minDE && dE <= seek_maxDE) ? calcDE(lab_r, lab) : 0)]++;
                    updatedBins[LAB_L][(int) Math.round((lab[0] >= seek_minL && lab[0] <= seek_maxL) ? lab[0] : 0)]++;
                    updatedBins[LAB_A1][(int) Math.round((labA1 >= seek_minA && labA1 <= seek_maxA) ? labA1 : 0)]++;
                    updatedBins[LAB_B2][(int) Math.round((labB2 >= seek_minB && labB2 <= seek_maxB) ? labB2 : 0)]++;

//                    cutoffBins[HSL][olde]++;
//                    if (x > 1010)
//                    Log.e("xx", " x " + x + " y " + y
//                            + " // lab " + Math.round(lab[0]) + " hsl " + Math.round(hsl[2] * 100) + " olde_lin " + olde_lin + " olde " + olde
//                            + " // red " + Color.red(pixel) + " green " + Color.green(pixel) + " blue " + Color.blue(pixel));

                    //int olde = (int) Math.round(((Color.red(pixel) * 299) + (Color.green(pixel) * 587) + (Color.blue(pixel) * 114)) / 1000 / 256.0 * 100);
                    //double l1 = (lab[1] + 110) / 2.1; // before

                    //    if (lab[0] < view_seek.getProgress())
                    ///px_total1 = lab[0] / w;

                    px_totalL = (lab[0] >= seek_minL && lab[0] <= seek_maxL) ? lab[0] / w : 0;
                    sumL = sumL + px_totalL;
                    avg_labL = sumL;

                    //px_total11 = l1 / w;
                    px_totalA = (labA1 >= seek_minA && labA1 <= seek_maxA) ? labA1 / w : 0;
                    sum1A = sum1A + px_totalA;
                    avg_labA = sum1A;

                    //px_total12 = l2 / w;
                    px_totalB = (labB2 >= seek_minB && labB2 <= seek_maxB) ? labB2 / w : 0;
                    sumB = sumB + px_totalB;
                    avg_labB = sumB;

                    int color_filter = ((lab[0] >= seek_minL && lab[0] <= seek_maxL) &&
                            (labA1 >= seek_minA && labA1 <= seek_maxA) &&
                            (labB2 >= seek_minB && labB2 <= seek_maxB)) ? Color.parseColor("#FFFFFF") : Color.parseColor("#000000");

                    //int color_filter = (avg_di_fill > 0) ? Color.parseColor("#FFFFFF") : Color.parseColor("#000000");
                    int index = y * bitmap.getWidth() + x;
                    filterArray1[index] = ((lab[0] >= seek_minL && lab[0] <= seek_maxL) &&
                            (labA1 >= seek_minA && labA1 <= seek_maxA) &&
                            (labB2 >= seek_minB && labB2 <= seek_maxB)) ?
                            ContextCompat.getColor(getActivity(), R.color.filteredLight) : ContextCompat.getColor(getActivity(), R.color.filteredDark);


//TEMPORARY CUT ==============================================================================================
                    //if (lab2 < 0)
                    //Log.e("xx", " lab[1] " + sum11 + " y " + sum12);
                    px_total_hsl = hsl[2] * 100 / w;
                    sum_hsl = sum_hsl + px_total_hsl;
                    avg_hsl = sum_hsl;

                    px_total_red = Color.red(pixel) / w;
                    sum_red = sum_red + px_total_red;
                    avg_red = sum_red;

//                    double c = Math.sqrt(Math.pow(lab[1], 2) + Math.pow(lab[2], 2)); //chroma
//                    double h = Math.atan(lab[2] / lab[1]); //hue
//                    double e94 = Math.sqrt(Math.pow((lab[0] - lab_r[0]) / 1, 2)
//                            + Math.pow((c - c_R) / (1 + 0.045 * c_R), 2)
//                            + Math.pow((h - h_R) / (1 + 0.015 * c_R), 2));  //empfindung
//                    double e2k = 0;
//
//                    int sums = 0;
//                    avg_hsl = sums + e94;

//unused - to be deleted(!)
//                    px_total3 = olde / w;
//                    sum3 = sum3 + px_total3;
//                    avg_olde = sum3;

                    // if (avg_lab > 0 && avg_lab1 > 0 && avg_lab2 > 0) avg_olde = 1;
                    // else avg_olde = 0;
                    //sum3 = sum3 + px_total3;
                    //avg_olde = sum3;
                    avg_di_fill = (avg_labL > 0 && avg_labA > 0 && avg_labB > 0) ? 1 : 0;

                    // avg_de_fill = (sumDE / w > 0) ? 1 : 0;

                    px_totalDE = (dE >= seek_minDE && dE <= seek_maxDE) ? dE / w : 0;
                    sumD = sumD + px_totalDE;
                    avg_labD = sumD;

                    filterArray2[index] = (dE >= seek_minDE && dE <= seek_maxDE) ?
                            ContextCompat.getColor(getActivity(), R.color.filteredLight) : ContextCompat.getColor(getActivity(), R.color.filteredDark);

                    if ((x < 204) && (y > 180) && (y < 200)) Log.e("xx" + x + " y " + y, ""
                            + " minA " + seek_minA + " <A1 " + Math.round(labA1) + "< maxA " + seek_maxA
                            + " minB " + seek_minB + " <B2 " + Math.round(labB2) + "< maxB " + seek_maxB
                            + " color " + color_filter + " fill " + avg_di_fill);

                    avg_de_fill = (avg_labD > 0) ? 1 : 0;

                    // double DIFF_FILL = calcDE(lab_r, lab);

//                    ((lab[0] > seek_minL && lab[0] < seek_maxL) &&
//                    (labA1 > seek_minA && labA1 < seek_maxA) &&
//                    (labB2 > seek_minB && labB2 < seek_maxB) )
//                    double sumDE = 0;
//                    double DIFF_FILL = (avg_olde > 0 ) ? calcDE(lab_r, lab) : 0;
//                    sumDE = sumDE + DIFF_FILL;

                    //Log.e("sum2", " lab[1] " + DIFF_FILL);

                    // int avg = (int) Math.round(lab[0]);
                    // Log.e("avg", bitmap.getWidth() + " px_total " + px_total + " sum " + sum + " avg " + avg);
                    logBins[RED][y] = avg_red;
                    logBins[HSL][y] = avg_hsl;
                    logBins[LAB_L][y] = avg_labL;
                    logBins[LAB_A1][y] = avg_labA;
                    logBins[LAB_B2][y] = avg_labB;
                    logBins[DIFF_FILL][y] = avg_di_fill;
                    logBins[DE_FILL][y] = avg_de_fill;
                    logBins[DE][y] = avg_labD;
                    //   if (x < 2 && y < 10)                        Log.e("xx", " avg_di_fill " + avg_di_fill + " avg_de_fill " + avg_de_fill + " logBins " + logBins[DE_FILL][y]);
                }
                //    if (y < 10)                    Log.e("xx y", " logBins " + logBins[DE_FILL][y]);
            }

            bMap1 = Bitmap.createBitmap(filterArray1, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);//Initialize the bitmap, with the replaced color
            bMap2 = Bitmap.createBitmap(filterArray2, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            // Log.e("getWidth", bitmap.getWidth() + " length " + compareBins[C_RED].length);
            //      Log.e("cutoffBins", bitmap.getWidth() + " length " + cutoffBins[LAB_L].length + cutoffBins[HSL].length + cutoffBins[OLDE].length);

            maxHist = new double[NUMBER_OF_VARIABLES];
            maxHistUpd = new double[NUMBER_OF_VARIABLES];
            for (int i = 0; i < NUMBER_OF_VARIABLES; i++) {
                double mHist = 0;
                double mHistUpd = 0;
                switch (i) {
                    case RED:
                        for (int j = 0; j < COLOR_MAX; j++) {
                            if (mHist < compareBins[i][j]) {
                                mHist = compareBins[i][j];
                            }
                            if (mHistUpd < updatedBins[i][j]) {
                                mHistUpd = updatedBins[i][j];
                            }
                        }
                        break;
                    case HSL:
                    case LAB_L:
                        for (int j = 0; j < LIGHT_MAX; j++) {
                            if (mHist < compareBins[i][j]) {
                                mHist = compareBins[i][j];
                            }
                            if (mHistUpd < updatedBins[i][j]) {
                                mHistUpd = updatedBins[i][j];
                            }
                        }
                        break;
                    case LAB_A1:
                        for (int j = 0; j < A_MAX; j++) {
                            if (mHist < compareBins[i][j]) {
                                mHist = compareBins[i][j];
                            }
                            if (mHistUpd < updatedBins[i][j]) {
                                mHistUpd = updatedBins[i][j];
                            }
                        }
                        break;
                    case LAB_B2:
                        for (int j = 0; j < B_MAX; j++) {
                            if (mHist < compareBins[i][j]) {
                                mHist = compareBins[i][j];
                            }
                            if (mHistUpd < updatedBins[i][j]) {
                                mHistUpd = updatedBins[i][j];
                            }
                        }
                        break;
                    case DE:
                        for (int j = 0; j < DE_MAX; j++) {
                            if (mHist < compareBins[i][j]) {
                                mHist = compareBins[i][j];
                            }
                            if (mHistUpd < updatedBins[i][j]) {
                                mHistUpd = updatedBins[i][j];
                            }
                        }
                        break;
                    case DIFF_FILL:
                    case DE_FILL:
                        for (int j = 0; j < FILL_MAX; j++) {
                            if (mHist < compareBins[i][j]) {
                                mHist = compareBins[i][j];
                            }
                            if (mHistUpd < updatedBins[i][j]) {
                                mHistUpd = updatedBins[i][j];
                            }
                        }
                        break;
                }
                maxHist[i] = mHist;
                maxHistUpd[i] = mHistUpd;
            }
            isLoaded = true;
        } else {
            isLoaded = false;
        }
        Log.e("maxHist[i]", maxHist[0] + " " + maxHist[1] + " " + maxHist[2] + " " + maxHist[3] + " " + maxHist[4] + " " + maxHist[5] + " " + maxHist[6] + " " + maxHist[7]);
    }

    class VariablesHistogram extends View {

        public VariablesHistogram(Context context, Bitmap bmp) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (isLoaded) {
                canvas.drawColor(Color.GRAY);

                // size - 1 = colors on xAxis, WIDTH_XML = pixels in xml layout (wrong to use)
                float scaleX = (float) ((double) getWidth() / ((double) LIGHT_MAX));
                float scaleDE = (float) ((double) getWidth() / ((double) DE_MAX));
                float scaleAX;
                float scaleBX;
                if (!isCut) {
                    scaleAX = (float) ((double) getWidth() / ((double) A_MAX));
                    scaleBX = (float) ((double) getWidth() / ((double) B_MAX));
                } else {
                    scaleAX = (float) ((double) getWidth() / ((double) LIGHT_MAX));
                    scaleBX = (float) ((double) getWidth() / ((double) LIGHT_MAX));
                }

                Log.e("Hist onDraw", "H : " + getHeight()
                        + ", W : " + getWidth()
                        + ", xInt : " + scaleX
                        + ", lineTo : " + scaleX * offset);

                for (int i = 0; i < NUMBER_OF_VARIABLES; i++) {
                    Paint wallpaint;

                    wallpaint = new Paint();
                    switch (i) {
                        case HSL:
                            wallpaint.setColor(Color.GREEN);
                            break;
                        case LAB_L:
                            wallpaint.setColor(ContextCompat.getColor(getActivity(), R.color.seekerL));
                            break;
                        case LAB_A1:
                            wallpaint.setColor(Color.BLACK);
                            break;
                        case LAB_B2:
                            wallpaint.setColor(Color.WHITE);
                            break;
                        case DE:
                            wallpaint.setColor(Color.BLUE);
                            break;
                    }

                    wallpaint.setStyle(Paint.Style.STROKE);

                    Path wallpath = new Path();
                    wallpath.reset();
                    wallpath.moveTo(0, getHeight());
                    switch (i) {
                        case LAB_L:
                        case HSL:
                            for (int x = 0; x < LIGHT_MAX; x++) {
                                int valueY = (int) ((compareBins[i][x] / maxHist[i]) * (getHeight()));
                                wallpath.lineTo(x * scaleX * offset, getHeight() - valueY);
                            }
                            break;
                        case LAB_A1:
                            if (!isCut) {
                                for (int x = 0; x < A_MAX; x++) {
                                    int valueY = (int) ((compareBins[i][x] / maxHist[i]) * (getHeight()));
                                    wallpath.lineTo(x * scaleAX * offset, getHeight() - valueY);
                                }
                            } else {
                                for (int x = 0; x < 100; x++) {
                                    int valueY = (int) ((compareBins[i][x] / maxHist[i]) * (getHeight()));
                                    wallpath.lineTo(x * scaleAX * offset, getHeight() - valueY);
                                }
                            }
                            break;
                        case LAB_B2:
                            if (!isCut) {
                                for (int x = 0; x < B_MAX; x++) {
                                    int valueY = (int) ((compareBins[i][x] / maxHist[i]) * (getHeight()));
                                    wallpath.lineTo(x * scaleBX * offset, getHeight() - valueY);
                                }
                            } else {
                                for (int x = 103; x < B_MAX; x++) {
                                    int valueY = (int) ((compareBins[i][x] / maxHist[i]) * (getHeight()));
                                    wallpath.lineTo((x - 103) * scaleBX * offset, getHeight() - valueY);
                                }
                            }
                            break;
                        case DE:
                            for (int x = 0; x < DE_MAX; x++) {
                                int valueY = (int) ((compareBins[i][x] / maxHist[i]) * (getHeight()));
                                wallpath.lineTo(x * scaleDE * offset, getHeight() - valueY);
                            }
                            break;
                    }
                    canvas.drawPath(wallpath, wallpaint);
                }
            }
        }
    }

    class UpdatedHistogram extends View {

        public UpdatedHistogram(Context context, Bitmap bmp) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (isLoaded) {
                canvas.drawColor(Color.GRAY);

                // size - 1 = colors on xAxis, WIDTH_XML = pixels in xml layout (wrong to use)
                float scaleX = (float) ((double) getWidth() / ((double) LIGHT_MAX));
                float scaleDE = (float) ((double) getWidth() / ((double) DE_MAX));
                float scaleAX;
                float scaleBX;
                if (!isCut) {
                    scaleAX = (float) ((double) getWidth() / ((double) A_MAX));
                    scaleBX = (float) ((double) getWidth() / ((double) B_MAX));
                } else {
                    scaleAX = (float) ((double) getWidth() / ((double) LIGHT_MAX));
                    scaleBX = (float) ((double) getWidth() / ((double) LIGHT_MAX));
                }

                Log.e("Hist onDraw", "H : " + getHeight()
                        + ", W : " + getWidth()
                        + ", xInt : " + scaleX
                        + ", lineTo : " + scaleX * offset);

                for (int i = 0; i < NUMBER_OF_VARIABLES; i++) {
                    Paint wallpaint;

                    wallpaint = new Paint();
                    switch (i) {
                        case HSL:
                            wallpaint.setColor(Color.GREEN);
                            break;
                        case LAB_L:
                            wallpaint.setColor(Color.RED);
                            break;
                        case LAB_A1:
                            wallpaint.setColor(Color.BLACK);
                            break;
                        case LAB_B2:
                            wallpaint.setColor(Color.WHITE);
                            break;
                        case DE:
                            wallpaint.setColor(Color.BLUE);
                            break;
                    }

                    wallpaint.setStyle(Paint.Style.STROKE);

                    Path wallpath = new Path();
                    wallpath.reset();
                    wallpath.moveTo(0, getHeight());
                    switch (i) {
                        case LAB_L:
                            for (int x = 0; x < LIGHT_MAX; x++) {
                                int valueY = (int) ((updatedBins[i][x] / maxHistUpd[i]) * (getHeight()));
                                wallpath.lineTo(x * scaleX * offset, getHeight() - valueY);
                                //if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                            }
                            break;
                        case HSL:
                            for (int x = 0; x < LIGHT_MAX; x++) {
                                int valueY = (int) ((compareBins[i][x] / maxHist[i]) * (getHeight()));
                                wallpath.lineTo(x * scaleX * offset, getHeight() - valueY);
                                //if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                            }
                            break;
                        case LAB_A1:
                            if (!isCut) {
                                for (int x = 0; x < A_MAX; x++) {
                                    int valueY = (int) ((updatedBins[i][x] / maxHistUpd[i]) * (getHeight()));
                                    wallpath.lineTo(x * scaleAX * offset, getHeight() - valueY);
                                    //if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                                }
                            } else {
                                for (int x = 0; x < 100; x++) {
                                    int valueY = (int) ((updatedBins[i][x] / maxHistUpd[i]) * (getHeight()));
                                    wallpath.lineTo(x * scaleAX * offset, getHeight() - valueY);
                                    //if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                                }
                            }
                            break;
                        case LAB_B2:
                            if (!isCut) {
                                for (int x = 0; x < B_MAX; x++) {
                                    int valueY = (int) ((updatedBins[i][x] / maxHistUpd[i]) * (getHeight()));
                                    wallpath.lineTo(x * scaleBX * offset, getHeight() - valueY);
                                    //if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                                }
                            } else {
                                for (int x = 103; x < B_MAX; x++) {
                                    int valueY = (int) ((updatedBins[i][x] / maxHistUpd[i]) * (getHeight()));
                                    wallpath.lineTo((x - 103) * scaleBX * offset, getHeight() - valueY);
                                    //if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                                }
                            }
                            break;
                        case DE:
                            for (int x = 0; x < DE_MAX; x++) {
                                int valueY = (int) ((updatedBins[i][x] / maxHistUpd[i]) * (getHeight()));
                                wallpath.lineTo(x * scaleDE * offset, getHeight() - valueY);
                                //if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                            }
                            break;
                    }
                    canvas.drawPath(wallpath, wallpaint);
                }
            }
        }
    }

    class LightnessLog extends View {

        private Bitmap bi;

        public LightnessLog(Context context, Bitmap bmp) {
            super(context);
            bi = bmp;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (isLoaded) {
                //canvas.drawColor(Color.GRAY);

                canvas.drawBitmap(bMap1, new Rect(0, 0, view_hist_log.getWidth(), view_hist_log.getHeight()), new Rect(0, 0, view_hist_log.getWidth(), view_hist_log.getHeight()), null);

                float scaleY = (float) ((double) getHeight() / ((double) bi.getHeight()));

                for (int i = 0; i < NUMBER_OF_VARIABLES; i++) {
                    Paint wallpaint;

                    wallpaint = new Paint();
                    switch (i) {
                        case RED:
                            wallpaint.setColor(Color.RED);
                            break;
                        case HSL:
                            wallpaint.setColor(Color.GREEN);
                            break;
                        case LAB_L:
                            wallpaint.setColor(Color.YELLOW);
                            break;
                        case LAB_A1:
                            wallpaint.setColor(Color.BLACK);
                            break;
                        case LAB_B2:
                            wallpaint.setColor(Color.WHITE);
                            break;
                        case DE:
                            wallpaint.setColor(Color.BLUE);
                            break;
                        case DIFF_FILL:
                            //wallpaint.setColor(Color.C_LAB);
                            //wallpaint.setColor(Color.parseColor("#3300ff00")); // another
                            wallpaint.setColor(Color.argb(40, 0, 255, 0));
                            break;
                        case DE_FILL:
                            wallpaint.setColor(Color.argb(40, 0, 0, 255));
                            break;
                    }

                    wallpaint.setStyle(Paint.Style.STROKE);

                    Path wallpath = new Path();
                    wallpath.reset();
                    wallpath.moveTo(0, 0);
                    switch (i) {
                        case RED:
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) COLOR_MAX) * (getWidth()));
                                wallpath.lineTo(scaleX, y * scaleY * offset);
                                //Log.e("maxLog"," maxLog[i] " + maxLog[i]);
                            }
                            break;
                        case HSL:
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) LIGHT_MAX) * (getWidth())); //maxLog[i]
                                wallpath.lineTo(scaleX, y * scaleY * offset);
                                //Log.e("maxLog"," maxLog[i] " + maxLog[i]);
                            }
                            break;
                        case LAB_L:
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) LIGHT_MAX) * (getWidth()));
                                wallpath.lineTo(scaleX, y * scaleY * offset);
                            }
                            break;
                        case LAB_A1:
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) A_MAX) * (getWidth()));
                                wallpath.lineTo(scaleX, y * scaleY * offset);
                                if (y == (bi.getHeight() - 1))
                                    wallpath.lineTo(0, y * scaleY * offset);
                            }
                            break;
                        case LAB_B2:
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) B_MAX) * (getWidth()));
                                //Log.e("HISTO", " j " + scaleX + " logBins[i][y] " + logBins[i][y] + " j " + ((double) logBins[i][y] / (double) LIGHT_MAX));
                                wallpath.lineTo(scaleX, y * scaleY * offset);
                                if (y == (bi.getHeight() - 1))
                                    wallpath.lineTo(0, y * scaleY * offset);
                            }
                            break;
                        case DE:
                            wallpath.moveTo(getWidth(), 0);
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) DE_MAX) * (getWidth()));
                                wallpath.lineTo(getWidth() - scaleX, y * scaleY * offset);
                            }
                            break;
                        case DIFF_FILL:
                            wallpaint.setStyle(Paint.Style.FILL);
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) FILL_MAX) * (getWidth()));
                                wallpath.lineTo(scaleX, y * scaleY * offset);
                                if (y == (bi.getHeight() - 1))
                                    wallpath.lineTo(0, y * scaleY * offset);
                            }
                            break;
                        case DE_FILL:
                            wallpath.moveTo(getWidth(), 0);
                            wallpaint.setStyle(Paint.Style.FILL);
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) FILL_MAX) * (getWidth()));
                                wallpath.lineTo(getWidth() - scaleX, y * scaleY * offset);
                                if (y == (bi.getHeight() - 1))
                                    wallpath.lineTo(getWidth(), y * scaleY * offset);
                                // if (logBins[i][y] > 0)                                    Log.e("scaleX", " avg_de_fill " + logBins[i][y]);
                            }
                            break;
                    }
                    canvas.drawPath(wallpath, wallpaint);
                }
            }
        }
    }

    class CompareHistogram extends View {

        public CompareHistogram(Context context, Bitmap bi) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (isLoaded) {
                canvas.drawColor(Color.GRAY);

                // size - 1 = colors on xAxis, WIDTH_XML = pixels in xml layout (wrong to use)
                float scaleRed = (float) ((double) getWidth() / ((double) COLOR_MAX - 1));
                float scaleX = (float) ((double) getWidth() / ((double) LIGHT_MAX));

                for (int i = 0; i < NUMBER_OF_COMPS; i++) {

                    Paint wallpaint;

                    wallpaint = new Paint();
                    if (isColored) {
                        if (i == RED) {
                            wallpaint.setColor(Color.RED);
                        } else if (i == HSL) {
                            wallpaint.setColor(Color.GREEN);
                        } else if (i == LAB_L) {
                            wallpaint.setColor(Color.YELLOW);
                        }
                    } else {
                        wallpaint.setColor(Color.WHITE);
                    }

                    wallpaint.setStyle(Paint.Style.STROKE);

                    Path wallpath = new Path();
                    wallpath.reset();
                    wallpath.moveTo(0, getHeight());
                    switch (i) {
                        case RED:
                            for (int j = 0; j < COLOR_MAX; j++) {
                                int value = (int) ((compareBins[i][j] / maxHist[i]) * (getHeight()));
                                wallpath.lineTo(j * scaleRed * offset, getHeight() - value);
                                //                      if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                            }
                            break;
                        case HSL:
                            for (int x = 0; x < LIGHT_MAX; x++) {
                                int valueY = (int) ((compareBins[i][x] / maxHist[i]) * (getHeight()));
                                wallpath.lineTo(x * scaleX * offset, getHeight() - valueY);
                                //if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                            }
                            break;
                        case LAB_L:
                            for (int x = 0; x < LIGHT_MAX; x++) {
                                int valueY = (int) ((compareBins[i][x] / maxHist[i]) * (getHeight()));
                                wallpath.lineTo(x * scaleX * offset, getHeight() - valueY);
                                //if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                            }
                            break;
                    }
                    canvas.drawPath(wallpath, wallpaint);
                }
            }

        }
    }

    class LogFileterd1 extends View {

        private Bitmap bi;

        public LogFileterd1(Context context, Bitmap bmp) {
            super(context);
            bi = bmp;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (isLoaded) {
                //canvas.drawColor(Color.GRAY);

                canvas.drawBitmap(bMap1, new Rect(0, 0, view_redraw_filt1.getWidth(), view_redraw_filt1.getHeight()), new Rect(0, 0, view_redraw_filt1.getWidth(), view_redraw_filt1.getHeight()), null);

                float scaleY = (float) ((double) getHeight() / ((double) bi.getHeight()));

                for (int i = 0; i < NUMBER_OF_VARIABLES; i++) {
                    Paint wallpaint;

                    wallpaint = new Paint();
                    switch (i) {
                        case LAB_L:
                            wallpaint.setColor(Color.YELLOW);
                            break;
                        case LAB_A1:
                            wallpaint.setColor(Color.BLACK);
                            break;
                        case LAB_B2:
                            wallpaint.setColor(Color.WHITE);
                            break;
                        case DIFF_FILL:
                            //wallpaint.setColor(Color.C_LAB);
                            //wallpaint.setColor(Color.parseColor("#3300ff00")); // another
                            wallpaint.setColor(Color.argb(40, 0, 255, 0));
                            break;
                    }

                    wallpaint.setStyle(Paint.Style.STROKE);

                    Path wallpath = new Path();
                    wallpath.reset();
                    wallpath.moveTo(0, 0);
                    switch (i) {
                        case LAB_L:
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) LIGHT_MAX) * (getWidth()));
                                wallpath.lineTo(scaleX, y * scaleY * offset);
                            }
                            break;
                        case LAB_A1:
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) A_MAX) * (getWidth()));
                                wallpath.lineTo(scaleX, y * scaleY * offset);
                                if (y == (bi.getHeight() - 1))
                                    wallpath.lineTo(0, y * scaleY * offset);
                            }
                            break;
                        case LAB_B2:
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) B_MAX) * (getWidth()));
                                //Log.e("HISTO", " j " + scaleX + " logBins[i][y] " + logBins[i][y] + " j " + ((double) logBins[i][y] / (double) LIGHT_MAX));
                                wallpath.lineTo(scaleX, y * scaleY * offset);
                                if (y == (bi.getHeight() - 1))
                                    wallpath.lineTo(0, y * scaleY * offset);
                            }
                            break;
                        case DIFF_FILL:
                            wallpaint.setStyle(Paint.Style.FILL);
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) (FILL_MAX / 2)) * (getWidth()));
                                wallpath.lineTo(scaleX, y * scaleY * offset);
                                if (y == (bi.getHeight() - 1))
                                    wallpath.lineTo(0, y * scaleY * offset);
                            }
                            break;
                    }
                    canvas.drawPath(wallpath, wallpaint);
                }
            }
        }
    }

    class LogFileterd2 extends View {

        private Bitmap bi;

        public LogFileterd2(Context context, Bitmap bmp) {
            super(context);
            bi = bmp;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (isLoaded) {
                //canvas.drawColor(Color.GRAY);

                canvas.drawBitmap(bMap2, new Rect(0, 0, view_redraw_filt2.getWidth(), view_redraw_filt2.getHeight()), new Rect(0, 0, view_redraw_filt2.getWidth(), view_redraw_filt2.getHeight()), null);

                float scaleY = (float) ((double) getHeight() / ((double) bi.getHeight()));

                for (int i = 0; i < NUMBER_OF_VARIABLES; i++) {
                    Paint wallpaint;

                    wallpaint = new Paint();
                    switch (i) {
                        case DE:
                            wallpaint.setColor(Color.BLUE);
                            break;
                        case DE_FILL:
                            wallpaint.setColor(Color.argb(40, 0, 0, 255));
                            break;
                    }

                    wallpaint.setStyle(Paint.Style.STROKE);

                    Path wallpath = new Path();
                    wallpath.reset();
                    wallpath.moveTo(0, 0);
                    switch (i) {
                        case DE:
                            wallpath.moveTo(getWidth(), 0);
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) DE_MAX) * (getWidth()));
                                wallpath.lineTo(getWidth() - scaleX, y * scaleY * offset);
                            }
                            break;
                        case DE_FILL:
                            wallpath.moveTo(getWidth(), 0);
                            wallpaint.setStyle(Paint.Style.FILL);
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) (FILL_MAX/2)) * (getWidth()));
                                wallpath.lineTo(getWidth() - scaleX, y * scaleY * offset);
                                if (y == (bi.getHeight() - 1))
                                    wallpath.lineTo(getWidth(), y * scaleY * offset);
                            }
                            break;
                    }
                    canvas.drawPath(wallpath, wallpaint);
                }
            }
        }
    }

    public void resetHist() {
        setSeekMinMax();
    }

    public void setSeekMinMax() {
        int minL, maxL, minA, maxA, minB, maxB, minDE, maxDE;
        seek_minL = minL = 0;
        seek_maxL = maxL = 100;
        seek_minDE = minDE = 0;
        seek_maxDE = maxDE = 120;
        if (!isCut) {
            seek_minA = minA = 0;
            seek_maxA = maxA = 186;
            seek_minB = minB = 0;
            seek_maxB = maxB = 203;
        } else {
            seek_minA = minA = 0;
            seek_maxA = maxA = 100;
            seek_minB = minB = 103;
            seek_maxB = maxB = 203;
        }

        // Log.e("setSeekMinMax", " seek_minxL " + seek_minL + " seek_maxL " + seek_maxL);

        view_seekL.setValue(minL, maxL);
        view_seekL.setMinProgress(minL);
        view_seekL.setMaxProgress(maxL);

        view_seekA.setRangeRules(minA, maxA, 0, 1);
        view_seekA.setValue(minA, maxA);
        view_seekA.setMinProgress(minA);
        view_seekA.setMaxProgress(maxA);

        view_seekB.setRangeRules(minB, maxB, 0, 1);
        view_seekB.setValue(minB, maxB);
        view_seekB.setMinProgress(minB);
        view_seekB.setMaxProgress(maxB);

        view_seekDE.setValue(minDE, maxDE);
        view_seekDE.setMinProgress(minDE);
        view_seekDE.setMaxProgress(maxDE);

        int mod_minB = minB - 103;
        int mod_maxB = maxB - 103;
        view_seek_txtL.setText(minL + "/" + maxL);
        view_seek_txtDE.setText(minDE + "/" + maxDE);
        view_seek_txtA.setText(minA + "/" + maxA);
        if (!isCut)
            view_seek_txtB.setText(minB + "/" + maxB);
        else
            view_seek_txtB.setText(mod_minB + "/" + mod_maxB);

        view_hist_variables.removeAllViews();
        view_hist_variables.addView(new VariablesHistogram(getActivity(), coreBitmap));
        view_hist_var_updated.removeAllViews();
        view_hist_var_updated.addView(new UpdatedHistogram(getActivity(), coreBitmap));
        isBuggedL = isBuggedA = isBuggedB = isBuggedDE = true;
    }

    public static double calcDE(double Lab1[], double Lab2[]) {
        double L1 = Lab1[0];
        double a1 = Lab1[1];
        double b1 = Lab1[2];
        double L2 = Lab2[0];
        double a2 = Lab2[1];
        double b2 = Lab2[2];

        double Lmean = (L1 + L2) / 2.0; //ok
        double C1 = Math.sqrt(a1 * a1 + b1 * b1); //ok
        double C2 = Math.sqrt(a2 * a2 + b2 * b2); //ok
        double Cmean = (C1 + C2) / 2.0; //ok

        double G = (1 - Math.sqrt(Math.pow(Cmean, 7) / (Math.pow(Cmean, 7) + Math.pow(25, 7)))) / 2; //ok
        double a1prime = a1 * (1 + G); //ok
        double a2prime = a2 * (1 + G); //ok

        double C1prime = Math.sqrt(a1prime * a1prime + b1 * b1); //ok
        double C2prime = Math.sqrt(a2prime * a2prime + b2 * b2); //ok
        double Cmeanprime = (C1prime + C2prime) / 2; //ok

        double h1prime = Math.atan2(b1, a1prime) + 2 * Math.PI * (Math.atan2(b1, a1prime) < 0 ? 1 : 0);
        double h2prime = Math.atan2(b2, a2prime) + 2 * Math.PI * (Math.atan2(b2, a2prime) < 0 ? 1 : 0);
        double Hmeanprime = ((Math.abs(h1prime - h2prime) > Math.PI) ? (h1prime + h2prime + 2 * Math.PI) / 2 : (h1prime + h2prime) / 2); //ok

        double T = 1.0 - 0.17 * Math.cos(Hmeanprime - Math.PI / 6.0) + 0.24 * Math.cos(2 * Hmeanprime) + 0.32 * Math.cos(3 * Hmeanprime + Math.PI / 30) - 0.2 * Math.cos(4 * Hmeanprime - 21 * Math.PI / 60); //ok

        double deltahprime = ((Math.abs(h1prime - h2prime) <= Math.PI) ? h2prime - h1prime : (h2prime <= h1prime) ? h2prime - h1prime + 2 * Math.PI : h2prime - h1prime - 2 * Math.PI); //ok

        double deltaLprime = L2 - L1; //ok
        double deltaCprime = C2prime - C1prime; //ok
        double deltaHprime = 2.0 * Math.sqrt(C1prime * C2prime) * Math.sin(deltahprime / 2.0); //ok
        double SL = 1.0 + ((0.015 * (Lmean - 50) * (Lmean - 50)) / (Math.sqrt(20 + (Lmean - 50) * (Lmean - 50)))); //ok
        double SC = 1.0 + 0.045 * Cmeanprime; //ok
        double SH = 1.0 + 0.015 * Cmeanprime * T; //ok

        double deltaTheta = (30 * Math.PI / 180) * Math.exp(-((180 / Math.PI * Hmeanprime - 275) / 25) * ((180 / Math.PI * Hmeanprime - 275) / 25));
        double RC = (2 * Math.sqrt(Math.pow(Cmeanprime, 7) / (Math.pow(Cmeanprime, 7) + Math.pow(25, 7))));
        double RT = (-RC * Math.sin(2 * deltaTheta));

        double KL = 1;
        double KC = 1;
        double KH = 1;

        double deltaE = Math.sqrt(
                ((deltaLprime / (KL * SL)) * (deltaLprime / (KL * SL))) +
                        ((deltaCprime / (KC * SC)) * (deltaCprime / (KC * SC))) +
                        ((deltaHprime / (KH * SH)) * (deltaHprime / (KH * SH))) +
                        (RT * (deltaCprime / (KC * SC)) * (deltaHprime / (KH * SH)))
        );

        return deltaE;
    }
}