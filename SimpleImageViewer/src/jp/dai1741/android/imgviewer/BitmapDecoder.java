package jp.dai1741.android.imgviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

/**
 * ビットマップを生成する情報を保持し、適宜デコードを行うインターフェース。
 */
public interface BitmapDecoder {

    /**
     * 保持している情報をデコードし、ビットマップを作成する。
     * 
     * @param op
     * @return ビットマップかnull
     * @throws IOException
     */
    Bitmap decode(BitmapFactory.Options op) throws IOException;

    /**
     * ファイルをデコードする{@code BitmapDecoder}。
     */
    public static class FileBitmapDecoder implements BitmapDecoder {

        private final String mFilePath;

        /**
         * @param filePath
         *            null可
         */
        public FileBitmapDecoder(String filePath) {
            mFilePath = filePath;
        }

        @Override
        public Bitmap decode(Options op) {
            return BitmapFactory.decodeFile(mFilePath, op);
        }

    }

    /**
     * {@code ContentResolver}が解釈するuriをデコードする{@code BitmapDecoder}。
     */
    public static class ContentBitmapDecoder implements BitmapDecoder {

        private final Context mContext;
        private final Uri mContentUri;

        public ContentBitmapDecoder(Context context, Uri contentUri) {
            mContext = context;
            mContentUri = contentUri;
        }

        /**
         * @throws IOException
         *             contentUriが不正のとき
         */
        @Override
        public Bitmap decode(Options op) throws IOException {
            InputStream stream;
            stream = mContext.getContentResolver().openInputStream(mContentUri);
            Bitmap ret = BitmapFactory.decodeStream(stream, null, op);
            stream.close();
            return ret;
        }

    }
}
