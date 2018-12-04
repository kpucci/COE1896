package com.coe1896.puckperfectapp.DataAnalysis;

import android.content.Context;

public interface PuckPerfectData {

    void storeData(String input);
    void storeData(byte[] buffer);
    String printLastDataPoint();
    void exportToFile(Context context);
    void clear();
    boolean isEmpty();
}
