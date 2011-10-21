package jp.dai1741.android.imgviewer;

import static jp.dai1741.android.imgviewer.ImageViewerConstants.*;

import jp.dai1741.android.view.PinchableImageView;
import jp.dai1741.util.FileIterator;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;

/**
 * 画像を表示するアクティビティ。
 * ファイル管理などをこのクラスで行い、
 * 画像に対する大体の処理は {@link PinchableImageView} で行う。
 * 
 * TODO: 例外の捕捉
 */
public class ImageActivity extends AbstractActivity {

    private static final String TAG = "ImageActivity";
    private static final String KEY_CURRENT_PATH = "current_path";
    private static final String KEY_CURRENT_ZOOM = "current_zoom";
    private static final String KEY_ORIGINAL_WIDTH = "original_width";
    private static final String KEY_ORIGINAL_HEIGHT = "original_height";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageactivity);

        File f;
        String bundledPath = savedInstanceState != null ? savedInstanceState
                .getString(KEY_CURRENT_PATH) : null;
        if (bundledPath == null) {
            Uri uri = getIntent().getData();
            // resolveの仕方：http://groups.google.com/group/android-group-japan/browse_thread/thread/84c6d026460c6ca6
            if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                Cursor c = getContentResolver().query(uri, null, null, null, null);
                if (!c.moveToFirst()) throw new IllegalStateException(
                        "couldn't resolve the content uri:" + uri);
                f = new File(c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA)));
            }
            else if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
                f = new File(uri.getPath());
            }
            else {
                throw new IllegalStateException("unsupported scheme: " + uri);
            }
        }
        else {
            f = new File(bundledPath);
        }

        Bitmap retainedBitmap = (Bitmap) getLastNonConfigurationInstance();
        // onRetainNonConfigurationInstance()でBitmapをいれているよ

        // TODO:設定で変更可能に
        PinchableImageView.ZoomState initialZoom = PinchableImageView.ZoomState.FIT_TO_SHORT_WIDTH;
        if (savedInstanceState != null) {
            String zoomString = savedInstanceState.getString(KEY_CURRENT_ZOOM);
            if (zoomString != null) initialZoom = PinchableImageView.ZoomState
                    .valueOf(zoomString);
        }

        ((ViewGroup) findViewById(R.id.layout_root))
                .addView(mImageView = new PinchableImageView(this, retainedBitmap,
                        initialZoom));

        String bitmapPath;
        if (f.isFile()) {
            mFileIterator = FileIterator.inDirectoryOf(f, IMAGE_FILTER,
                    FileIterator.FileComparators.NATURAL);
            bitmapPath = f.toString();
        }
        else if (f.isDirectory()) {
            mFileIterator = FileIterator.in(f, IMAGE_FILTER,
                    FileIterator.FileComparators.NATURAL);
            bitmapPath = mFileIterator.hasNext() ? mFileIterator.next().toString() : null;
            if (LOG_D) {
                Log.v(TAG, "given file is directory. resolved image path: " + bitmapPath);
            }
        }
        else {
            mFileIterator = DUMMY_ITERATOR;
            bitmapPath = null;
        }
        if (retainedBitmap == null) {
            mBitmapMakerTask = new BitmapMakerTask().execute(bitmapPath);
        }
        else {
            mLastLoadedImageOriginalWidth = savedInstanceState.getInt(KEY_ORIGINAL_WIDTH);
            mLastLoadedImageOriginalHeight = savedInstanceState.getInt(KEY_ORIGINAL_HEIGHT);
        }

        mImageView.setAlphaChangeRate(ALPHA_CHANGE_RATE);

        mImageView.setOnTouchListener(new View.OnTouchListener() {

            float initialZoom;
            float initialX;
            float initialY;

            /*
             * (non-Javadoc) TODO:小さい画像だと誤判定する
             * TODO:色々と酷いので {@link GestureDetector} とか使う
             */
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & ~MotionEvent.ACTION_POINTER_ID_MASK) {
                // 何かがおかしいswitch文

                case MotionEvent.ACTION_DOWN:
                    initialZoom = mImageView.getZoomRate();
                    initialX = event.getX();
                    initialY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    if (mPreparingBitmap) {
                        if (LOG_D) {
                            Log.v(TAG, "Gesture ignored because of bitmap being prepared");
                        }
                        break;
                    }
                    float diffX = (event.getX() - initialX) / v.getWidth();
                    float diffY = (event.getY() - initialY) / v.getHeight();

                    RectF lengths = mImageView.getBounds();

                    boolean swipingLeft = diffX > MIN_TRANSISION_RATE
                            && lengths.left < -v.getWidth() * MIN_TRANSISION_RATE;
                    boolean swipingRight = -diffX > MIN_TRANSISION_RATE
                            && lengths.right - mImageView.getBitmap().getWidth() > v
                                    .getWidth() * MIN_TRANSISION_RATE;
                    boolean swipingTop = diffY > MIN_TRANSISION_RATE
                            && lengths.top < -v.getHeight() * MIN_TRANSISION_RATE;
                    boolean swipingBottom = -diffY > MIN_TRANSISION_RATE
                            && lengths.bottom - mImageView.getBitmap().getHeight() > v
                                    .getHeight() * MIN_TRANSISION_RATE;

                    if (initialZoom == mImageView.getZoomRate()
                            && (swipingLeft ^ swipingRight || swipingTop ^ swipingBottom)) {
                        if (swipingLeft || swipingTop) {
                            goPreviousImage();
                        }
                        else {
                            goNextImage();
                        }
                    }
                    break;
                }
                return false;
            }
        });
    }

    private static final FileFilter IMAGE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            // 面倒なので拡張子で判断
            // もうちょっとどうにかしたい
            String filename = pathname.getName();
            filename = filename.substring(Math.max(0, filename.length() - 5))
                    .toLowerCase();
            return filename.endsWith(".png") || filename.endsWith(".jpg")
                    || filename.endsWith(".jpeg") || filename.endsWith(".gif");
        }
    };

    // Collections.emptyList()の型推論はメソッドには効かない
    @SuppressWarnings("unchecked")
    static final FileIterator DUMMY_ITERATOR = FileIterator.of(Collections.EMPTY_LIST,
            null);

    private void goNextImage() {
        if (mFileIterator.hasNext()) {
            changeImage(mFileIterator.next().toString());
        }
        else {
            Toast.makeText(this, R.string.toast_last_file, Toast.LENGTH_SHORT).show();
        }
    }

    private void goPreviousImage() {
        if (mFileIterator.hasPrevious()) {
            changeImage(mFileIterator.previous().toString());
        }
        else {
            Toast.makeText(this, R.string.toast_first_file, Toast.LENGTH_SHORT).show();
        }
    }

    protected volatile boolean mPreparingBitmap;
    protected final Bitmap EMPTY_BITMAP = PinchableImageView.createEmptyBitmap();
    protected AsyncTask<?, ?, ? extends Bitmap> mBitmapMakerTask;

    private void changeImage(String path) {
        mImageView.setBitmap(EMPTY_BITMAP, true);
        // ここにウェイト用のアイコンを入れる処理・・

        mBitmapMakerTask = new BitmapMakerTask().execute(path);
    }

    class BitmapMakerTask extends AsyncTask<String, Boolean, Bitmap> {

        @Override
        protected void onPreExecute() {
            mPreparingBitmap = true;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap ret = createSizeLimitedBitmap(params[0]);
            if (ret == null && !isCancelled()) {
                publishProgress(true);
                ret = BitmapFactory.decodeResource(getResources(),
                        android.R.drawable.ic_menu_close_clear_cancel);
                if (ret == null) {
                    throw new InternalError(
                            "failed to decode a premade drawable that is supposed to");
                }
            }
            return ret;
        }

        @Override
        protected void onProgressUpdate(Boolean... failed) {
            if (failed[0]) {
                Toast.makeText(ImageActivity.this, R.string.error_image_load_failed,
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mImageView.setBitmap(result, false);
            mPreparingBitmap = false;
        }
    }

    /**
     * http://www.hoge256.net/2009/08/432.html ここを参考に作成。ほぼコピペ
     * 処理が重いのでUIスレッドから呼ぶのはやめよう
     * 
     * @param filePath null可
     * @return 作成したbitmap、もしくはnull
     */
    private Bitmap createSizeLimitedBitmap(String filePath) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, op);
        if (op.outWidth == -1 || op.outHeight == -1) return null;

        int maxAllowedImageVolume = ViewerPreferenceManager.INSTANCE.getMaxImageSize();

        mLastLoadedImageOriginalWidth = op.outWidth;
        mLastLoadedImageOriginalHeight = op.outHeight;
        int volume = op.outWidth * op.outHeight;
        op.inSampleSize = 1 + (maxAllowedImageVolume > 0
                ? volume / maxAllowedImageVolume
                : 0);
        op.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, op);

    }

    int mLastLoadedImageOriginalWidth;
    int mLastLoadedImageOriginalHeight;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFileIterator != null && mFileIterator.hasCurrent()) {
            outState.putString(KEY_CURRENT_PATH, mFileIterator.current()
                    .getAbsolutePath());
        }
        if (mImageView != null) {
            outState.putString(KEY_CURRENT_ZOOM, mImageView.getZoomState().toString());
        }
        //Point使いたいけど、API Level 8ではParcelableじゃない…
        outState.putInt(KEY_ORIGINAL_WIDTH, mLastLoadedImageOriginalWidth);
        outState.putInt(KEY_ORIGINAL_HEIGHT, mLastLoadedImageOriginalHeight);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mImageView != null ? mImageView.getBitmap() : null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmapMakerTask != null) {
            mBitmapMakerTask.cancel(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.imageactivity, menu);
        return true;
    }

    static boolean orientationInversed = false;
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_set_preferences:
            Intent intent = new Intent(this, ViewerPreferenceActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SETTING_UPDATE);
            break;

        case R.id.menu_change_orientation:
            setRequestedOrientation(orientationInversed 
                    ? ViewerPreferenceManager.INSTANCE.getRequestedOrientation()
                    : ViewerPreferenceManager.INSTANCE.getInversedRequestedOrientation(this));
            orientationInversed = !orientationInversed;
            break;
        case R.id.menu_show_property:
            String str;
            if (mFileIterator.size() != 0) {
                File file = mFileIterator.current();
                str = String.format(getResources().getString(R.string.file_property),
                        file.getName(), (int) (file.length() + 999) / 1000,
                        String.format(
                                getResources().getString(R.string.image_resolution),
                                mLastLoadedImageOriginalWidth,
                                mLastLoadedImageOriginalHeight), file.getParent(),
                        String.format(
                                getResources().getString(R.string.image_resolution),
                                mImageView.getBitmap().getWidth(), mImageView.getBitmap()
                                        .getHeight()),
                        mFileIterator.getCurrentIndex() + 1, mFileIterator.size());
            }
            else {
                str = String.format(
                        getResources().getString(R.string.error_no_file_in_directory),
                        "未実装です");
            }
            new AlertDialog.Builder(this)
                    .setMessage(str)
                    .setPositiveButton(R.string.button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
            // TODO:コピペできるようにする
            break;
        default:
            break;
        }
        return false;
    }

    PinchableImageView mImageView;
    FileIterator mFileIterator;

}
