package com.ogma.restrohubadmin.utility;

import android.net.Uri;
import android.os.Environment;

import com.ogma.restrohubadmin.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by User on 24-01-2017.
 */

public class AppFileManager {

    public static synchronized File makeAppDir(String appName) {
        File storageDir = Environment.getExternalStorageDirectory();

        File appDir = new File(storageDir + "/" + appName);

        boolean exists = appDir.exists();

        if (!exists)
            exists = appDir.mkdir();
        if (exists)
            return appDir;

        return null;
    }

    public static synchronized File createImageFile(String appName) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File appDir = makeAppDir(appName);

        if (appDir != null) {
            return File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    appDir      /* directory */
            );
        }
        return null;
    }

    public static synchronized File copyFileToAppDir(String appName, Uri fileUri) throws Exception {
        File appDir = makeAppDir(appName);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_" + fileUri.getLastPathSegment();

        File saveFile = new File(appDir, imageFileName);

        FileInputStream inStream = new FileInputStream(new File(fileUri.getPath()));
        FileOutputStream outStream = new FileOutputStream(saveFile);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();

        return saveFile;
    }
}
