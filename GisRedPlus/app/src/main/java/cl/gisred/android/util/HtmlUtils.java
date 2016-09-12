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
    private String path;

    public HtmlUtils(String sHtml) {
        mHtml = sHtml;
        setHtmlFinal(mHtml);
    }

    public HtmlUtils(Context context, String sHtml) {
        mContext = context;
        mHtml = sHtml;
        setHtmlFinal(mHtml);

        setPath(String.format("%s/insp/", context.getExternalCacheDir().getAbsolutePath()));
        createStructure("css.css", R.raw.css);
        //createStructure("esquema_conexion.jpg", R.raw.esquema_conexion);
    }

    public void setTitleHtml(String numMedidor, String dateTime) {
        String sTag;
        String sTagNew;
        int iFin = 0;
        int iIni = 0;
        if (mHtml.contains("title")) {
            int idx = mHtml.indexOf("title");
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
            sTagNew = String.format("%sfi_%s_%s", sTag, numMedidor, dateTime);

            setHtmlFinal(htmlFinal.replace(sTag, sTagNew));
        }
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
                if (sId.equals("rut_prop")) sValue = Util.formatRut(sValue);
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

    public static String getMapvalue(int res) {
        String sValue;
        switch (res) {
            case R.id.txtNumMedidor:
                sValue = "txt_num_medidor";
                break;
            case R.id.spinnerMarca:
                sValue = "txt_marca";
                break;
            case R.id.spinnerTipo:
                sValue = "txt_tipo";
                break;
            case R.id.txtLectura:
                sValue = "txt_lectura";
                break;
            case R.id.spinnerFase:
                sValue = "txt_fase";
                break;
            case R.id.txtPoste:
                sValue = "txt_poste";
                break;
            case R.id.txtRotulo:
                sValue = "txt_rotulo";
                break;
            case R.id.txtSellos:
                sValue = "txt_sellos";
                break;
            case R.id.txtIcp:
                sValue = "txt_icp";
                break;
            case R.id.txtSe:
                sValue = "txt_se_kva";
                break;
            case R.id.txtFechaEjec:
                sValue = "txt_fecha";
                break;
            case R.id.txtHoraIni:
                sValue = "txt_hora_ini";
                break;
            case R.id.txtHoraFin:
                sValue = "txt_hora_fin";
                break;
            case R.id.txtEjecutor:
                sValue = "txt_exe";
                break;
            case R.id.txtVoltF1n:
                sValue = "txt_volt_f1fn";
                break;
            case R.id.txtVoltF1F2:
                sValue = "txt_volt_f1f2";
                break;
            case R.id.txtVoltF2n:
                sValue = "txt_volt_f2fn";
                break;
            case R.id.txtVoltF2F3:
                sValue = "txt_volt_f2f3";
                break;
            case R.id.txtVoltF3n:
                sValue = "txt_volt_f3fn";
                break;
            case R.id.txtVoltF1F3:
                sValue = "txt_volt_f1f3";
                break;
            case R.id.txtVoltNeutro:
                sValue = "txt_volt_neutro";
                break;
            case R.id.chkVerif1:
                sValue = "chk_1_";
                break;
            case R.id.chkVerif2:
                sValue = "chk_2_";
                break;
            case R.id.chkVerif3:
                sValue = "chk_3_";
                break;
            case R.id.chkVerif4:
                sValue = "chk_4_";
                break;
            case R.id.txtNomInst:
                sValue = "nom_prop";
                break;
            case R.id.txtRut:
                sValue = "rut_prop";
                break;
            default:
                sValue = "";
                break;
        }
        return sValue;
    }

    public String getPath() {
        return path;
    }

    public String getPathHtml() {
        return path.concat("index.html");
    }

    public void setPath(String path) {
        this.path = path;
    }
}
