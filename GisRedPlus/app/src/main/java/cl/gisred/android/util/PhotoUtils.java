package cl.gisred.android.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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

    public static File createFileExternal(String name, String ext, Context myContext) throws Exception {
        if (myContext != null) {
            String path = myContext.getExternalFilesDir(null).getAbsolutePath() + "/insp/";
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

    public static void createFirma(Bitmap sData, Context myContext, String sNom) throws Exception {
        try
        {
            File oFile = PhotoUtils.createFile(sNom, "jpg", myContext);
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
            Toast.makeText(myContext, "No es posible guardar la firma, si el problema persiste contacte soporte", Toast.LENGTH_SHORT).show();
        }
    }

    public static void copyFile(File src, File dst) throws IOException
    {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    public void copyToGallery(Uri uri) {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fecha = sdf.format(Calendar.getInstance().getTime());
            String pathtoimage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();

            File oImage = new File(uri.getPath());

            File tempDir = new File(pathtoimage);
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }

            File oFile = new File(tempDir, String.format("%s.%s", fecha, "jpg"));
            oFile.createNewFile();

            copyFile(oImage, oFile);
        }
        catch (Exception ex)
        {
            Toast.makeText(mContext, "No es posible acceder a la imagen, intente nuevamente", Toast.LENGTH_SHORT).show();
        }
    }

    public void copyFromGallery(Uri uri, String name) {
        try
        {
            File oImage = new File(getRealPathFromURI_API19(mContext, uri));

            File oFile = PhotoUtils.createFile(name, "jpg", mContext);
            oFile.createNewFile();

            copyFile(oImage, oFile);
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al escribir fichero a memoria interna: " +ex.getMessage());
        }
    }

    public Bitmap getImage(Uri uri, int scale) {
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
        return scaleImage(options, uri, scale);
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
            // Parche funcional para evitar escalado
            if (targetWidth != 300){
                if (sample > 1) bitmap = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
            }
            is.close();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        if (uri.getHost().contains("com.android.providers.media")) {
            // Image pick from recent
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Images.Media.DATA};

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
            return filePath;
        }
        return filePath;
    }
}
