/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.graphics.ColorUtils;
import com.goodwy.files.R;
import me.zhanghai.android.files.util.ViewUtils;
import me.zhanghai.java.reflected.ReflectedField;

public class MoreActivityCompat {

    static {
        RestrictedHiddenApiAccess.allow();
    }

    @RestrictedHiddenApi
    private static final ReflectedField<Activity> sTaskDescriptionField = new ReflectedField<>(
            Activity.class, "mTaskDescription");

    private MoreActivityCompat() {}

    public static void setTheme(@NonNull Activity activity, @StyleRes int resid) {
        activity.setTheme(resid);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ActivityManager.TaskDescription taskDescription = sTaskDescriptionField.getObject(
                    activity);
            int appBarSurfaceColor = ViewUtils.getColorFromAttrRes(R.attr.colorAppBarSurface, 0,
                    activity);
            if (appBarSurfaceColor == 0
                    || taskDescription.getPrimaryColor() == appBarSurfaceColor) {
                return;
            }
            if (Color.alpha(appBarSurfaceColor) != 0xFF) {
                appBarSurfaceColor = ColorUtils.setAlphaComponent(appBarSurfaceColor, 0xFF);
            }
            TaskDescriptionCompat.setPrimaryColor(taskDescription, appBarSurfaceColor);
            activity.setTaskDescription(taskDescription);
        }
    }
}
