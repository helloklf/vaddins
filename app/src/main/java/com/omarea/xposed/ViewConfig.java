package com.omarea.xposed;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import android.widget.ScrollView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by Hello on 2018/03/02.
 */

public class ViewConfig {
    static float mDensity = -1;
    static final float MULTIPLIER_SCROLL_FRICTION = 10000f;

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        hookViewConfiguration(ViewConfiguration.class);
        hookScrollbarNoFading(ViewConfiguration.class);
        hookOverscrollDistance(ViewConfiguration.class);
        hookOverflingDistance(ViewConfiguration.class);
        hookMaxFlingVelocity(ViewConfiguration.class);
        hookScrollFriction(ViewConfiguration.class);

        // TODO velocity 0 to 100000 / def 8000 // try 2000
        // overscroll dist 0 to 1000 / def 0
        // overfling dist 0 to 1000 / def 6
        // friction * 10000 // 0 to 2000 //def 150 // try 50

        //overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent)
        /*
        XposedBridge.hookAllMethods(View.class, "overScrollBy", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (param.args.length == 9) {
                    //mMaxOverScrollY
                    param.args[6] = 200;
                    param.args[7] = 400;
                }
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "overScrollBy",
                int.class,int.class,int.class,int.class,int.class,int.class,int.class,int.class,boolean.class , new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                //ListView
            }
        });
        */
    }

    private static void hookViewConfiguration(final Class<?> clazz) {
        XposedBridge.hookAllConstructors(clazz, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args == null) return;
                // Not null means there's a context for density scaling
                Context context = (Context) param.args[0];
                final Resources res = context.getResources();
                final float density = res.getDisplayMetrics().density;
                if (res.getConfiguration().isLayoutSizeAtLeast(
                        Configuration.SCREENLAYOUT_SIZE_XLARGE)) {
                    mDensity = density * 1.5f;
                } else {
                    mDensity = density;
                }
            }
        });
    }

    private static void hookMaxFlingVelocity(final Class<?> clazz) {
        XposedBridge.hookAllMethods(clazz, "getMaximumFlingVelocity", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!isEnabled()) return;
                param.setResult(Common.DEFAULT_SCROLLING_VELOCITY);
            }
        });

        XposedBridge.hookAllMethods(clazz, "getScaledMaximumFlingVelocity", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!isEnabled()) return;
                final int max_velocity = Common.DEFAULT_SCROLLING_VELOCITY;
                if (mDensity == -1) {
                    param.setResult(max_velocity);
                } else {
                    final int scaled_velocity = (int) (mDensity * max_velocity + 0.5f);
                    param.setResult(scaled_velocity);
                }
            }
        });
    }

    private static void hookScrollFriction(final Class<?> clazz) {
        XposedBridge.hookAllMethods(clazz, "getScrollFriction", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!isEnabled()) return;
                final int raw_friction = Common.DEFAULT_SCROLLING_FRICTION;
                final float actual_friction = ((float)raw_friction) / MULTIPLIER_SCROLL_FRICTION;
                param.setResult(actual_friction);
            }
        });
    }

    private static void hookOverscrollDistance(final Class<?> clazz) {
        XposedBridge.hookAllMethods(clazz, "getScaledOverscrollDistance", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!isEnabled()) return;
                final int overscroll_distance = Common.DEFAULT_SCROLLING_OVERSCROLL;
                if (mDensity == -1) {
                    param.setResult(overscroll_distance);
                } else {
                    final int scaled_dist = (int) (mDensity * overscroll_distance + 0.5f);
                    param.setResult(scaled_dist);
                }
            }
        });
    }

    private static void hookOverflingDistance(final Class<?> clazz) {
        XposedBridge.hookAllMethods(clazz, "getScaledOverflingDistance", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!isEnabled()) return;
                final int overfling = Common.DEFAULT_SCROLLING_OVERFLING;
                if (mDensity == -1) {
                    param.setResult(overfling);
                } else {
                    final int scaled_dist = (int) (mDensity * overfling + 0.5f);
                    param.setResult(scaled_dist);
                }
            }
        });
    }

    private static void hookScrollbarNoFading(final Class<?> clazz) {
        XposedBridge.hookAllConstructors(View.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!(param.thisObject instanceof ScrollView) &&
                        !(param.thisObject instanceof ListView)) {
                    // If we set scrollbar for non-scrollable views, we will bootloop
                    return;
                }

                if (!isEnabled()) return;
                if (!Common.DEFAULT_SCROLLING_NO_FADING) return;

                final View that = (View) param.thisObject;

                final Object scroll_cache = XposedHelpers
                        .findField(that.getClass(), "mScrollCache").get(that);
                if (scroll_cache == null) return;
                // If null, the ScrollView/ListView is not scrollable
                // in the first place (poorly written apps)

                final Object scroll_bar_drawable = XposedHelpers.findField(scroll_cache.getClass(),
                        "scrollBar").get(scroll_cache);
                if (scroll_bar_drawable == null) return;
                // If null, the ScrollView/ListView is not currently scrollable
                // (when screen is big enough to show all contents)
                // Don't continue, else the app will have NullPointerException.
                // Apparently, Google didn't catch this in the first place

                that.setScrollbarFadingEnabled(false);
            }
        });

    }

    private static boolean isEnabled() {
        return Common.DEFAULT_SCROLLING_ENABLE;
    }


    public class Common {
        //是否启用
        static final boolean DEFAULT_SCROLLING_ENABLE = true;
        //
        static final boolean DEFAULT_SCROLLING_NO_FADING = false;
        //滚动溢出最大距离
        static final int DEFAULT_SCROLLING_OVERSCROLL = 160;
        //滚动回弹距离
        static final int DEFAULT_SCROLLING_OVERFLING = 160;
        //滚动惯性
        static final int DEFAULT_SCROLLING_FRICTION = 256;
        //最大滚动速度
        static final int DEFAULT_SCROLLING_VELOCITY = 4000;
    }
}
