package jp.dai1741.android.imgviewer;

import static jp.dai1741.android.imgviewer.ImageViewerConstants.*;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * ランチャーから開かれるアクティビティ。
 * 基本的には使わないがエクスプローラーを呼んだりできる。
 * 
 * @author dai
 */
public class Main extends AbstractActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void onSelectFileButtonClick(View view) {
        //本当はpickしたいわけじゃないけど、ACTION_VIEWだとエクスプローラーがヒットしない
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        if (getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, R.string.error_no_explorer_found, Toast.LENGTH_LONG)
                    .show();
        }
        else startActivityForResult(intent, REQUEST_CODE_IMAGE);
    }

    public void onOpenFileButtonClick(View view) {
        String uriString = ((EditText) findViewById(R.id.edittext_file_path)).getText()
                .toString();
        if (uriString.length() == 0) {
            Toast.makeText(this, R.string.error_file_path_empty, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        Uri uri = Uri.parse(uriString);
        if (uri.isRelative()) {
            uri = uri.buildUpon().scheme(ContentResolver.SCHEME_FILE).build();
        }
        if (uri.isHierarchical() && uri.isAbsolute()) {
            Intent intent = new Intent(Main.this, ImageActivity.class);
            intent.setData(uri);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, R.string.error_invalid_file_path, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
    }

    public void onGoSettingsButtonClick(View view) {
        Intent intent = new Intent(this, ViewerPreferenceActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SETTING_UPDATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK
                && data.getData() != null) {
            Intent intent = new Intent(this, ImageActivity.class);
            intent.setData(data.getData());
            startActivity(intent);
        }
    }


}