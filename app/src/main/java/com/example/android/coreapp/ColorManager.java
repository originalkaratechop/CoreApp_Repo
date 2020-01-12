package com.example.android.coreapp;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorManager {

    private int r, g, b;
    private double dr, dg, db;

    private static final int I_COUNT = 10;
    private static final int LAB_L_MAX = 94;
    private static final int DE_MAX = 24;

    public ColorManager() {
    }

    public List<Integer> getColorArray(int color) {
        r = Color.red(color);
        g = Color.green(color);
        b = Color.blue(color);
        return (List<Integer>) new ArrayList(Arrays.asList(r, g, b));
    }

    public List<Integer> mixAlphaCompositing(double a, double c1r, double c1g, double c1b,
                                             double c2r, double c2g, double c2b) {
//        dr = (c2r - c1r * (1 - a)) / a;
//        dg = (c2g - c1g * (1 - a)) / a;
//        db = (c2b - c1b * (1 - a)) / a;
        dr = (c2r - c1r * a);
        dg = (c2g - c1g * a);
        db = (c2b - c1b * a);
        return (List<Integer>) new ArrayList(Arrays.asList((int) dr, (int) dg, (int) db));
    }


    public List<Integer> mixAlphaCompositingMax(double c1r, double c1g, double c1b,
                                                double c2r, double c2g, double c2b) {
        double[] ar = new double[3];
        ar[0] = c2r / c1r;
        ar[1] = c2g / c1g;
        ar[2] = c2b / c1b;

        double min_a = 1;
        for (int i = 0; i < ar.length; i++) {
            if (ar[i] < min_a && ar[i] >= 0) {
                min_a = ar[i];
            }
        }

        dr = (c2r - c1r * min_a);
        dg = (c2g - c1g * min_a);
        db = (c2b - c1b * min_a);
        return (List<Integer>) new ArrayList(Arrays.asList((int) dr, (int) dg, (int) db, (int) (min_a * 100)));
    }

/* //blender alpha comp as is (w/o subtract)
    dr = (c1r * (1 - a) + c2r * (a));
    dg = (c1g * (1 - a) + c2g * (a));
    db = (c1b * (1 - a) + c2b * (a));

    //stack overflow formula
            dr = 255 - Math.sqrt((Math.pow((255 - c2r), 2) + Math.pow((255 - c1r), 2)) / 2);
            dg = 255 - Math.sqrt((Math.pow((255 - c2g), 2) + Math.pow((255 - c1g), 2)) / 2);
            db = 255 - Math.sqrt((Math.pow((255 - c2b), 2) + Math.pow((255 - c1b), 2)) / 2);
*/

    public List<Integer> mixInverseGamma(double a, double c1r, double c1g, double c1b,
                                         double c2r, double c2g, double c2b) {

        c1r = inverseGammaAdjustment(c1r, c1g, c1b).get(0);
        c1g = inverseGammaAdjustment(c1r, c1g, c1b).get(1);
        c1b = inverseGammaAdjustment(c1r, c1g, c1b).get(2);

        c2r = inverseGammaAdjustment(c2r, c2g, c2b).get(0);
        c2g = inverseGammaAdjustment(c2r, c2g, c2b).get(1);
        c2b = inverseGammaAdjustment(c2r, c2g, c2b).get(2);

//        dr = (c2r - c1r * (1 - a)) / a;
//        dg = (c2g - c1g * (1 - a)) / a;
//        db = (c2b - c1b * (1 - a)) / a;
        dr = (c2r - c1r * a);
        dg = (c2g - c1g * a);
        db = (c2b - c1b * a);

        dr = properGammaAdjustment(dr, dg, db).get(0);
        dg = properGammaAdjustment(dr, dg, db).get(1);
        db = properGammaAdjustment(dr, dg, db).get(2);

        return (List<Integer>) new ArrayList(Arrays.asList((int) dr, (int) dg, (int) db));
    }

    private List<Double> inverseGammaAdjustment(double r, double g, double b) {
        //Convert color from 0..255 to 0..1
        r = r / 255;
        g = g / 255;
        b = b / 255;

        //Inverse Red, Green, and Blue
        if (r > 0.04045) r = Math.pow((r + 0.055) / 1.055, 2.4);
        else r = r / 12.92;
        if (g > 0.04045) g = Math.pow((g + 0.055) / 1.055, 2.4);
        else g = g / 12.92;
        if (b > 0.04045) b = Math.pow((b + 0.055) / 1.055, 2.4);
        else b = b / 12.92;

        //return new color. Convert 0..1 back into 0..255
        r = r * 255;
        g = g * 255;
        b = b * 255;
        return (List<Double>) new ArrayList(Arrays.asList(r, g, b));
    }

    private List<Double> properGammaAdjustment(double r, double g, double b) {
        //Convert color from 0..255 to 0..1
        r = r / 255;
        g = g / 255;
        b = b / 255;

        //Apply companding to Red, Green, and Blue
        if (r > 0.0031308) r = 1.055 * Math.pow(r, 1 / 2.4) - 0.055;
        else r = r * 12.92;
        if (g > 0.0031308) g = 1.055 * Math.pow(g, 1 / 2.4) - 0.055;
        else g = g * 12.92;
        if (b > 0.0031308) b = 1.055 * Math.pow(b, 1 / 2.4) - 0.055;
        else b = b * 12.92;

        //return new color. Convert 0..1 back into 0..255
        r = r * 255;
        g = g * 255;
        b = b * 255;
        return (List<Double>) new ArrayList(Arrays.asList(r, g, b));
    }

    public List<Integer> mixLAB(double a, int c1r, int c1g, int c1b,
                                int c2r, int c2g, int c2b) {
        double[] lab1 = new double[3];
        double[] lab2 = new double[3];
        double[] outResult = new double[3];

        ColorUtils.RGBToLAB(c1r, c1g, c1b, lab1);
        ColorUtils.RGBToLAB(c2r, c2g, c2b, lab2);
        reverseBlendLAB(lab1, lab2, a, outResult);

        int color = ColorUtils.LABToColor(outResult[0], outResult[1], outResult[2]);
        r = Color.red(color);
        g = Color.green(color);
        b = Color.blue(color);

        return (List<Integer>) new ArrayList(Arrays.asList(r, g, b));
    }

    //from ColorUtils
    public static void reverseBlendLAB(@NonNull double[] lab1, @NonNull double[] lab2,
                                       @FloatRange(from = 0.0, to = 1.0) double ratio, @NonNull double[] outResult) {
        if (outResult.length != 3) {
            throw new IllegalArgumentException("outResult must have a length of 3.");
        }
        final double inverseRatio = 1 - ratio;
//        outResult[0] = (lab2[0] - lab1[0] * inverseRatio) / ratio;
//        outResult[1] = (lab2[1] - lab1[1] * inverseRatio) / ratio;
//        outResult[2] = (lab2[2] - lab1[2] * inverseRatio) / ratio;
        outResult[0] = (lab2[0] - lab1[0] * ratio);
        outResult[1] = (lab2[1] - lab1[1] * ratio);
        outResult[2] = (lab2[2] - lab1[2] * ratio);
    }


    public List<Integer> mixExperimentalGamma(double a, double c1r, double c1g, double c1b,
                                              double c2r, double c2g, double c2b) {
        c1r = inverseGammaAdjustment(c1r, c1g, c1b).get(0);
        c1g = inverseGammaAdjustment(c1r, c1g, c1b).get(1);
        c1b = inverseGammaAdjustment(c1r, c1g, c1b).get(2);

        c2r = inverseGammaAdjustment(c2r, c2g, c2b).get(0);
        c2g = inverseGammaAdjustment(c2r, c2g, c2b).get(1);
        c2b = inverseGammaAdjustment(c2r, c2g, c2b).get(2);

//        dr = (c2r - c1r * (1 - a)) / a;
//        dg = (c2g - c1g * (1 - a)) / a;
//        db = (c2b - c1b * (1 - a)) / a;
        dr = (c2r - c1r * a);
        dg = (c2g - c1g * a);
        db = (c2b - c1b * a);
        //     Log.e("inverse ", Math.round(dr) +" "+  Math.round(dg)+" "+Math.round(db));

        double gamma = 0.43;
        double brightness1 = Math.pow(c1r + c1g + c1b, gamma);
        double brightness2 = Math.pow(c2r + c2g + c2b, gamma);

        //      Log.e("inverse ", Math.round(brightness1) +" "+  Math.round(brightness2));
        //Interpolate a new brightness value, and convert back to linear light
//        double brightness = (brightness2 - brightness1 * (1 - a)) / a;
        double brightness = (brightness2 - brightness1 * a);
        double intensity = Math.pow(brightness, (1 / gamma));

        //    Log.e("inverse ", Math.round(brightness) +" "+  Math.round(intensity));
        //Apply adjustment factor to each rgb value based
        if ((dr + dg + db) != 0) {
            double factor = (intensity / (dr + dg + db));
            dr = dr * factor;
            dg = dg * factor;
            db = db * factor;
        }
        //   Log.e("inverse ", Math.round(dr) +" "+  Math.round(dg)+" "+Math.round(db));
        return (List<Integer>) new ArrayList(Arrays.asList((int) dr, (int) dg, (int) db));
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

    public List<Double> calcMinA1(double max_a, double c1r, double c1g, double c1b,
                                  double c2r, double c2g, double c2b, double[] lab_ref) {
        double real_min = 0;
        int rj, gj, bj;
        rj = gj = bj = 0;
        nest:
        for (int o = 0; o < I_COUNT; o++) {
            int ri, gi, bi;
            double dEi;
            double m = o / 10.0;
            double ma = max_a / 100.0;
            double[] lab_sample = new double[3];
            List<Integer> mix_i;
            mix_i = mixAlphaCompositing(m,
                    c1r, c1g, c1b,
                    c2r, c2g, c2b);
            ri = mix_i.get(0);
            gi = mix_i.get(1);
            bi = mix_i.get(2);

            ColorUtils.RGBToLAB(ri, gi, bi, lab_sample);
            lab_sample[0] = LAB_L_MAX;
            lab_sample[1] = lab_sample[1];
            lab_sample[2] = lab_sample[2];
            dEi = calcDE(lab_ref, lab_sample);

            if (m <= ma && dEi <= DE_MAX && ri >= 0 && gi >= 0 && bi >= 0)
                for (int j = 0; j < I_COUNT; j++) {
                    double k = (o * 10 + j) / 100.0;
                    mix_i = mixAlphaCompositing(k,
                            c1r, c1g, c1b,
                            c2r, c2g, c2b);
                    ri = mix_i.get(0);
                    gi = mix_i.get(1);
                    bi = mix_i.get(2);

                    ColorUtils.RGBToLAB(ri, gi, bi, lab_sample);
                    lab_sample[0] = LAB_L_MAX;
                    lab_sample[1] = lab_sample[1];
                    lab_sample[2] = lab_sample[2];
                    dEi = calcDE(lab_ref, lab_sample);

                    if (k <= ma && dEi <= DE_MAX && ri >= 0 && gi >= 0 && bi >= 0) {
                        real_min = k;
                        rj = ri;
                        gj = gi;
                        bj = bi;
                    } else
                        return (List<Double>) new ArrayList(Arrays.asList(real_min, (double) rj, (double) gj, (double) bj));
                }
        }
        return (List<Double>) new ArrayList(Arrays.asList(real_min, (double) rj, (double) gj, (double) bj));
    }

    public List<Double> calcMinA2(double max_a, double c1r, double c1g, double c1b,
                                  double c2r, double c2g, double c2b, double[] lab_ref) {
        double real_min = 0;
        int rj, gj, bj;
        rj = gj = bj = 0;
        nest:
        for (int o = 0; o < I_COUNT; o++) {
            int ri, gi, bi;
            double dEi;
            double m = o / 10.0;
            double ma = max_a / 100.0;
            double[] lab_sample = new double[3];
            List<Integer> mix_i;
            mix_i = mixInverseGamma(m,
                    c1r, c1g, c1b,
                    c2r, c2g, c2b);
            ri = mix_i.get(0);
            gi = mix_i.get(1);
            bi = mix_i.get(2);

            ColorUtils.RGBToLAB(ri, gi, bi, lab_sample);
            lab_sample[0] = LAB_L_MAX;
            lab_sample[1] = lab_sample[1];
            lab_sample[2] = lab_sample[2];
            dEi = calcDE(lab_ref, lab_sample);

            if (m <= ma && dEi <= DE_MAX)
                for (int j = 0; j < I_COUNT; j++) {
                    double k = (o * 10 + j) / 100.0;
                    mix_i = mixInverseGamma(k,
                            c1r, c1g, c1b,
                            c2r, c2g, c2b);
                    ri = mix_i.get(0);
                    gi = mix_i.get(1);
                    bi = mix_i.get(2);

                    ColorUtils.RGBToLAB(ri, gi, bi, lab_sample);
                    lab_sample[0] = LAB_L_MAX;
                    lab_sample[1] = lab_sample[1];
                    lab_sample[2] = lab_sample[2];
                    dEi = calcDE(lab_ref, lab_sample);

                    if (k <= ma && dEi <= DE_MAX && ri >= 0 && gi >= 0 && bi >= 0) {
                        real_min = k;
                        rj = ri;
                        gj = gi;
                        bj = bi;
                    } else
                        return (List<Double>) new ArrayList(Arrays.asList(real_min, (double) rj, (double) gj, (double) bj));
                }
        }
        return (List<Double>) new ArrayList(Arrays.asList(real_min, (double) rj, (double) gj, (double) bj));
    }

    public List<Double> calcMinA3(double max_a, int c1r, int c1g, int c1b,
                                  int c2r, int c2g, int c2b, double[] lab_ref) {
        double real_min = 0;
        int rj, gj, bj;
        rj = gj = bj = 0;
        nest:
        for (int o = 0; o < I_COUNT; o++) {
            int ri, gi, bi;
            double dEi;
            double m = o / 10.0;
            double ma = max_a / 100.0;
            double[] lab_sample = new double[3];
            List<Integer> mix_i;
            mix_i = mixLAB(m,
                    c1r, c1g, c1b,
                    c2r, c2g, c2b);
            ri = mix_i.get(0);
            gi = mix_i.get(1);
            bi = mix_i.get(2);

            ColorUtils.RGBToLAB(ri, gi, bi, lab_sample);
            lab_sample[0] = LAB_L_MAX;
            lab_sample[1] = lab_sample[1];
            lab_sample[2] = lab_sample[2];
            dEi = calcDE(lab_ref, lab_sample);

            if (m <= ma && dEi <= DE_MAX)
                for (int j = 0; j < I_COUNT; j++) {
                    double k = (o * 10 + j) / 100.0;
                    mix_i = mixLAB(k,
                            c1r, c1g, c1b,
                            c2r, c2g, c2b);
                    ri = mix_i.get(0);
                    gi = mix_i.get(1);
                    bi = mix_i.get(2);

                    ColorUtils.RGBToLAB(ri, gi, bi, lab_sample);
                    lab_sample[0] = LAB_L_MAX;
                    lab_sample[1] = lab_sample[1];
                    lab_sample[2] = lab_sample[2];
                    dEi = calcDE(lab_ref, lab_sample);

                    if (k <= ma && dEi <= DE_MAX && ri >= 0 && gi >= 0 && bi >= 0) {
                        real_min = k;
                        rj = ri;
                        gj = gi;
                        bj = bi;
                    } else
                        return (List<Double>) new ArrayList(Arrays.asList(real_min, (double) rj, (double) gj, (double) bj));
                }
        }
        return (List<Double>) new ArrayList(Arrays.asList(real_min, (double) rj, (double) gj, (double) bj));
    }

    public List<Double> calcMinA4(double max_a, double c1r, double c1g, double c1b,
                                  double c2r, double c2g, double c2b, double[] lab_ref) {
        double real_min = 0;
        int rj, gj, bj;
        rj = gj = bj = 0;
        nest:
        for (int o = 0; o < I_COUNT; o++) {
            int ri, gi, bi;
            double dEi;
            double m = o / 10.0;
            double ma = max_a / 100.0;
            double[] lab_sample = new double[3];
            List<Integer> mix_i;
            mix_i = mixExperimentalGamma(m,
                    c1r, c1g, c1b,
                    c2r, c2g, c2b);
            ri = mix_i.get(0);
            gi = mix_i.get(1);
            bi = mix_i.get(2);

            ColorUtils.RGBToLAB(ri, gi, bi, lab_sample);
            lab_sample[0] = LAB_L_MAX;
            lab_sample[1] = lab_sample[1];
            lab_sample[2] = lab_sample[2];
            dEi = calcDE(lab_ref, lab_sample);

            if (m <= ma && dEi <= DE_MAX)
                for (int j = 0; j < I_COUNT; j++) {
                    double k = (o * 10 + j) / 100.0;
                    mix_i = mixExperimentalGamma(k,
                            c1r, c1g, c1b,
                            c2r, c2g, c2b);
                    ri = mix_i.get(0);
                    gi = mix_i.get(1);
                    bi = mix_i.get(2);

                    ColorUtils.RGBToLAB(ri, gi, bi, lab_sample);
                    lab_sample[0] = LAB_L_MAX;
                    lab_sample[1] = lab_sample[1];
                    lab_sample[2] = lab_sample[2];
                    dEi = calcDE(lab_ref, lab_sample);

                    if (k <= ma && dEi <= DE_MAX && ri >= 0 && gi >= 0 && bi >= 0) {
                        real_min = k;
                        rj = ri;
                        gj = gi;
                        bj = bi;
                    } else
                        return (List<Double>) new ArrayList(Arrays.asList(real_min, (double) rj, (double) gj, (double) bj));
                }
        }
        return (List<Double>) new ArrayList(Arrays.asList(real_min, (double) rj, (double) gj, (double) bj));
    }
}
