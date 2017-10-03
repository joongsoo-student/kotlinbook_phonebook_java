package kr.devdogs.kotlinbook.phonebookjava.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Daniel on 2017. 9. 24..
 */

public class BitmapUtils {
    public static Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate((float)degrees, (float)bitmap.getWidth() / 2,
                    (float)bitmap.getHeight() / 2);

            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch (OutOfMemoryError err) {
            }

        }
        return bitmap;
    }

    public static String saveBitmap(Bitmap bitmap) {
        String fileName = System.currentTimeMillis() + ".jpg";
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/phonebook/" + fileName);
        if(!dir.getParentFile().exists()) {
            dir.getParentFile().mkdirs();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(dir);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                try { out.close(); } catch(IOException ioe){}
            }
        }


        return dir.getPath();
    }
}
