package com.tomclaw.appsend.util;

import static com.tomclaw.appsend.util.ColorsKt.getAttributedColor;

import android.app.Activity;

import com.jaeger.library.StatusBarUtil;
import com.tomclaw.appsend.R;

/**
 * Created by ivsolkin on 21.09.16.
 */
public class ThemeHelper {

    public static boolean updateTheme(Activity activity) {
        boolean isDarkTheme = PreferenceHelper.isDarkTheme(activity);
        activity.setTheme(isDarkTheme ? R.style.AppThemeBlack : R.style.AppTheme);
        return isDarkTheme;
    }

}
