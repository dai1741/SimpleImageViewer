package jp.dai1741.android.imgviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

public interface BitmapDecoder {
    
    Bitmap decode(BitmapFactory.Options op) throws IOException;
    
    public static class FileBitmapDecoder implements BitmapDecoder {

        private final String mFilePath;
        
        public FileBitmapDecoder(String filePath) {
            mFilePath = filePath;
        }

        @Override
        public Bitmap decode(Options op) {
            return BitmapFactory.decodeFile(mFilePath, op);
        }
        
    }
    
    public static class ContentBitmapDecoder implements BitmapDecoder {

        private final Context mContext;
        private final Uri mContentUri;
        
        public ContentBitmapDecoder(Context context, Uri contentUri) {
            mContext = context;
            mContentUri = contentUri;
        }

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
