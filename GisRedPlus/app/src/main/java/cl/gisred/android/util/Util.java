package cl.gisred.android.util;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Segment;
import com.esri.core.geometry.Transformation2D;
import com.esri.core.internal.geometry.ConversionType;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.geocode.LocatorFieldInfo;
import com.esri.core.tasks.identify.IdentifyResult;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.gisred.android.R;
import cl.gisred.android.classes.GisEditText;
import cl.gisred.android.entity.CalloutTvClass;

/**
 * Created by cramiret on 23-05-2016.
 */
public class Util {

    public static final int REQUEST_READ_PHONE_STATE = 1001;
    private Point oUbic = null;

    public Util(Point mPoint) {
        oUbic = mPoint;
    }

    public Util() { }

    public CalloutTvClass getCalloutValues(Map<String, Object> map) {
        String value = "";
        String title;
        int objectId = 0;
        String tipo = "";
        String lsp = System.getProperty("line.separator");
        StringBuilder viewText = new StringBuilder();

        for (String key : map.keySet()) {
            String valKey = (map.get(key) != null) ? map.get(key).toString() : "";

            if (key.equalsIgnoreCase("CALLE"))
                value = valKey + value;
            else if (key.equalsIgnoreCase("NOMBRE_CALLE")) { //EXISTENTES
                value = valKey + value;
            } else if (key.equalsIgnoreCase("NUMERO")) {
                value += " " + valKey;
                title = "Dirección: ";
                viewText.append(title + value);
                viewText.append(lsp);
            } else if (key.equalsIgnoreCase("ROTULO")) {
                value += valKey;
                title = "Rótulo: ";
                viewText.append(title + value);
                viewText.append(lsp);
            } else if (key.equalsIgnoreCase("TIPO_POSTE")) {
                viewText.append("Tipo Poste: " + valKey);
                viewText.append(lsp);
                tipo = valKey;
            } else if (key.equalsIgnoreCase("TIPO_TENSION")) {
                viewText.append("Tipo Tensión: " + valKey);
                viewText.append(lsp);
            } else if (key.equalsIgnoreCase("TIPO")) { //EXISTENTES
                viewText.append("Tipo Tensión: " + valKey);
                viewText.append(lsp);
                tipo = valKey; //TIPO POSTE cambia por TIPO TENSION al ser EXISTENTE
            } else if (key.equalsIgnoreCase("TIPO_EDIFICACION")) {
                viewText.append("Tipo Edificación: " + valKey);
                viewText.append(lsp);
                tipo = valKey;
            } else if (key.equalsIgnoreCase("OBJECTID")) {
                if (map.get(key).getClass().equals(Integer.class))
                    objectId = (int) map.get(key);
            } else if (key.equalsIgnoreCase("ID_DIRECCION")) {
                if (map.get(key).getClass().equals(String.class))
                    objectId = Integer.valueOf(map.get(key).toString());
            } else if (key.equalsIgnoreCase("ID_NODO")) {
                if (map.get(key).getClass().equals(String.class))
                    objectId = Integer.valueOf(map.get(key).toString());
            }
        }

        viewText.deleteCharAt(viewText.length() - 1);

        return new CalloutTvClass(viewText.toString(), value, objectId, tipo);
    }

    public ArrayList<Map<String, Object>> getAttrAddByView(View v, int idRes) {
        Map<String, Object> objectMap = new HashMap<>();
        Map<String, Object> oMapDireccion = new HashMap<>();
        Map<String, Object> oMapPoste = new HashMap<>();
        Map<String, Object> oMapUbicacion = new HashMap<>();

        ArrayList<Map<String, Object>> arrayMapAttr = new ArrayList<>();

        if (idRes == R.layout.dialog_poste) {

            for (View view : v.getTouchables()) {

                if (view.getClass().getGenericSuperclass().equals(EditText.class)) {
                    EditText oText = (EditText) view;

                    if (oText.getId() == R.id.txtRotulo)
                        objectMap.put("ROTULO", oText.getText().toString());

                } else if (view.getClass().getGenericSuperclass().equals(Spinner.class)) {
                    Spinner oSpinner = (Spinner) view;
                    String sValue = oSpinner.getSelectedItem().toString();

                    if (oSpinner.getId() == R.id.spinnerTipoPoste)
                        objectMap.put("TIPO_POSTE", sValue);
                    else if (oSpinner.getId() == R.id.spinnerTension)
                        objectMap.put("TIPO_TENSION", sValue);
                }
            }

            if (oUbic != null) {
                objectMap.put("X", oUbic.getX());
                objectMap.put("Y", oUbic.getY());
            }

        } else if (idRes == R.layout.dialog_direccion) {

            for (View view : v.getTouchables()) {

                if (view.getClass().getGenericSuperclass().equals(EditText.class)) {
                    EditText oText = (EditText) view;

                    if (oText.getText() != null && !oText.getText().toString().isEmpty()) {
                        if (oText.getId() == R.id.txtStreet)
                            objectMap.put("CALLE", oText.getText().toString());
                        else if (oText.getId() == R.id.txtNumber)
                            objectMap.put("NUMERO", oText.getText().toString());
                        else if (oText.getId() == R.id.txtAnexo1)
                            objectMap.put("ANEXO1", oText.getText().toString());
                        else if (oText.getId() == R.id.txtAnexo2)
                            objectMap.put("ANEXO2", oText.getText().toString());
                    }
                } else if (view.getClass().getGenericSuperclass().equals(Spinner.class)) {
                    Spinner oSpinner = (Spinner) view;
                    String sValue = oSpinner.getSelectedItem().toString();

                    if (oSpinner.getId() == R.id.spinnerTipoEdif)
                        objectMap.put("TIPO_EDIFICACION", sValue);
                }
            }

            if (oUbic != null) {
                objectMap.put("X", oUbic.getX());
                objectMap.put("Y", oUbic.getY());
            }

        } else if (idRes == R.layout.dialog_cliente) {

            for (View view : v.getTouchables()) {

                if (view.getClass().equals(GisEditText.class)) {
                    GisEditText oText = (GisEditText) view;

                    if (oText.getText() != null && !oText.getText().toString().isEmpty()) {
                        if (oText.getId() == R.id.txtAsocAddress){
                            objectMap.put("ID_DIRECCION", oText.getIdObjeto());
                            objectMap.put("TIPO_DIRECCION", oText.getTipo());

                            oMapDireccion.put("ID_DIRECCION", oText.getIdObjeto());
                            oMapUbicacion.put("DIRECCION_POINT", oText.getPoint());
                        } else if (oText.getId() == R.id.txtAsocPoste) {
                            objectMap.put("ID_POSTE_CAMARA", oText.getIdObjeto());
                            objectMap.put("TIPO_POSTE_CAMARA", oText.getTipo());

                            oMapPoste.put("ID_POSTE", oText.getIdObjeto());
                            oMapPoste.put("ROTULO", oText.getText().toString());
                            oMapUbicacion.put("POSTE_POINT", oText.getPoint());
                        }
                    }

                } else if (view.getClass().getGenericSuperclass().equals(EditText.class)) {
                    EditText oText = (EditText) view;

                    if (oText.getText() != null && !oText.getText().toString().isEmpty()) {
                        if (oText.getId() == R.id.txtNis){
                            objectMap.put("NIS", oText.getText().toString());

                            oMapDireccion.put("NIS", oText.getText().toString());
                            oMapPoste.put("NIS", oText.getText().toString());
                        }
                        else if (oText.getId() == R.id.txtOS)
                            objectMap.put("OS", oText.getText().toString());
                        else if (oText.getId() == R.id.txtNumMedidor)
                            objectMap.put("NUMERO_MEDIDOR", oText.getText().toString());
                    }

                } else if (view.getClass().getGenericSuperclass().equals(Spinner.class)) {
                    Spinner oSpinner = (Spinner) view;
                    String sValue = oSpinner.getSelectedItem().toString();

                    if (oSpinner.getId() == R.id.spinnerTipoMedidor)
                        objectMap.put("TIPO_MEDIDOR", sValue);
                    else if (oSpinner.getId() == R.id.spinnerTecMedidor)
                        objectMap.put("TIPO_TECNOLOGIA", sValue);
                    else if (oSpinner.getId() == R.id.spinnerTipoEmpalme)
                        objectMap.put("TIPO_EMPALME", sValue);
                }
            }
        } else if (idRes == R.layout.dialog_cliente_cnr) {

            for (View view : v.getTouchables()) {

                if (view.getClass().equals(GisEditText.class)) {
                    GisEditText oText = (GisEditText) view;

                    if (oText.getText() != null && !oText.getText().toString().isEmpty()) {
                        if (oText.getId() == R.id.txtAsocAddress){
                            objectMap.put("ID_DIRECCION", oText.getIdObjeto());
                            objectMap.put("TIPO_DIRECCION", oText.getTipo());

                            oMapDireccion.put("ID_DIRECCION", oText.getIdObjeto());
                            oMapUbicacion.put("DIRECCION_POINT", oText.getPoint());
                        } else if (oText.getId() == R.id.txtAsocPoste) {
                            objectMap.put("ID_POSTE_CAMARA", oText.getIdObjeto());
                            objectMap.put("TIPO_POSTE_CAMARA", oText.getTipo());

                            oMapPoste.put("ID_POSTE", oText.getIdObjeto());
                            oMapPoste.put("ROTULO", oText.getText().toString());
                            oMapUbicacion.put("POSTE_POINT", oText.getPoint());
                        } else if (oText.getId() == R.id.txtAsocTramo) {
                            objectMap.put("ID_TRAMO_BT", oText.getText().toString());
                        }
                    }

                } else if (view.getClass().getGenericSuperclass().equals(EditText.class)) {
                    EditText oText = (EditText) view;

                    if (oText.getText() != null && !oText.getText().toString().isEmpty()) {
                        if (oText.getId() == R.id.txtNis){
                            objectMap.put("NIS", oText.getText().toString());

                            oMapDireccion.put("NIS", oText.getText().toString());
                            oMapPoste.put("NIS", oText.getText().toString());
                        }
                        else if (oText.getId() == R.id.txtOS)
                            objectMap.put("OS", oText.getText().toString());
                        else if (oText.getId() == R.id.txtNumMedidor)
                            objectMap.put("NUMERO_MEDIDOR", oText.getText().toString());
                    }

                } else if (view.getClass().getGenericSuperclass().equals(Spinner.class)) {
                    Spinner oSpinner = (Spinner) view;
                    String sValue = oSpinner.getSelectedItem().toString();

                    if (oSpinner.getId() == R.id.spinnerTipoMedidor)
                        objectMap.put("TIPO_MEDIDOR", sValue);
                    else if (oSpinner.getId() == R.id.spinnerTecMedidor)
                        objectMap.put("TIPO_TECNOLOGIA", sValue);
                    else if (oSpinner.getId() == R.id.spinnerTipoEmpalme)
                        objectMap.put("TIPO_EMPALME", sValue);
                    else if (oSpinner.getId() == R.id.spinnerFaseConex)
                        objectMap.put("FASE_CONEXION", sValue);
                    else if (oSpinner.getId() == R.id.spinnerTipoCNR)
                        objectMap.put("TIPO_CNR", sValue);
                }
            }
        }

        arrayMapAttr.add(objectMap);
        arrayMapAttr.add(oMapDireccion);
        arrayMapAttr.add(oMapPoste);
        arrayMapAttr.add(oMapUbicacion);

        return arrayMapAttr;
    }

    public static Graphic[] addAttrUnionPoint(ArrayList<Map<String, Object>> arrayMap, Point oUbic) {

        Graphic graphicDireccion = null;
        Graphic graphicPoste = null;

        if (arrayMap.get(3).containsKey("DIRECCION_POINT")) {
            Point pointDireccion = (Point) arrayMap.get(3).get("DIRECCION_POINT");
            Polyline oLine = new Polyline();
            oLine.startPath(oUbic);
            oLine.lineTo(pointDireccion);
            graphicDireccion = new Graphic(oLine, null, arrayMap.get(1));
        }
        if (arrayMap.get(3).containsKey("POSTE_POINT")) {
            Point pointPoste = (Point) arrayMap.get(3).get("POSTE_POINT");
            Polyline oLine = new Polyline();
            oLine.startPath(oUbic);
            oLine.lineTo(pointPoste);
            graphicPoste = new Graphic(oLine, null, arrayMap.get(2));
        }

        Graphic[] adds = {graphicDireccion, graphicPoste};

        return adds;
    }

    public String formatCapitalize(String str) {
        String formatStr = str.replace("_", " ");
        String[] splitStr = formatStr.split(" ");
        String resp = "";

        if (splitStr.length > 0) {
            for (String s : splitStr) {
                String sTemp = s.toUpperCase();
                s = s.trim().replaceFirst(""+s.charAt(0), ""+sTemp.charAt(0));
                resp += s + " ";
            }
        }
        return resp.trim();
    }

    public StringBuilder getStringByClassAttr(IdentifyResult identResult) {
        String LSP = System.getProperty("line.separator");
        StringBuilder outStr = new StringBuilder();
        boolean isOrdenable = true;
        Map<String, Object> oAtrr = identResult.getAttributes();

        if (identResult.getLayerName().equalsIgnoreCase("Nodos")) {

            if (oAtrr.containsKey("tipo_nodo")) {
                outStr.append("NODO " + oAtrr.get("tipo_nodo").toString().replace("ele!", "").toUpperCase());
                outStr.append(LSP); outStr.append(LSP);
            }

            String[] keys = {"rotulo", "sed", "alimentador", "comuna", "tipo", "catalogo", "cudn", "fecha", "año_poste"};
            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().equalsIgnoreCase("RED BT")) {

            outStr.append("RED BT"); outStr.append(LSP); outStr.append(LSP);

            String[] keys = {"sed", "alimentador", "comuna", "propiedad", "catalogo", "descripcion", "fecha", "cudn"};
            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().equalsIgnoreCase("RED AP")) {

            outStr.append("RED AP"); outStr.append(LSP); outStr.append(LSP);

            String[] keys = {"sed", "alimentador", "comuna", "propiedad", "descripcion"};
            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().equalsIgnoreCase("Equipos_linea_006")) {

            String[] keys = {"nombre", "estado_normal", "descripcion", "estado", "alimentador", "fecha"};
            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().equalsIgnoreCase("Clientes")) {

            outStr.append("CLIENTE"); outStr.append(LSP); outStr.append(LSP);

            String[] keys = {"nis", "nm_estado_suministro", "categoria", "cd_area_tipica", "empalme", "nm_tarifa", "cd_sector", "cd_area", "consumidor", "zona",
                    "nm_comuna", "oficina", "resp_tipo_cliente", "estado_direccion", "estado_poste", "estado_final", "resp_rotulo_nodo", "direccion_resu"};

            String sTemp = setValuesByKey(keys, oAtrr);
            sTemp = sTemp.replace("Nm ", "");
            sTemp = sTemp.replace("Cd ", "");
            sTemp = sTemp.replace("Resp ", "");
            sTemp = sTemp.replace(" Nodo", "");
            sTemp = sTemp.replace(" Resu", "");

            outStr.append(sTemp);

        } else if (identResult.getLayerName().equalsIgnoreCase("STx Torres")) {

            String[] keys = {"nombre_obj", "empresa"};
            String sTemp = setValuesByKey(keys, oAtrr);
            sTemp = sTemp.replace(" Obj", "");

            outStr.append(sTemp);

        } else if (identResult.getLayerName().equalsIgnoreCase("STx TRAMOS")) {

            String[] keys = {"nm_linea", "tension", "nm_tramo_l", "largo", "cable_guar", "circuito_s", "empresa"};
            String sTemp = setValuesByKey(keys, oAtrr);

            sTemp = sTemp.replace("Nm Tramo L", "Tramo Linea");
            sTemp = sTemp.replace("Nm ", "Nombre ");
            sTemp = sTemp.replace(" S", "");
            sTemp = sTemp.replace(" Guar", " Guardia");

            outStr.append(sTemp);

        } else if (identResult.getLayerName().contains("Salida")) {

            outStr.append("Salida Alimentador"); outStr.append(LSP); outStr.append(LSP);

            String[] keys = {"nombre", "tension"};
            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().contains("Subestaciones")) {

            outStr.append("SED"); outStr.append(LSP); outStr.append(LSP);

            String[] keys = {"codigo", "nombre", "kva", "alimentador", "comuna", "montaje", "fecha"};
            outStr.append(setValuesByKey(keys, oAtrr));

        } else {
            isOrdenable = false;
        }

        if (!isOrdenable) {
            for (Map.Entry<String, Object> oKeyVal : oAtrr.entrySet()) {
                Log.w("MultiIdentifyResults", String.format("%s : %s", oKeyVal.getKey(), oKeyVal.getValue()));

                if (!oKeyVal.getKey().contains("id_") &&
                        !oKeyVal.getKey().contains("ID_") &&
                        !oKeyVal.getKey().equalsIgnoreCase("OBJECTID") &&
                        !oKeyVal.getKey().contains("SHAPE")) {

                    outStr.append(String.format("%s: %s", formatCapitalize(oKeyVal.getKey()), oKeyVal.getValue()));
                    outStr.append(LSP);
                }
            }

            outStr.deleteCharAt(outStr.length() - 1);
        }

        return outStr;
    }

    @NonNull
    private String setValuesByKey(String[] keys, Map<String, Object> oAtrr) {
        String LSP = System.getProperty("line.separator");
        StringBuilder outStr = new StringBuilder();

        for (String key : keys) {
            if (oAtrr.containsKey(key) && !oAtrr.get(key).toString().trim().isEmpty()) {
                outStr.append(String.format("%s: %s", formatCapitalize(key), oAtrr.get(key).toString()));
                outStr.append(LSP);
            }
        }

        outStr.deleteCharAt(outStr.length() - 1);

        return outStr.toString();
    }

    public Point calculateCenterPolyline(Polyline oPolyline) {
        Point center = new Point();
        double valX, valY;

        valX = (oPolyline.getPoint(0).getX() + oPolyline.getPoint(1).getX()) / 2;
        valY = (oPolyline.getPoint(0).getY() + oPolyline.getPoint(1).getY()) / 2;

        center.setXY(valX, valY);

        return center;
    }

    public static String getSerial(){
        String sSerial = Build.SERIAL;

        return sSerial;
    }

    public static String getImei23(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            String sImei = "";

            try {
                TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
                int permissionCheck = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    Log.w("permissionCheck", "permision request");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                    Log.w("permissionCheck", "permision ok");
                    sImei = telephonyManager.getDeviceId();
                    Log.w("permissionCheck", "sImei: " + sImei);
                } else {
                    //TODO
                    Log.w("permissionCheck", "permision null");
                }
                return sImei;
            } catch (Exception ex) {
                Log.w("permissionCheck", "permision error: " +ex.getMessage());
                return "null";
            }

        } else {
            return getImei(activity.getApplicationContext());
        }
    }

    public static String getImei(Context c) {
        TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static String getIpAddress(){
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            while(enumNetworkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while(enumInetAddress.hasMoreElements()){
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()){
                        ip += inetAddress.getHostAddress();
                        break;
                    }
                }
                if (!ip.isEmpty()) break;
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }

        return ip;
    }

    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static boolean isPackageExisted(String targetPackage, Context mContext){
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = mContext.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(targetPackage))
                return true;
        }

        return false;
    }

    public static boolean validateRut(String rut_string){
        boolean output = true;
        try {
            if (rut_string.length()<2) output = false;
            else output = verificarRut(Integer.parseInt(rut_string.substring(0,rut_string.length()-1)), rut_string.charAt(rut_string.length()-1));
        } catch (Exception e){
            output = false;
        }
        return output;
    }

    private static boolean verificarRut(int rut, char dv) {
        int dgt, cnt, mult, acc;
        String r_dig, dv_s;
        cnt = 2; acc = 0;
        while (rut != 0)
        {
            mult = (rut % 10) * cnt;
            acc += mult;
            rut /= 10;
            cnt++;
            if (cnt == 8) cnt = 2;
        }
        dgt = 11 - (acc % 11);
        r_dig = ""+dgt;
        dv_s = ""+dv;
        if (dgt == 10) r_dig = "k";
        if (dgt == 11) r_dig = "0";
        return r_dig.equalsIgnoreCase(dv_s);
    }

    public static String getUserWithoutDomain(String user) {
        String[] sTemp = user.split("\\\\");
        if (sTemp.length > 1) {
            return sTemp[1];
        } else return user;
    }

    public static String formatRut(String sValue) {
        try {
            String sDig = sValue.toUpperCase().substring(sValue.length() - 1);
            String sNum = sValue.substring(0, sValue.length() - 1);



            Log.w("formatRut", sNum + "-" + sDig);
            return String.format("%s-%s", sNum, sDig);
        } catch (Exception ex) {
            ex.printStackTrace();
            return sValue;
        }
    }
}
