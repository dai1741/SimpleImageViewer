package jp.dai1741.android.imgviewer;

/**
 * 定数クラス。
 * 
 * @author dai
 */
public enum ImageViewerConstants {
    ;


    static final int ALPHA_CHANGE_RATE = 51;

    /**
     * 画面遷移に必要な最小のフリック量
     * TODO: 設定可能にする
     */
    public static final float MIN_TRANSISION_RATE = 1f / 4;

    static final int PREF_ORIENTATION_AUTO = 0;
    static final int PREF_ORIENTATION_PORTRAIT = 1;
    static final int PREF_ORIENTATION_LANDSCAPE = 2;

    static final int PREF_IMAGESIZE_CUSTOM = 3;

    static final int[] VALUES_IMAGESIZE = {
            750 * 750,
            1000 * 1000,
            2000 * 2000,
    };


    public static final int REQUEST_CODE_IMAGE = 1;
    public static final int REQUEST_CODE_SETTING_UPDATE = 2;
    
    public static final boolean LOG_D = true;


}
