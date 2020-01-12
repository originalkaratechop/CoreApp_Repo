package com.example.android.coreapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

public class FragmentHistogram extends Fragment {

    Bitmap coreBitmap = null;

    boolean isColored;

    LinearLayout view_hist_lightness, view_hist_rgb, view_hist_log, view_root_hist,
            lo_switchRGB, lo_histRGB;
    View lo_histSeparator;
    ImageView view_core_img, view_hist_img;
    FloatingActionButton view_fab;
    ImageView btn_hide;
    Switch switchRGB;
    boolean isHidden;

    RangeSeekBar view_seekL, view_seekA, view_seekB;
    TextView view_seek_txtL, view_seek_txtA, view_seek_txtB;
    private ProgressBar spinner;
    TextView txt_load;

    boolean isNotNew;

    private int SIZE = 256;
    private int WIDTH_XML = 257;
    private int LIGHT_MAX = 101;
    private int seek_minL, seek_maxL, seek_minA, seek_maxA, seek_minB, seek_maxB;
    // Red, Green, Blue
    private int NUMBER_OF_COLOURS = 3;
    public final int RED = 0;
    public final int GREEN = 1;
    public final int BLUE = 2;

    private int NUMBER_OF_LIGHTNESSES = 5;
    public final int LAB = 0;
    public final int HSL = 1;
    public final int OLDE = 2;
    public final int LAB1 = 3;
    public final int LAB2 = 4;

    public double[][] colourBins; // all were int[][]
    public double[][] lightnessBins;
    public double[][] logBins;
    public double[] maxColor, maxHist;
    private volatile boolean loaded = false;

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

        view_hist_lightness = view.findViewById(R.id.hist_lightness);
        view_hist_rgb = view.findViewById(R.id.hist_rgb);
        view_hist_log = view.findViewById(R.id.hist_log);
        view_root_hist = view.findViewById(R.id.view_root_hist);
        lo_histRGB = view.findViewById(R.id.lo_hist_rgb);
        lo_histSeparator = view.findViewById(R.id.lo_separator);

        view_core_img = getActivity().findViewById(R.id.view_core);
        view_core_img = view.findViewById(R.id.hist_img);

        view_fab = view.findViewById(R.id.hist_fab);

        view_seekL = view.findViewById(R.id.seekBarL);
        view_seekA = view.findViewById(R.id.seekBarA);
        view_seekB = view.findViewById(R.id.seekBarB);
        view_seek_txtL = view.findViewById(R.id.seekTxtL);
        view_seek_txtA = view.findViewById(R.id.seekTxtA);
        view_seek_txtB = view.findViewById(R.id.seekTxtB);
        resetHist();

        view_seekL.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser) {
                seek_minL = (int) min;
                seek_maxL = (int) max;
                view_seek_txtL.setText(seek_minL + "/" + seek_maxL);
                view_fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
            }
        });
        view_seekA.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser) {
                seek_minA = (int) min;
                seek_maxA = (int) max;
                view_seek_txtA.setText(seek_minA + "/" + seek_maxA);
                view_fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
            }
        });
        view_seekB.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser) {
                seek_minB = (int) min;
                seek_maxB = (int) max;
                view_seek_txtB.setText(seek_minB + "/" + seek_maxB);
                view_fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
            }
        });

        view_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                starter(view_core_img);
            }
        });

        switchRGB = view.findViewById(R.id.switchRGB);
        lo_switchRGB = view.findViewById(R.id.switchRGB_container);

        switchRGB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchRGB.isChecked()) {
                    lo_histRGB.setVisibility(View.VISIBLE);
                    lo_histSeparator.setVisibility(View.GONE);
                }
                else {
                    lo_histRGB.setVisibility(View.GONE);
                    lo_histSeparator.setVisibility(View.VISIBLE);
                }
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
        btn_hide.setImageDrawable(ContextCompat.getDrawable(getActivity(), (android.R.drawable.arrow_down_float)));
        isHidden = false;
        btn_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isHidden) {
                    btn_hide.setImageDrawable(ContextCompat.getDrawable(getActivity(), (android.R.drawable.arrow_down_float)));
                    isHidden = true;
                    view_root_hist.setVisibility(View.VISIBLE);
                    lo_switchRGB.setVisibility(View.VISIBLE);
                } else {
                    btn_hide.setImageDrawable(ContextCompat.getDrawable(getActivity(), (android.R.drawable.arrow_up_float)));
                    isHidden = false;
                    view_root_hist.setVisibility(View.GONE);
                    lo_switchRGB.setVisibility(View.GONE);
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

        loaded = false;
        isNotNew = false;
        isColored = true;
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

            view_root_hist.setVisibility(View.GONE);
            view_fab.setVisibility(View.GONE);
            btn_hide.setVisibility(View.GONE);
            lo_switchRGB.setVisibility(View.GONE);
            if (isNotNew) {
                view_hist_lightness.removeAllViews();
                view_hist_rgb.removeAllViews();
                view_hist_log.removeAllViews();

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

            btn_hide.setVisibility(View.VISIBLE);
            btn_hide.setImageDrawable(ContextCompat.getDrawable(getActivity(), (android.R.drawable.arrow_down_float)));
            isHidden = false;
            lo_switchRGB.setVisibility(View.VISIBLE);
            if (coreBitmap != null) {
                view_hist_lightness.addView(new LightnessHistogram(getActivity(), coreBitmap));
                view_hist_rgb.addView(new regularHist(getActivity(), coreBitmap));
                view_hist_log.addView(new LightnessLog(getActivity(), coreBitmap));
            }
        }
    }

    public void load(Bitmap bitmap) {

        colourBins = new double[NUMBER_OF_COLOURS][];
        lightnessBins = new double[NUMBER_OF_LIGHTNESSES][];
        logBins = new double[NUMBER_OF_LIGHTNESSES][];

        for (int i = 0; i < NUMBER_OF_COLOURS; i++) {
            colourBins[i] = new double[SIZE];
        }

        for (int i = 0; i < NUMBER_OF_LIGHTNESSES; i++) {
            lightnessBins[i] = new double[LIGHT_MAX];
            logBins[i] = new double[bitmap.getHeight()];
        }

        if (bitmap != null) {
            // Reset all the bins
            for (int i = 0; i < NUMBER_OF_COLOURS; i++) {
                for (int x = 0; x < SIZE; x++) {
                    colourBins[i][x] = 0;
                }
            }

            for (int i = 0; i < NUMBER_OF_LIGHTNESSES; i++) {
                for (int x = 0; x < LIGHT_MAX; x++) {
                    lightnessBins[i][x] = 0;
                }

                for (int y = 0; y < bitmap.getHeight(); y++) {
                    logBins[i][y] = 0;
                }
            }

            for (int x = 0; x < bitmap.getWidth(); x++) {
                for (int y = 0; y < bitmap.getHeight(); y++) {

                    int pixel = bitmap.getPixel(x, y);
                    colourBins[RED][Color.red(pixel)]++;
                    colourBins[GREEN][Color.green(pixel)]++;
                    colourBins[BLUE][Color.blue(pixel)]++;

                    double[] lab = new double[3];
                    ColorUtils.RGBToLAB(Color.red(pixel), Color.green(pixel), Color.blue(pixel), lab);

                    float[] hsl = new float[3];
                    ColorUtils.RGBToHSL(Color.red(pixel), Color.green(pixel), Color.blue(pixel), hsl);

                    double sr = Color.red(pixel) / 255.0;
                    sr = sr < 0.04045 ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
                    double sg = Color.green(pixel) / 255.0;
                    sg = sg < 0.04045 ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
                    double sb = Color.blue(pixel) / 255.0;
                    sb = sb < 0.04045 ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);

                    int olde_lin = (int) Math.round(100 * ((sr * 0.299) + (sg * 0.587) + (sb * 0.114)));
                    int olde = (int) Math.round(((Color.red(pixel) * 299) + (Color.green(pixel) * 587) + (Color.blue(pixel) * 114)) / 1000 / 256.0 * 100);
                    //    double olde = 100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722);
//                    Log.e("xx", Color.red(pixel) + " " + Color.green(pixel) + " " + Color.blue(pixel) + " "
//                            + (100 * ((sr * 0.299) + (sg * 0.587) + (sb * 0.114))) + " lab " + olde_lin + " " + Math.round(olde));

                    lightnessBins[LAB][(int) Math.round(lab[0])]++;
                    lightnessBins[HSL][Math.round(hsl[2] * 100)]++;
                    lightnessBins[OLDE][olde]++;

                    int lab1 = (int) Math.round((lab[1] + 110) / 2.1);
                    int lab2 = (int) Math.round((lab[2] + 110) / 2.1);
                    lightnessBins[LAB1][lab1]++;
                    lightnessBins[LAB2][lab2]++;
//                    lightnessBins[HSL][olde]++;
                    //  if (x > 1010)
//                    Log.e("xx", " x " + x + " y " + y
//                            + " // lab " + Math.round(lab[0]) + " hsl " + Math.round(hsl[2] * 100) + " olde_lin " + olde_lin + " olde " + olde
//                            + " // red " + Color.red(pixel) + " green " + Color.green(pixel) + " blue " + Color.blue(pixel));
                }
            }
            for (int y = 0; y < bitmap.getHeight(); y++) {

                double px_total1, px_total11, px_total12, px_total2, px_total3;
                double sum1, sum11, sum12, sum2, sum3;
                sum1 = sum11 = sum12 = sum2 = sum3 = 0;
                int avg_lab, avg_lab1, avg_lab2, avg_hsl, avg_olde;
                double w = bitmap.getWidth();

                for (int x = 0; x < bitmap.getWidth(); x++) {
                    int pixel = bitmap.getPixel(x, y);
                    double[] lab = new double[3];
                    ColorUtils.RGBToLAB(Color.red(pixel), Color.green(pixel), Color.blue(pixel), lab);
                    float[] hsl = new float[3];
                    ColorUtils.RGBToHSL(Color.red(pixel), Color.green(pixel), Color.blue(pixel), hsl);

                    int olde = (int) Math.round(((Color.red(pixel) * 299) + (Color.green(pixel) * 587) + (Color.blue(pixel) * 114)) / 1000 / 256.0 * 100);
                    //double l1 = (lab[1] + 110) / 2.1; // before
                    double l1 = (lab[1] + 110.0) / 2.1;
                    double l2 = (lab[2] + 110.0) / 2.1;
                    //    if (lab[0] < view_seek.getProgress())
                    ///px_total1 = lab[0] / w;
                    px_total1 = (lab[0] > seek_minL && lab[0] < seek_maxL) ? lab[0] / w : 0;
                    sum1 = sum1 + px_total1;
                    avg_lab = (int) sum1;

                    //px_total11 = l1 / w;
                    px_total11 = (l1 > seek_minA && l1 < seek_maxA) ? l1 / w : 0;
                    sum11 = sum11 + px_total11;
                    avg_lab1 = (int) sum11;

                    //px_total12 = l2 / w;
                    px_total12 = (l2 > seek_minB && l2 < seek_maxB) ? l2 / w : 0;
                    sum12 = sum12 + px_total12;
                    avg_lab2 = (int) sum12;

                    //if (lab2 < 0)
                    //Log.e("xx", " lab[1] " + sum11 + " y " + sum12);
                    px_total2 = hsl[2] * 100 / w;
                    sum2 = sum2 + px_total2;
                    avg_hsl = (int) sum2;

                    px_total3 = olde / w;
                    sum3 = sum3 + px_total3;
                    avg_olde = (int) sum3;

                    // int avg = (int) Math.round(lab[0]);
                    // Log.e("avg", bitmap.getWidth() + " px_total " + px_total + " sum " + sum + " avg " + avg);
                    logBins[LAB][y] = avg_lab;
                    logBins[HSL][y] = avg_hsl;
                    logBins[OLDE][y] = avg_olde;
                    logBins[LAB1][y] = avg_lab1;
                    logBins[LAB2][y] = avg_lab2;
                }
            }

            // Log.e("getWidth", bitmap.getWidth() + " length " + compareBins[RED].length);
            //      Log.e("lightnessBins", bitmap.getWidth() + " length " + lightnessBins[LAB_L].length + lightnessBins[HSL].length + lightnessBins[OLDE].length);

            maxColor = new double[NUMBER_OF_COLOURS];
            for (int i = 0; i < NUMBER_OF_COLOURS; i++) {
                double max = 0;
                for (int j = 0; j < SIZE; j++) {
                    if (max < colourBins[i][j]) {
                        max = colourBins[i][j];
                    }
                }
                maxColor[i] = max;
                max = 0;
            }
            Log.e("mex[]", maxColor[0] + " " + maxColor[1] + " " + maxColor[2] + " ");

            maxHist = new double[NUMBER_OF_LIGHTNESSES];
            for (int i = 0; i < NUMBER_OF_LIGHTNESSES; i++) {
                double max = 0;
                for (int j = 0; j < LIGHT_MAX; j++) {
                    if (max < lightnessBins[i][j]) {
                        max = lightnessBins[i][j];
                    }
                }
                maxHist[i] = max;
                max = 0;
            }

            loaded = true;
        } else {
            loaded = false;
        }
    }

    class LightnessHistogram extends View {

        public LightnessHistogram(Context context, Bitmap bmp) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (loaded) {
                canvas.drawColor(Color.GRAY);

                // size - 1 = colors on xAxis, WIDTH_XML = pixels in xml layout (wrong to use)
                float scaleX = (float) ((double) getWidth() / ((double) LIGHT_MAX - 1));

                Log.e("HI", "H : " + getHeight()
                        + ", W : " + getWidth()
                        + ", xInt : " + scaleX
                        + ", lineTo : " + scaleX * offset);

                for (int i = 0; i < NUMBER_OF_LIGHTNESSES; i++) {
                    Paint wallpaint;

                    wallpaint = new Paint();
                    if (i == LAB) {
                        wallpaint.setColor(Color.RED);
                    } else if (i == HSL) {
                        wallpaint.setColor(Color.GREEN);
                    } else if (i == OLDE) {
                        wallpaint.setColor(Color.BLUE);
                    } else if (i == LAB1) {
                        wallpaint.setColor(Color.BLACK);
                    } else wallpaint.setColor(Color.WHITE);

                    wallpaint.setStyle(Paint.Style.STROKE);

                    Path wallpath = new Path();
                    wallpath.reset();
                    wallpath.moveTo(0, getHeight());
                    for (int x = 0; x < LIGHT_MAX; x++) {
                        int valueY = (int) ((((double) lightnessBins[i][x]  / (double) maxHist[i]) + 110.0) / 2.1 * (getHeight()));
                        wallpath.lineTo(x * scaleX * offset, getHeight() - valueY);
                        //if (j > SIZE - 3) Log.e("HISTO"," j " + j * xInterval * offset);
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

            if (loaded) {
                canvas.drawColor(Color.GRAY);

                float scaleY = (float) ((double) getHeight() / ((double) bi.getHeight()));

                for (int i = 0; i < NUMBER_OF_LIGHTNESSES; i++) {
                    Paint wallpaint;

                    wallpaint = new Paint();
                    if (i == LAB) {
                        wallpaint.setColor(Color.RED);
                    } else if (i == HSL) {
                        wallpaint.setColor(Color.GREEN);
                    } else if (i == OLDE) {
                        wallpaint.setColor(Color.BLUE);
                    } else if (i == LAB1) {
                        wallpaint.setColor(Color.BLACK);
                    } else wallpaint.setColor(Color.WHITE);

                    wallpaint.setStyle(Paint.Style.STROKE);

                    Path wallpath = new Path();
                    wallpath.reset();
                    wallpath.moveTo(0, 0);
                    for (int y = 0; y < bi.getHeight(); y++) {
                        int scaleX = (int) (((double) logBins[i][y] / (double) LIGHT_MAX) * (getWidth()));
                        wallpath.lineTo(scaleX, y * scaleY * offset);
                    }
                    canvas.drawPath(wallpath, wallpaint);
                }
            }
        }
    }

    class regularHist extends View {

        public regularHist(Context context, Bitmap bi) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (loaded) {
                canvas.drawColor(Color.GRAY);

                // size - 1 = colors on xAxis, WIDTH_XML = pixels in xml layout (wrong to use)
                float xInterval = (float) ((double) getWidth() / ((double) SIZE - 1));

                for (int i = 0; i < NUMBER_OF_COLOURS; i++) {

                    Paint wallpaint;

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
                        int value = (int) (((double) colourBins[i][j] / (double) maxColor[i]) * (getHeight()));
                        wallpath.lineTo(j * xInterval * offset, getHeight() - value);
                        //                      if (j > SIZE - 3) Log.e("HISTO"," j " + j * xInterval * offset);
                    }
                    canvas.drawPath(wallpath, wallpaint);
                }
            }

        }
    }

    public void resetHist() {
        seek_minL = seek_minA = seek_minB = 0;
        seek_maxL = seek_maxA = seek_maxB = 100;
        view_seek_txtL.setText(seek_minL + "/" + seek_maxL);
        view_seek_txtA.setText(seek_minA + "/" + seek_maxA);
        view_seek_txtB.setText(seek_minB + "/" + seek_maxB);
        view_seekL.setValue(seek_minL, seek_maxL);
        view_seekA.setValue(seek_minA, seek_maxA);
        view_seekB.setValue(seek_minB, seek_maxB);
    }
}