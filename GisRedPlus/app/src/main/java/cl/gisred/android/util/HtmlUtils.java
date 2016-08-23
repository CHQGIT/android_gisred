package cl.gisred.android.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import cl.gisred.android.R;

/**
 * Created by cramiret on 19-08-2016.
 */
public class HtmlUtils {

    private static Context mContext;
    private static String mHtml;
    static final String IMG = "img";
    static final String TXT = "txt";
    static final String CHK = "chk";
    private int iSub = 0;
    private String htmlFinal;

    public HtmlUtils(String sHtml) {
        mHtml = sHtml;
        setHtmlFinal(mHtml);
    }

    public HtmlUtils(Context context, String sHtml) {
        mContext = context;
        mHtml = sHtml;
        setHtmlFinal(mHtml);

        createStructure("css.css", R.raw.css);
        createStructure("esquema_conexion.jpg", R.raw.esquema_conexion);
    }

    public void setValueById(String sId, String sType, String sValue) {
        String sTag;
        String sTagNew;
        int iFin = 0;
        int iIni = 0;
        if (mHtml.contains(sId)) {
            int idx = mHtml.indexOf(sId);

            for (int i = idx; i < mHtml.length(); i++) {
                iSub = i + 1;
                String sChar = mHtml.substring(i, iSub);
                if (sChar.contains(">")) {
                    iFin = iSub;
                    break;
                }
            }

            for (int i = idx; i > 0; i--) {
                iSub = i - 1;
                String sChar = mHtml.substring(iSub, i);
                if (sChar.contains("<")) {
                    iIni = iSub;
                    break;
                }
            }

            sTag = mHtml.substring(iIni, iFin);

            if (sType.equals(IMG)) {
                sTagNew = replaceSrc(sTag, sValue);
            } else if (sType.equals(TXT)) {
                sTagNew = sTag + sValue;
            } else {
                sTagNew = sTag + "<b>X</b>";
            }

            setHtmlFinal(htmlFinal.replace(sTag, sTagNew));
        }
    }

    private String replaceSrc(String sTag, String sValue) {
        int iSrc = sTag.indexOf("src=");
        int contSrc = 0;

        String sNew = String.format("src=\"%s\"", sValue);

        for (int i = iSrc; i < sTag.length(); i++) {
            iSub = i + 1;
            String sChar = sTag.substring(i, iSub);
            if (sChar.contains("\"")) {
                contSrc++;
                if (contSrc > 1) {
                    break;
                }
            }
        }

        String sOld = sTag.substring(iSrc, iSub);
        sTag = sTag.replace(sOld, sNew);

        return sTag;
    }

    public static void createPathInspeccion(Context myContext) throws Exception {
        if (myContext != null) {
            String path = String.format("%s/insp/", myContext.getExternalCacheDir().getAbsolutePath());
            File tempDir = new File(path);
            if (!tempDir.exists()) {
                tempDir.mkdir();
            } else {
                File[] archivos = tempDir.listFiles();
                for (File archivo : archivos)
                    archivo.delete();
            }
        }
    }

    private void createStructure(String sName, int resource){
        try
        {
            String[] nomFile = sName.split("\\.");
            File oFile = PhotoUtils.createFile(nomFile[0], nomFile[1], mContext);
            oFile.createNewFile();

            InputStream fraw = mContext.getResources().openRawResource(resource);
            byte[] b = new byte[fraw.available()];
            fraw.read(b);
            String sHtml = new String(b);

            //OutputStreamWriter fout = new OutputStreamWriter(mContext.openFileOutput(oFile.getPath(), Context.MODE_PRIVATE));
            FileOutputStream fout = new FileOutputStream(oFile);

            fout.write(b);
            fout.close();
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al escribir fichero a memoria interna");
        }
    }

    public void createHtml(String sData){
        try
        {
            File oFile = PhotoUtils.createFile("index", "html", mContext);
            oFile.createNewFile();

            FileOutputStream fout = new FileOutputStream(oFile);

            fout.write(sData.getBytes());
            fout.close();
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al escribir fichero a memoria interna");
        }
    }

    public String getHtmlFinal() {
        return htmlFinal;
    }

    public void setHtmlFinal(String htmlFinal) {
        this.htmlFinal = htmlFinal;
    }
}
