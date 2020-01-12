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
import android.os.Handler;
import android.os.Looper;
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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import java.util.List;

public class FragmentHistogram extends Fragment {

    ColorManager cClass = new ColorManager();

    Bitmap coreBitmap = null;
    Bitmap bMap1 = null;
    Bitmap bMap2 = null;
    Bitmap bMap3 = null;
    Bitmap bMapA = null;
    Bitmap bMapB = null;

    boolean isColored, isCut, isDE,
            isBuggedL, isBuggedA, isBuggedB, isBuggedDE;

    LinearLayout view_hist_variables, view_hist_comp, view_hist_var_updated, view_hist_log, view_root_hist,
            lo_switchers, lo_histComp, view_root_redraw, view_redraw_filt1, view_redraw_filt2, view_redraw_filt3, lo_butttons;
    TextView lo_histSeparator;
    CheckBox view_check;
    ImageView view_core_img, view_hist_img;
    FloatingActionButton view_fab;
    ImageView btn_hide, btnRedrawOn, btnRedrawOff;
    Switch switchComp, switchCut, switchUpd;
    boolean isHidden;
    RangeSeekBar view_seekL, view_seekA, view_seekB, view_seekDE;
    TextView view_seek_txtL, view_seek_txtA, view_seek_txtB, view_seek_txtDE;
    private ProgressBar spinner;
    TextView txt_load;
    String text;

    boolean isNotNew;

    private int seek_minL, seek_maxL, seek_minA, seek_maxA, seek_minB, seek_maxB, seek_minDE, seek_maxDE;
    private double PackageDouble;
    private List<Double> PackageList;

    private static final int COLOR_MAX = 256;
    private static final int WIDTH_XML = 257;
    private static final int L_MAX = 100; //L [0, 100]
    private static final int A_MAX = 186; //A [-86.185, 98.254]
    private static final int B_MAX = 203; //B [-107.863, 94.482]
    private static final int DE_MAX = 120;
    private static final int FILL_MAX = 2;
    private static final int LAB_L_MAX = 94;
    private static final int A_LIM = 100;
    private static final int B_LIM = 103;
    private static final int DE_LIM = 30;

    private int NUMBER_OF_COMPS = 3;

    private int NUMBER_OF_VARIABLES = 10;
    public final int RED = 0;
    public final int HSL = 1;
    public final int LAB_L = 2;
    public final int LAB_A1 = 3;
    public final int LAB_B2 = 4;
    public final int DE = 5;
    public final int DIFF_FILL = 6;
    public final int DE_FILL = 7;
    public final int TOTAL_FILL = 8;
    public final int NTG = 9;


    public double[][] compareBins, logBins, updatedBins; // all were int[][]
    public double[] maxHist, maxHistUpd;
    public int[] filterArray1, filterArray2, filterArray3, filterArrayL, filterArrayA, filterArrayB;
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
        view_redraw_filt3 = view.findViewById(R.id.hist_redraw_filt3);

        view_fab = view.findViewById(R.id.hist_fab);
        view_check = view.findViewById(R.id.checkDE);

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
                int mod_minB = (int) (min - B_LIM);
                int mod_maxB = (int) (max - B_LIM);

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

        view_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isDE)
                    view_check.setChecked(true);
                else
                    view_check.setChecked(false);
                isDE = !isDE;
            }
        });

        switchComp = view.findViewById(R.id.switchComp);
        switchCut = view.findViewById(R.id.switchCut);
        switchUpd = view.findViewById(R.id.switchUpd);
        btnRedrawOn = view.findViewById(R.id.btn_Redraw_on);
        btnRedrawOn.setImageDrawable(ContextCompat.getDrawable(getActivity(), (R.drawable.ic_equalizer)));
        btnRedrawOff = view.findViewById(R.id.btn_Redraw_off);
        btnRedrawOff.setImageDrawable(ContextCompat.getDrawable(getActivity(), (R.drawable.ic_keyboard_arrow_down)));
        lo_switchers = view.findViewById(R.id.switchers_container);
        lo_butttons = view.findViewById(R.id.buttons_container);

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
                view_fab.setVisibility(View.GONE);
            }
        });

        btnRedrawOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view_root_redraw.setVisibility(View.GONE);
                if (!isBuggedL || !isBuggedA || !isBuggedB || !isBuggedDE)
                    view_fab.setVisibility(View.VISIBLE);
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
        isDE = false;
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
            lo_switchers.setVisibility(View.GONE);
            lo_butttons.setVisibility(View.GONE);
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
            lo_butttons.setVisibility(View.VISIBLE);
            btn_hide.setImageDrawable(ContextCompat.getDrawable(getActivity(), (R.drawable.ic_keyboard_arrow_down)));
            isHidden = false;
            lo_switchers.setVisibility(View.VISIBLE);
            if (coreBitmap != null) {
                view_hist_variables.addView(new VariablesHistogram(getActivity(), coreBitmap));
                view_hist_var_updated.addView(new UpdatedHistogram(getActivity(), coreBitmap));
                view_hist_comp.addView(new CompareHistogram(getActivity(), coreBitmap));
                view_hist_log.addView(new LightnessLog(getActivity(), coreBitmap));
                view_redraw_filt1.addView(new LogFiltered1(getActivity(), coreBitmap));
                view_redraw_filt2.addView(new LogFiltered2(getActivity(), coreBitmap));
                view_redraw_filt3.addView(new LogFiltered3(getActivity(), coreBitmap));
            }
        }
    }

    void UpdateProgress() {
        txt_load = getActivity().findViewById(R.id.progressTxt);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                txt_load.setText(text);
            }
        });
    }

    protected void ReceiveList(List<Double> reference) {
        PackageList = reference;
    }

    protected void ReceiveDouble(double reference) {
        PackageDouble = reference;
    }

    public void load(Bitmap bitmap) {
        compareBins = new double[NUMBER_OF_VARIABLES][];
        logBins = new double[NUMBER_OF_VARIABLES][];
        updatedBins = new double[NUMBER_OF_VARIABLES][];
        int yx = bitmap.getHeight() * bitmap.getWidth();

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
                case NTG:
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
                    case NTG:
                        for (int x = 0; x < L_MAX; x++) {
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
            filterArray1 = new int[yx];
            filterArray2 = new int[yx];
            filterArray3 = new int[yx];
            filterArrayL = new int[yx];
            filterArrayA = new int[yx];
            filterArrayB = new int[yx];
            int count = 0;
            for (int y = 0; y < bitmap.getHeight(); y++) {

                double px_totalL, px_totalA, px_totalB, px_total_hsl, px_total_red, px_total3, px_totalDE, px_total_di, px_total_tot, px_total_ntg;
                double sumL, sum1A, sumB, sum_hsl, sum_red, sumDE, sum3, sumD, sum_di, sum_tot, sum_ntg;
                sumDE = sumL = sum1A = sumB = sum_hsl = sum_red = sum3 = sumD = sum_di = sum_tot = sum_ntg = 0;
                double avg_labL, avg_labA, avg_labB, avg_hsl, avg_di_fill, avg_red, avg_de_fill, avg_labD, avg_total_fill, avg_di, avg_tot, avg_ntg;
                double w = bitmap.getWidth();

                for (int x = 0; x < bitmap.getWidth(); x++) {
                    count++;
                    int perc = count * 100 / yx;
                    text = getString(R.string.view_load, perc);
                    UpdateProgress();

                    int pixel = bitmap.getPixel(x, y);
                    int r = Color.red(pixel);
                    int g = Color.green(pixel);
                    int b = Color.blue(pixel);
                    compareBins[RED][r]++;

                    double[] lab = new double[3];
                    ColorUtils.RGBToLAB(r, g, b, lab);
                    float[] hsl = new float[3];
                    ColorUtils.RGBToHSL(r, g, b, hsl);

// Deprecated linear method to prove it  wrong
//                    double sr = r / 255.0;
//                    sr = sr < 0.04045 ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
//                    double sg = Color.green(pixel) / 255.0;
//                    sg = sg < 0.04045 ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
//                    double sb = Color.blue(pixel) / 255.0;
//                    sb = sb < 0.04045 ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);
//
//                    int olde_lin = (int) Math.round(100 * ((sr * 0.299) + (sg * 0.587) + (sb * 0.114)));
//                    int olde = (int) Math.round(((r * 299) + (Color.green(pixel) * 587) + (Color.blue(pixel) * 114)) / 1000 / 256.0 * 100);
//                    cutoffBins[OLDE][olde]++;
//                    //    double olde = 100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722);
////                    Log.e("xx", r + " " + Color.green(pixel) + " " + Color.blue(pixel) + " "
////                            + (100 * ((sr * 0.299) + (sg * 0.587) + (sb * 0.114))) + " lab " + olde_lin + " " + Math.round(olde));

                    compareBins[LAB_L][(int) Math.round(lab[0])]++;
                    compareBins[HSL][Math.round(hsl[2] * 100)]++;

                    double labA1 = (lab[1] + 87);
                    double labB2 = (lab[2] + 108);
                    //int lab1 = (int) Math.round((lab[1] + 110) / 2.1);
                    compareBins[LAB_A1][(int) Math.round(labA1)]++;
                    compareBins[LAB_B2][(int) Math.round(labB2)]++;

                    double[] lab1_100 = new double[3];
                    lab1_100[0] = LAB_L_MAX;
                    lab1_100[1] = lab_r[1];
                    lab1_100[2] = lab_r[2];
                    double[] lab2_100 = new double[3];
                    lab2_100[0] = LAB_L_MAX;
                    lab2_100[1] = lab[1];
                    lab2_100[2] = lab[2];

                    //double dE = cClass.calcDE(lab_r, lab);
                    double dE = cClass.calcDE(lab1_100, lab2_100);
                    sumDE = sumDE + dE;
                    //logBins[DE][y] = sumDE / w;
                    compareBins[DE][(int) dE]++;

                    updatedBins[DE][(int) Math.round((dE >= seek_minDE && dE <= seek_maxDE) ? cClass.calcDE(lab_r, lab) : 0)]++;
                    updatedBins[LAB_L][(int) Math.round((lab[0] >= seek_minL && lab[0] <= seek_maxL) ? lab[0] : 0)]++;
                    updatedBins[LAB_A1][(int) Math.round((labA1 >= seek_minA && labA1 <= seek_maxA) ? labA1 : 0)]++;
                    updatedBins[LAB_B2][(int) Math.round((labB2 >= seek_minB && labB2 <= seek_maxB) ? labB2 : 0)]++;

//                    cutoffBins[HSL][olde]++;
//                    if (x > 1010)
//                    Log.e("xx", " x " + x + " y " + y
//                            + " // lab " + Math.round(lab[0]) + " hsl " + Math.round(hsl[2] * 100) + " olde_lin " + olde_lin + " olde " + olde
//                            + " // red " + r + " green " + Color.green(pixel) + " blue " + Color.blue(pixel));

                    //int olde = (int) Math.round(((r * 299) + (Color.green(pixel) * 587) + (Color.blue(pixel) * 114)) / 1000 / 256.0 * 100);
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

                    int index = y * bitmap.getWidth() + x;
                    filterArray1[index] = ((lab[0] >= seek_minL && lab[0] <= seek_maxL) &&
                            (labA1 >= seek_minA && labA1 <= seek_maxA) &&
                            (labB2 >= seek_minB && labB2 <= seek_maxB) ) ?
// was inside                          // && (dE >= seek_minDE && dE <= seek_maxDE)
                            ContextCompat.getColor(getActivity(), R.color.filteredLight) : ContextCompat.getColor(getActivity(), R.color.filteredDark);

                    //   filterArrayL[index] = (lab[0] >= seek_minL && lab[0] <= seek_maxL) ?
                    //           ContextCompat.getColor(getActivity(), R.color.filteredR) : ContextCompat.getColor(getActivity(), R.color.filteredBlack);


//TEMPORARY CUT ==============================================================================================
                    //if (lab2 < 0)
                    //Log.e("xx", " lab[1] " + sum11 + " y " + sum12);
                    px_total_hsl = hsl[2] * 100 / w;
                    sum_hsl = sum_hsl + px_total_hsl;
                    avg_hsl = sum_hsl;

                    px_total_red = r / w;
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
//
//                     if (avg_lab > 0 && avg_lab1 > 0 && avg_lab2 > 0) avg_olde = 1;
//                     else avg_olde = 0;
//                    sum3 = sum3 + px_total3;
//                    avg_olde = sum3;

                    px_totalDE = (dE >= seek_minDE && dE <= seek_maxDE) ? dE / w : 0;
                    sumD = sumD + px_totalDE;
                    avg_labD = sumD;
// olde AVERAGE! while current px is required
//                    avg_di_fill = (avg_labL > 0 && avg_labA > 0 && avg_labB > 0) ? 1 : 0;
//                    avg_total_fill = (avg_labL > 0 && avg_labA > 0 && avg_labB > 0 && avg_labD > 0) ? 1 : 0;

                    px_total_di = ((lab[0] >= seek_minL && lab[0] <= seek_maxL) &&
                            (labA1 >= seek_minA && labA1 <= seek_maxA) &&
                            (labB2 >= seek_minB && labB2 <= seek_maxB)) ? labB2 / w : 0;
                    sum_di = sum_di + px_total_di;
                    avg_di = sum_di;
                    avg_di_fill = (avg_di > 0) ? 1 : 0;

                    px_total_tot = ((lab[0] >= seek_minL && lab[0] <= seek_maxL) &&
                            (labA1 >= seek_minA && labA1 <= seek_maxA) &&
                            (labB2 >= seek_minB && labB2 <= seek_maxB) &&
                            (dE >= seek_minDE && dE <= seek_maxDE)) ? labB2 / w : 0;
                    sum_tot = sum_tot + px_total_tot;
                    avg_tot = sum_tot;
                    avg_total_fill = (avg_tot > 0) ? 1 : 0;


                    filterArrayA[index] = (labA1 >= seek_minA && labA1 <= seek_maxA) ?
                            ContextCompat.getColor(getActivity(), R.color.filteredG) : ContextCompat.getColor(getActivity(), R.color.filteredBlack);

                    filterArrayB[index] = (labB2 >= seek_minB && labB2 <= seek_maxB) ?
                            ContextCompat.getColor(getActivity(), R.color.filteredB) : ContextCompat.getColor(getActivity(), R.color.filteredBlack);

                    filterArray2[index] = (dE >= seek_minDE && dE <= seek_maxDE) ?
                            //ContextCompat.getColor(getActivity(), R.color.filteredLight) : ContextCompat.getColor(getActivity(), R.color.filteredDark);
                            ContextCompat.getColor(getActivity(), R.color.filteredLight) : Color.BLACK;


                    if (view_check.isChecked()) {
                        double[] ref = new double[3];
                        double c1r = 0;
                        double c1g = 0;
                        double c1b = 0;
                        if (PackageList == null) {
                            for (int i = 0; i < (ref.length - 1); i++)
                                ref[i] = 0;
                        } else {
                            ref[0] = PackageList.get(3);
                            ref[1] = PackageList.get(4);
                            ref[2] = PackageList.get(5);
                            c1r = PackageList.get(0);
                            c1g = PackageList.get(1);
                            c1b = PackageList.get(2);
                        }

                        int c = ColorUtils.LABToColor(LAB_L_MAX, lab2_100[1], lab2_100[2]);
                        List<Integer> colorArray = cClass.getColorArray(c);
                        int mix_r = colorArray.get(0);
                        int mix_g = colorArray.get(1);
                        int mix_b = colorArray.get(2);

                        List<Double> ca = cClass.calcMinA3(PackageDouble, (int) c1r, (int) c1g, (int) c1b, mix_r, mix_g, mix_b, ref);
                        double dEi = ca.get(0);
                        double delta = lab[0] * dEi / 100 * 255;


                        Log.e("delta", " dEi " + dEi + " delta " + delta + " y " + y + " x " + x);
//                            +" MAX_A " + PackageDouble + " c1r " + c1r+ " c1g " + c1g+ " c1b " + c1b
//                            + " mix_r " + mix_r + " mix_g " + mix_g + " mix_b " + mix_b);

                        px_total_ntg = ((lab[0] >= seek_minL && lab[0] <= seek_maxL) &&
                                (labA1 >= seek_minA && labA1 <= seek_maxA) &&
                                (labB2 >= seek_minB && labB2 <= seek_maxB) &&
                                (dE >= seek_minDE && dE <= seek_maxDE)) ? (lab[0] * dEi) / w : 0;
                        sum_ntg = sum_ntg + px_total_ntg;
                        avg_ntg = sum_ntg;

                        logBins[NTG][y] = avg_ntg;

                        filterArray3[index] = ((lab[0] >= seek_minL && lab[0] <= seek_maxL) &&
                                (labA1 >= seek_minA && labA1 <= seek_maxA) &&
                                (labB2 >= seek_minB && labB2 <= seek_maxB) &&
                                (dE >= seek_minDE && dE <= seek_maxDE)) ?
                                // ColorUtils.setAlphaComponent(Color.WHITE, (int) delta) : ContextCompat.getColor(getActivity(), R.color.filteredDark);
                                ColorUtils.setAlphaComponent(Color.WHITE, (int) delta) : Color.BLACK;

                        bMap3 = Bitmap.createBitmap(filterArray3, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    }


//                    if ((x < 204) && (y > 180) && (y < 200)) Log.e("xx" + x + " y " + y, ""
//                            + " minA " + seek_minA + " <A1 " + Math.round(labA1) + "< maxA " + seek_maxA
//                            + " minB " + seek_minB + " <B2 " + Math.round(labB2) + "< maxB " + seek_maxB
//                            + " color " + color_filter + " fill " + avg_di_fill);


//                    if ((x < 204) && (y > 180) && (y < 200)) Log.e("xx" + x + " y " + y, ""
//                            + " avg_di_fill " + avg_di_fill
//                            + "!!! avg_labL " + avg_labL + " avg_labA " + avg_labA + " avg_labB " + avg_labB);

                    avg_de_fill = (avg_labD > 0) ? 1 : 0;

                    // double DIFF_FILL = cClass.calcDE(lab_r, lab);

//                    ((lab[0] > seek_minL && lab[0] < seek_maxL) &&
//                    (labA1 > seek_minA && labA1 < seek_maxA) &&
//                    (labB2 > seek_minB && labB2 < seek_maxB) )
//                    double sumDE = 0;
//                    double DIFF_FILL = (avg_olde > 0 ) ? cClass.calcDE(lab_r, lab) : 0;
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
                    logBins[TOTAL_FILL][y] = avg_total_fill;
                    logBins[DE][y] = avg_labD;
                    //   if (x < 2 && y < 10)                        Log.e("xx", " avg_di_fill " + avg_di_fill + " avg_de_fill " + avg_de_fill + " logBins " + logBins[DE_FILL][y]);
                }
                //    if (y < 10)                    Log.e("xx y", " logBins " + logBins[DE_FILL][y]);
            }

            bMap1 = Bitmap.createBitmap(filterArray1, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);//Initialize the bitmap, with the replaced color
            bMap2 = Bitmap.createBitmap(filterArray2, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            //bMapL = Bitmap.createBitmap(filterArrayL, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            bMapA = Bitmap.createBitmap(filterArrayA, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            bMapB = Bitmap.createBitmap(filterArrayB, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            // Log.e("getWidth", bitmap.getWidth() + " length " + compareBins[C_RED].length);
            //      Log.e("cutoffBins", bitmap.getWidth() + " length " + cutoffBins[LAB_L_MAX].length + cutoffBins[HSL].length + cutoffBins[OLDE].length);

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
                        for (int j = 0; j < L_MAX; j++) {
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
                float scaleX = (float) ((double) getWidth() / ((double) L_MAX));
                float scaleDE;
                float scaleAX;
                float scaleBX;
                if (!isCut) {
                    scaleAX = (float) ((double) getWidth() / ((double) A_MAX));
                    scaleBX = (float) ((double) getWidth() / ((double) B_MAX));
                    scaleDE = (float) ((double) getWidth() / ((double) DE_MAX));
                } else {
                    scaleAX = (float) ((double) getWidth() / ((double) L_MAX));
                    scaleBX = (float) ((double) getWidth() / ((double) L_MAX));
                    scaleDE = (float) ((double) getWidth() / ((double) DE_LIM));
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
                            for (int x = 0; x < L_MAX; x++) {
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
                                for (int x = 0; x < A_LIM; x++) {
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
                                for (int x = B_LIM; x < B_MAX; x++) {
                                    int valueY = (int) ((compareBins[i][x] / maxHist[i]) * (getHeight()));
                                    wallpath.lineTo((x - B_LIM) * scaleBX * offset, getHeight() - valueY);
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
                float scaleX = (float) ((double) getWidth() / ((double) L_MAX));
                float scaleDE;
                float scaleAX;
                float scaleBX;
                if (!isCut) {
                    scaleAX = (float) ((double) getWidth() / ((double) A_MAX));
                    scaleBX = (float) ((double) getWidth() / ((double) B_MAX));
                    scaleDE = (float) ((double) getWidth() / ((double) DE_MAX));
                } else {
                    scaleAX = (float) ((double) getWidth() / ((double) L_MAX));
                    scaleBX = (float) ((double) getWidth() / ((double) L_MAX));
                    scaleDE = (float) ((double) getWidth() / ((double) DE_LIM));
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
                            for (int x = 0; x < L_MAX; x++) {
                                int valueY = (int) ((updatedBins[i][x] / maxHistUpd[i]) * (getHeight()));
                                wallpath.lineTo(x * scaleX * offset, getHeight() - valueY);
                                //if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                            }
                            break;
                        case HSL:
                            for (int x = 0; x < L_MAX; x++) {
                                int valueY = (int) ((compareBins[i][x] / maxHistUpd[i]) * (getHeight()));
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
                                for (int x = 0; x < A_LIM; x++) {
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
                                for (int x = B_LIM; x < B_MAX; x++) {
                                    int valueY = (int) ((updatedBins[i][x] / maxHistUpd[i]) * (getHeight()));
                                    wallpath.lineTo((x - B_LIM) * scaleBX * offset, getHeight() - valueY);
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

                canvas.drawBitmap(bMap1, new Rect(0, 0, bi.getWidth(), bi.getHeight()), new Rect(0, 0, getWidth(), (int) (getHeight() * offset)), null);

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
                            wallpaint.setColor(Color.CYAN);
                            wallpaint.setColor(Color.TRANSPARENT);
                            break;
                        case LAB_B2:
                            wallpaint.setColor(Color.WHITE);
                            wallpaint.setColor(Color.TRANSPARENT);
                            break;
                        case DE:
                            wallpaint.setColor(Color.BLUE);
                            wallpaint.setColor(Color.TRANSPARENT);
                            break;
                        case DIFF_FILL:
                            //wallpaint.setColor(Color.parseColor("#3300ff00")); // another
                            wallpaint.setColor(Color.argb(40, 0, 255, 0));
                            //wallpaint.setColor(Color.TRANSPARENT);
                            break;
                        case DE_FILL:
                            wallpaint.setColor(Color.argb(40, 0, 0, 255));
                            //wallpaint.setColor(Color.TRANSPARENT);
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
                                int scaleX = (int) ((logBins[i][y] / (double) L_MAX) * (getWidth())); //maxLog[i]
                                wallpath.lineTo(scaleX, y * scaleY * offset);
                                //Log.e("maxLog"," maxLog[i] " + maxLog[i]);
                            }
                            break;
                        case LAB_L:
                            for (int y = 0; y < bi.getHeight(); y++) {
                                int scaleX = (int) ((logBins[i][y] / (double) L_MAX) * (getWidth()));
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
                                //Log.e("HISTO", " j " + scaleX + " logBins[i][y] " + logBins[i][y] + " j " + ((double) logBins[i][y] / (double) L_MAX));
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
                float scaleX = (float) ((double) getWidth() / ((double) L_MAX));

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
                            for (int x = 0; x < L_MAX; x++) {
                                int valueY = (int) ((compareBins[i][x] / maxHist[i]) * (getHeight()));
                                wallpath.lineTo(x * scaleX * offset, getHeight() - valueY);
                                //if (j > COLOR_MAX - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                            }
                            break;
                        case LAB_L:
                            for (int x = 0; x < L_MAX; x++) {
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

    class LogFiltered1 extends View {

        private Bitmap bi;

        public LogFiltered1(Context context, Bitmap bmp) {
            super(context);
            bi = bmp;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (isLoaded) {
                //canvas.drawColor(Color.GRAY);

                //canvas.drawBitmap(bMapL, new Rect(0, 0, view_redraw_filt1.getWidth(), view_redraw_filt1.getHeight()), new Rect(0, 0, view_redraw_filt1.getWidth(), view_redraw_filt1.getHeight()), null);
                canvas.drawBitmap(bMapA, new Rect(0, 0, bi.getWidth(), bi.getHeight()), new Rect(0, 0, getWidth(), (int) (getHeight() * offset)), null);
                canvas.drawBitmap(bMapB, new Rect(0, 0, bi.getWidth(), bi.getHeight()), new Rect(0, 0, getWidth(), (int) (getHeight() * offset)), null);


                float scaleY = (float) ((double) getHeight() / ((double) bi.getHeight()));

                for (int i = 0; i < NUMBER_OF_VARIABLES; i++) {
                    Paint wallpaint;

                    wallpaint = new Paint();
                    switch (i) {
                        case LAB_A1:
                            wallpaint.setColor(Color.BLACK);
                            break;
                        case LAB_B2:
                            wallpaint.setColor(Color.WHITE);
                            break;
                        case DIFF_FILL:
                            //wallpaint.setColor(Color.parseColor("#3300ff00")); // another
                            wallpaint.setColor(Color.argb(40, 0, 0, 0));
                            break;
                    }
                    wallpaint.setStyle(Paint.Style.STROKE);

                    Path wallpath = new Path();
                    wallpath.reset();
                    wallpath.moveTo(0, 0);
                    switch (i) {
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

    class LogFiltered2 extends View {

        private Bitmap bi;

        public LogFiltered2(Context context, Bitmap bmp) {
            super(context);
            bi = bmp;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (isLoaded) {
                //canvas.drawColor(Color.GRAY);

                canvas.drawBitmap(bMap2, new Rect(0, 0, bi.getWidth(), bi.getHeight()), new Rect(0, 0, getWidth(), (int) (getHeight() * offset)), null);

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

    class LogFiltered3 extends View {

        private Bitmap bi;

        public LogFiltered3(Context context, Bitmap bmp) {
            super(context);
            bi = bmp;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (isLoaded) {
                //canvas.drawColor(Color.GRAY);

                if (view_check.isChecked())
                    canvas.drawBitmap(bMap3, new Rect(0, 0, bi.getWidth(), bi.getHeight()), new Rect(0, 0, getWidth(), (int) (getHeight() * offset)), null);
                else
                    canvas.drawBitmap(bMap1, new Rect(0, 0, bi.getWidth(), bi.getHeight()), new Rect(0, 0, getWidth(), (int) (getHeight() * offset)), null);


                float scaleY = (float) ((double) getHeight() / ((double) bi.getHeight()));

                for (int i = 0; i < NUMBER_OF_VARIABLES; i++) {
                    Paint wallpaint;

                    wallpaint = new Paint();
                    switch (i) {
                        case NTG:
                            wallpaint.setColor(Color.RED);
                            break;
                        case TOTAL_FILL:
                            wallpaint.setColor(Color.argb(80, 0, 0, 255));
                            break;
                    }
                    wallpaint.setStyle(Paint.Style.STROKE);

                    Path wallpath = new Path();
                    wallpath.reset();
                    wallpath.moveTo(0, 0);
                    switch (i) {
                        case NTG:
                            if (view_check.isChecked()) {
                                wallpath.moveTo(getWidth(), 0);
                                for (int y = 0; y < bi.getHeight(); y++) {
                                    int scaleX = (int) ((logBins[i][y] / (double) DE_MAX) * (getWidth()));
                                    wallpath.lineTo(getWidth() - scaleX, y * scaleY * offset);
                                }
                                break;
                            }
                        case TOTAL_FILL:
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

    public void resetHist() {
        setSeekMinMax();
    }

    public void setSeekMinMax() {
        int minL, maxL, minA, maxA, minB, maxB, minDE, maxDE;
        seek_minL = minL = 0;
        seek_maxL = maxL = L_MAX;
        if (!isCut) {
            seek_minA = minA = 0;
            seek_maxA = maxA = A_MAX;
            seek_minB = minB = 0;
            seek_maxB = maxB = B_MAX;
            seek_minDE = minDE = 0;
            seek_minDE = maxDE = DE_MAX;
        } else {
            seek_minA = minA = 0;
            seek_maxA = maxA = A_LIM;
            seek_minB = minB = B_LIM;
            seek_maxB = maxB = B_MAX;
            seek_minDE = minDE = 0;
            seek_minDE = maxDE = DE_LIM;
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

        view_seekDE.setRangeRules(minDE, maxDE, 0, 1);
        view_seekDE.setValue(minDE, maxDE);
        view_seekDE.setMinProgress(minDE);
        view_seekDE.setMaxProgress(maxDE);

        int mod_minB = minB - B_LIM;
        int mod_maxB = maxB - B_LIM;
        view_seek_txtL.setText(minL + "/" + maxL);
        view_seek_txtA.setText(minA + "/" + maxA);
        view_seek_txtDE.setText(minDE + "/" + maxDE);
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
}