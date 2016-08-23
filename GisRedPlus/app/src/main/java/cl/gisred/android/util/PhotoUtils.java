package cl.gisred.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cramiret on 16-08-2016.
 */
public class PhotoUtils {
    private static Context mContext;
    private BitmapFactory.Options generalOptions;

    public PhotoUtils(Context context) {
        mContext = context;
    }

    public static File createFile(String name, String ext, Context myContext) throws Exception {
        if (myContext != null) {
            String path = myContext.getExternalCacheDir().getAbsolutePath() + "/insp/";
            File tempDir = new File(path);
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }

            return new File(tempDir, String.format("%s.%s", name, ext));
        }
        else {
            return null;
        }
    }

    public static File createTemporaryFile(String part, String ext, Context myContext) throws Exception {
        if (myContext != null) {
            String path = myContext.getExternalCacheDir().getAbsolutePath() + "/insp/";
            File tempDir = new File(path);
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }

            return File.createTempFile(part, ext, tempDir);
        }
        else {
            return null;
        }
    }

    public static void createFirma(Bitmap sData, Context myContext) throws Exception {
        try
        {
            File oFile = PhotoUtils.createFile("firma", "jpg", myContext);
            oFile.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            sData.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fout = new FileOutputStream(oFile);

            fout.write(bitmapdata);
            fout.close();
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al escribir fichero a memoria interna");
        }
    }

    public Bitmap getImage(Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream is = null;
        try {
            is = mContext.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(is, null, options);
            is.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.generalOptions = options;
        return scaleImage(options, uri, 250);
    }

    public static int nearest2pow(int value) {
        return value == 0 ? 0
                : (32 - Integer.numberOfLeadingZeros(value - 1)) / 2;
    }

    public Bitmap scaleImage(BitmapFactory.Options options, Uri uri,
                             int targetWidth) {
        if (options == null)
            options = generalOptions;
        Bitmap bitmap = null;
        double ratioWidth = ((float) targetWidth) / (float) options.outWidth;
        double ratioHeight = ((float) targetWidth) / (float) options.outHeight;
        double ratio = Math.min(ratioWidth, ratioHeight);
        int dstWidth = (int) Math.round(ratio * options.outWidth);
        int dstHeight = (int) Math.round(ratio * options.outHeight);
        ratio = Math.floor(1.0 / ratio);
        int sample = nearest2pow((int) ratio);

        Log.w("scaleImage", String.format("W: %s H: %s", dstWidth, dstHeight));

        options.inJustDecodeBounds = false;
        if (sample <= 0) {
            sample = 1;
        }
        options.inSampleSize = (int) sample;
        options.inPurgeable = true;
        try {
            InputStream is;
            is = mContext.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is, null, options);
            if (sample > 1) bitmap = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
            is.close();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmap;
    }

}
