package com.malin.hook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
public class HookPMS {


    public static Object getPackageManager() {
        Object sPackageManager = null;
        try {
            //1.获取ActivityThread的值
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");

            //public static ActivityThread currentActivityThread() {return sCurrentActivityThread;}
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object activityThread = currentActivityThreadMethod.invoke(null);

            //2.获取ActivityThread里面原始的 sPackageManager
            //static IPackageManager sPackageManager;
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            sPackageManager = sPackageManagerField.get(activityThread);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return sPackageManager;
    }


    public static void resetPackageManager(Object object) {
        Object sPackageManager;
        try {
            //1.获取ActivityThread的值
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");

            //public static ActivityThread currentActivityThread() {return sCurrentActivityThread;}
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object activityThread = currentActivityThreadMethod.invoke(null);

            //2.获取ActivityThread里面原始的 sPackageManager
            //static IPackageManager sPackageManager;
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            sPackageManager = sPackageManagerField.get(activityThread);
            sPackageManagerField.set(sPackageManager, object);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void resetApplicationPackageManager(Context context, Object object) {
        try {
            //1.获取 ApplicationPackageManager里面的 mPM对象
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            //PackageManager的实现类ApplicationPackageManager中的mPM
            //private final IPackageManager mPM;
            Field mPmField = packageManager.getClass().getDeclaredField("mPM");
            mPmField.setAccessible(true);
            mPmField.set(packageManager, object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

}
