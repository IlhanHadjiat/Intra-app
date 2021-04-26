package com.example.intrapro;

import android.graphics.Bitmap;

import org.json.JSONObject;

public interface VolleyCallback {
    JSONObject jsonSuccess(JSONObject result);
    Bitmap imageSuccess(Bitmap image);
}
