package com.inbody2014.inbody;

import android.content.Context;
import android.widget.Toast;

public class UIUtils {

    private static Context mContext;

    public static void initContext(Context context) {

        mContext = context;
    }

    public static void runOnUIToast(String str){
        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();

    }
}