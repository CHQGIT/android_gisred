package cl.gisred.android.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    static final String RAD = "rad";
    private int iSub = 0;
    private String htmlFinal;
    private String path;
    public double sumHH = 0.0;

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
        createStructure("image_no.png", R.raw.image_no);
    }

    public void setTitleHtml(String numMedidor) {
        String sTag;
        String sTagNew;
        int iFin = 0;
        int iIni = 0;
        if (mHtml.contains("title")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fecha = sdf.format(Calendar.getInstance().getTime());

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
            sTagNew = String.format("%sfi_%s_%s", sTag, numMedidor, fecha);

            setHtmlFinal(htmlFinal.replace(sTag, sTagNew));
        }
    }

    public void setValueById(String sId, String sType, String sValue) {
        if (sId == null) return;
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
                if (sId.equals("rut")) sValue = Util.formatRut(sValue);
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
            case R.id.txtSerieMedidor:
                sValue = "txt_serie_medidor";
                break;
            case R.id.txtNumMedidor:
                sValue = "txt_num_medidor";
                break;
            case R.id.txtProducto:
                sValue = "txt_producto";
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
            case R.id.txtVoltF1nF2:
                sValue = "txt_volt_f1nf2";
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
            case R.id.txtTsTp:
                sValue = "txt_ts_tp";
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
            case R.id.chkVerif5:
                sValue = "chk_5_";
                break;
            case R.id.chkConsVerif1:
                sValue = "chkc_1_";
                break;
            case R.id.chkConsVerif2:
                sValue = "chkc_2_";
                break;
            case R.id.chkConsVerif3:
                sValue = "chkc_3_";
                break;
            case R.id.chkConsVerif4:
                sValue = "chkc_4_";
                break;
            case R.id.chkConsVerif5:
                sValue = "chkc_5_";
                break;
            case R.id.txtNomInst:
                sValue = "nom_prop";
                break;
            case R.id.txtRutInst:
                sValue = "rut_pro";
                break;
            case R.id.txtNomTecn:
                sValue = "nom_tecn";
                break;
            case R.id.txtRutTecn:
                sValue = "rut_tecn";
                break;
            case R.id.txtMat1:
                sValue = "txt_mat01";
                break;
            case R.id.txtMat2:
                sValue = "txt_mat02";
                break;
            case R.id.txtMat3:
                sValue = "txt_mat03";
                break;
            case R.id.txtMat4:
                sValue = "txt_mat04";
                break;
            case R.id.txtMat5:
                sValue = "txt_mat05";
                break;
            case R.id.txtMat6:
                sValue = "txt_mat06";
                break;
            case R.id.txtMat7:
                sValue = "txt_mat07";
                break;
            case R.id.txtMat8:
                sValue = "txt_mat08";
                break;
            case R.id.txtMat9:
                sValue = "txt_mat09";
                break;
            case R.id.txtMat10:
                sValue = "txt_mat10";
                break;
            case R.id.txtMat11:
                sValue = "txt_mat11";
                break;
            case R.id.txtMat12:
                sValue = "txt_mat12";
                break;
            case R.id.txtMat13:
                sValue = "txt_mat13";
                break;
            case R.id.txtMat14:
                sValue = "txt_mat14";
                break;
            case R.id.txtMat15:
                sValue = "txt_mat15";
                break;
            case R.id.txtMat16:
                sValue = "txt_mat16";
                break;
            case R.id.txtMat17:
                sValue = "txt_mat17";
                break;
            case R.id.txtMat18:
                sValue = "txt_mat18";
                break;
            case R.id.txtMat19:
                sValue = "txt_mat19";
                break;
            case R.id.txtMat20:
                sValue = "txt_mat20";
                break;
            case R.id.txtMat21:
                sValue = "txt_mat21";
                break;
            case R.id.txtMat22:
                sValue = "txt_mat22";
                break;
            case R.id.txtMat23:
                sValue = "txt_mat23";
                break;
            case R.id.txtMat24:
                sValue = "txt_mat24";
                break;
            case R.id.txtMat25:
                sValue = "txt_mat25";
                break;
            case R.id.txtMat26:
                sValue = "txt_mat26";
                break;
            case R.id.txtMat27:
                sValue = "txt_mat27";
                break;
            case R.id.txtMat28:
                sValue = "txt_mat28";
                break;
            case R.id.txtMat29:
                sValue = "txt_mat29";
                break;
            case R.id.txtMat30:
                sValue = "txt_mat30";
                break;
            case R.id.txtMat31:
                sValue = "txt_mat31";
                break;
            case R.id.txtMat32:
                sValue = "txt_mat32";
                break;
            case R.id.txtMat33:
                sValue = "txt_mat33";
                break;
            case R.id.txtMat34:
                sValue = "txt_mat34";
                break;
            case R.id.txtMat35:
                sValue = "txt_mat35";
                break;
            case R.id.txtMat36:
                sValue = "txt_mat36";
                break;
            case R.id.txtMat37:
                sValue = "txt_mat37";
                break;
            case R.id.txtMat38:
                sValue = "txt_mat38";
                break;
            case R.id.txtMat39:
                sValue = "txt_mat39";
                break;
            case R.id.txtMaterialesAdd1:
                sValue = "txt_mat_add1";
                break;
            case R.id.txtMaterialCant1:
                sValue = "txt_mat40";
                break;
            case R.id.txtMaterialesAdd2:
                sValue = "txt_mat_add2";
                break;
            case R.id.txtMaterialCant2:
                sValue = "txt_mat41";
                break;
            case R.id.txtMaterialesAdd3:
                sValue = "txt_mat_add3";
                break;
            case R.id.txtMaterialCant3:
                sValue = "txt_mat42";
                break;
            case R.id.txtTrabajoServ1:
                sValue = "txt_trabajo_serv1";
                break;
            case R.id.txtTrabajoCant1:
                sValue = "txt_trabajo_cant1";
                break;
            case R.id.txtTrabajoServ2:
                sValue = "txt_trabajo_serv2";
                break;
            case R.id.txtTrabajoCant2:
                sValue = "txt_trabajo_cant2";
                break;
            case R.id.txtTrabajoServ3:
                sValue = "txt_trabajo_serv3";
                break;
            case R.id.txtTrabajoCant3:
                sValue = "txt_trabajo_cant3";
                break;
            case R.id.txtTrabajoServ4:
                sValue = "txt_trabajo_serv4";
                break;
            case R.id.txtTrabajoCant4:
                sValue = "txt_trabajo_cant4";
                break;
            case R.id.txtTrabajoServ5:
                sValue = "txt_trabajo_serv5";
                break;
            case R.id.txtTrabajoCant5:
                sValue = "txt_trabajo_cant5";
                break;
            case R.id.txtTrabajoServ6:
                sValue = "txt_trabajo_serv6";
                break;
            case R.id.txtTrabajoCant6:
                sValue = "txt_trabajo_cant6";
                break;
            case R.id.txtTrabajoServ7:
                sValue = "txt_trabajo_serv7";
                break;
            case R.id.txtTrabajoCant7:
                sValue = "txt_trabajo_cant7";
                break;
            case R.id.txtTrabajoServ8:
                sValue = "txt_trabajo_serv8";
                break;
            case R.id.txtTrabajoCant8:
                sValue = "txt_trabajo_cant8";
                break;
            case R.id.txtMedRetirado:
                sValue = "txt_medidor_ret";
                break;
            case R.id.spinnerMarcaRet:
                sValue = "txt_marca_ret";
                break;
            case R.id.spinnerTipoRet:
                sValue = "txt_tipo_ret";
                break;
            case R.id.preNum1:
                sValue = "rad_1_";
                break;
            case R.id.preNum2:
                sValue = "rad_2_";
                break;
            case R.id.preNum3:
                sValue = "rad_3_";
                break;
            case R.id.preNum4:
                sValue = "rad_4_";
                break;
            case R.id.preNum5:
                sValue = "rad_5_";
                break;
            case R.id.preNum6:
                sValue = "rad_6_";
                break;
            case R.id.preNum7:
                sValue = "rad_7_";
                break;
            case R.id.preNum8:
                sValue = "rad_8_";
                break;
            case R.id.preNum9:
                sValue = "rad_9_";
                break;
            case R.id.preNum10:
                sValue = "rad_10_";
                break;
            case R.id.preNum11:
                sValue = "rad_11_";
                break;
            case R.id.preNum12:
                sValue = "rad_12_";
                break;
            default:
                sValue = null;
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
