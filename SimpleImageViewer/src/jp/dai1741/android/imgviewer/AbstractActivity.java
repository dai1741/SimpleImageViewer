package jp.dai1741.android.imgviewer;

import static jp.dai1741.android.imgviewer.ImageViewerConstants.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public abstract class AbstractActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            ViewerPreferenceManager.INSTANCE.setPrefferedOrientation(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTING_UPDATE && resultCode == RESULT_OK) {
            ViewerPreferenceManager.INSTANCE.setPrefferedOrientation(this);
        }
    }

}
