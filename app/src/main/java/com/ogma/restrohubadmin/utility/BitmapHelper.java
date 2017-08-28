package com.ogma.restrohubadmin.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by User on 24-01-2017.
 */

public class BitmapHelper {

    private Context context;

    private BitmapHelper(Context context) {
        this.context = context;
    }


    public static BitmapHelper getInstance(Context context) {
        return new BitmapHelper(context);
    }


    public File saveCompressedBitmap(Bitmap bitmap, int quality, String path) {
        File imageFile = new File(path);

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }
        return imageFile;
    }

    public int findOrientation(File file) {
        int orientation = 0;
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(file.getAbsolutePath());
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.e("Orientation", orientation + "");
        } catch (IOException e) {
            e.printStackTrace();
            orientation = -1;
            Toast.makeText(context, "File does not exists..!", Toast.LENGTH_SHORT).show();
        }
        return orientation;
    }

    public Bitmap rotateBitmap(Bitmap source, int orientation) {
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                if (source != null)
                    return RotateBitmap(source, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                if (source != null)
                    return RotateBitmap(source, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                if (source != null)
                    return RotateBitmap(source, 270);
            default:
                return source;
        }
    }

    private Bitmap RotateBitmap(Bitmap source, float angle) {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            Toast.makeText(context, "The resolution of the image is too high. Please try with smaller image..!", Toast.LENGTH_SHORT).show();
        }
        return source;
    }
}
