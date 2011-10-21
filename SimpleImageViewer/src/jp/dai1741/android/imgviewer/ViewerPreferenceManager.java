package jp.dai1741.android.imgviewer;

import static jp.dai1741.android.imgviewer.ImageViewerConstants.*;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.Display;

/**
 * 設定に関する処理をまとめたクラス。
 */
public enum ViewerPreferenceManager {
    INSTANCE;

    Context mContext;

    public void setContext(Context context) {
        mContext = context;
    }

    /**
     * @return {@link ActivityInfo#screenOrientation
     *         ActivityInfo.screenOrientation} で使用される定数
     */
    public int getRequestedOrientation() {

        Resources resources = mContext.getResources();
        // intにpersistすればいいんだけど、xmlからではできない？
        int orientation = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
                mContext).getString(
                resources.getString(R.string.pref_key_screen_orientation), "1"));
        return orientation == PREF_ORIENTATION_AUTO
                ? ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                : orientation == PREF_ORIENTATION_PORTRAIT
                        ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    public void setPrefferedOrientation(Activity activity) {
        activity.setRequestedOrientation(getRequestedOrientation());
    }

    /**
     * @return {@link ActivityInfo.SCREEN_ORIENTATION_PORTRAIT} or
     *         {@link ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE}
     */
    public int getInversedRequestedOrientation(Activity activity) {
        int orientation = getRequestedOrientation();
        switch (orientation) {
        case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        default:
            Display d = activity.getWindowManager().getDefaultDisplay();
            return d.getHeight() >= d.getWidth()
                    ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    public int getMaxImageSize() {
        // intで受け取れないのはなぜ？ --stringでpersistしてるからです
        // integer-array使えば？ --初期値がstringになるんだけど…
        int val = Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(mContext).getString(
                        mContext.getResources()
                                .getString(R.string.pref_key_max_imagesize), "1"));

        if (val == PREF_IMAGESIZE_CUSTOM) {
            return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
                    mContext).getString(
                    mContext.getResources().getString(
                            R.string.pref_key_custom_max_imagesize), "10000"));
        }
        else {
            return VALUES_IMAGESIZE[val];
        }

    }

}
