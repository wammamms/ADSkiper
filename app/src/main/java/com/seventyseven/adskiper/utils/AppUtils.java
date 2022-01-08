package com.seventyseven.adskiper.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class AppUtils {

    public static String getAppName(Context context, String pkg){
        PackageManager pm = context.getPackageManager();
        String name = "";
        try{
            PackageInfo info = pm.getPackageInfo(pkg,0);
            if(info != null){
                name = String.valueOf(info.applicationInfo.loadLabel(pm));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return name;
    }

    public static Drawable getDrawable(Context context, String pkg){
        PackageManager pm = context.getPackageManager();
        try{
            //获取包名对应的App图标
            Drawable icon = pm.getApplicationIcon(pkg);
            return icon;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
