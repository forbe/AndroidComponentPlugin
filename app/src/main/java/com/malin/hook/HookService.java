package com.malin.hook;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 代码参考《Android进阶解密》
 */
@SuppressWarnings("JavaReflectionMemberAccess")
@SuppressLint("PrivateApi")
public class HookService {

    public static void hookAMSForService(Context context, Class<?> proxyServiceClass) throws Exception {
        Object IActivityManagerSingletonObj;

        //1.IActivityManagerSingleton
        if (Build.VERSION.SDK_INT >= 26) {
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
            Field iActivityManagerSingletonField = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
            iActivityManagerSingletonField.setAccessible(true);
            IActivityManagerSingletonObj = iActivityManagerSingletonField.get(null);
        } else {
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            IActivityManagerSingletonObj = gDefaultField.get(null);
        }

        //2.IActivityManager
        Class<?> singletonClass = Class.forName("android.util.Singleton");
        Field mInstanceField = singletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        Object iActivityManagerObj = mInstanceField.get(IActivityManagerSingletonObj);


        //3. IActivityManagerProxy
        Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");

        Object proxy = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{iActivityManagerClass},
                new IActivityManagerProxy(context, iActivityManagerObj, proxyServiceClass)
        );

        //4.重新设置新值
        mInstanceField.set(IActivityManagerSingletonObj, proxy);
    }

    private static class IActivityManagerProxy implements InvocationHandler {
        private Context mContext;
        private Object mActivityManager;
        private Class<?> mStubServiceClass;
        private static final String TAG = "IActivityManagerProxy";

        public IActivityManagerProxy(Context context, Object activityManager, Class<?> proxyServiceClass) {
            mContext = context;
            mActivityManager = activityManager;
            mStubServiceClass = proxyServiceClass;

        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public Object invoke(Object o, Method method, Object[] args) throws Throwable {
            if ("startService".equals(method.getName())) {
                Intent intent;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        index = i;
                        break;
                    }
                }
                intent = (Intent) args[index];
                Intent proxyIntent = new Intent(mContext, mStubServiceClass);
                proxyIntent.putExtra(ProxyService.TARGET_SERVICE, intent.getComponent().getClassName());
                args[index] = proxyIntent;
                Log.d(TAG, "Hook成功");
            }
            return method.invoke(mActivityManager, args);
        }
    }
}
