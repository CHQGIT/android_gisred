package cl.gisred.android.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Feature;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.Symbol;
import com.esri.core.tasks.identify.IdentifyResult;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cl.gisred.android.BuildConfig;
import cl.gisred.android.R;
import cl.gisred.android.classes.GisEditText;
import cl.gisred.android.entity.CalloutTvClass;

/**
 * Created by cramiret on 23-05-2016.
 */
public class Util {

    public static final int REQUEST_READ_PHONE_STATE = 1001;
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1002;
    public static final int REQUEST_CAMERA = 1003;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 1004;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1005;
    private Point oUbic = null;
    private String sCapa;

    public Util(Point mPoint) {
        oUbic = mPoint;
    }

    public Util(String mCapa) {
        sCapa = mCapa;
    }

    public Util() {
    }

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

    public void setAttrInView(int idRes, View v, Map<String, Object> oAttr) {

        for (View view : v.getTouchables()) {

            if (view.getClass().equals(GisEditText.class)) {
                GisEditText oText = (GisEditText) view;

                if (oText.getText() != null && !oText.getText().toString().isEmpty()) {
                    /*if (oText.getId() == R.id.txtAsocAddress) {
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
                    }*/
                }

            } else if (view.getClass().getGenericSuperclass().equals(EditText.class)) {
                EditText oText = (EditText) view;

                if (oText.getText() != null) {
                    if (oText.getId() == R.id.txtNumMedidor)
                        oText.setText(formatValCampoDB(oAttr.get("NUMERO_MEDIDOR")));
                }

            }
        }
    }

    public ArrayList<Map<String, Object>> getAttrAddByView(View v, int idRes, String emp) {
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
                            objectMap.put("CALLE", oText.getText().toString().toUpperCase());
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
                        if (oText.getId() == R.id.txtAsocAddress) {
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
                        if (oText.getId() == R.id.txtNis) {
                            objectMap.put("NIS", oText.getText().toString());

                            oMapDireccion.put("NIS", oText.getText().toString());
                            oMapPoste.put("NIS", oText.getText().toString());
                        } else if (oText.getId() == R.id.txtOS)
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
                }
            }
        } else if (idRes == R.layout.dialog_cliente_cnr) {

            for (View view : v.getTouchables()) {

                if (view.getClass().equals(GisEditText.class)) {
                    GisEditText oText = (GisEditText) view;

                    if (oText.getText() != null && !oText.getText().toString().isEmpty()) {
                        if (oText.getId() == R.id.txtAsocAddress) {
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
                        if (oText.getId() == R.id.txtNis) {
                            objectMap.put("NIS", oText.getText().toString());

                            oMapDireccion.put("NIS", oText.getText().toString());
                            oMapPoste.put("NIS", oText.getText().toString());
                        } else if (oText.getId() == R.id.txtOS)
                            objectMap.put("OS", oText.getText().toString());
                        else if (oText.getId() == R.id.txtNumMedidor)
                            objectMap.put("NUMERO_MEDIDOR", oText.getText().toString());
                        else if (oText.getId() == R.id.txtNumIcp)
                            objectMap.put("ICP", oText.getText().toString());
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

        //ADD Empresa 19/01/17
        objectMap.put("EMPRESA", emp);
        oMapDireccion.put("EMPRESA", emp);
        oMapPoste.put("EMPRESA", emp);

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

    public String formatCampoDB(String str) {
        String formatStr = str.replace(".", " ");
        String[] splitStr = formatStr.split(" ");

        if (splitStr.length > 1) {
            int nLast = splitStr.length - 1;
            String resp = splitStr[nLast];

            return resp;
        } else {
            return str;
        }
    }

    public String formatCapitalize(String str) {
        String formatStr = str.replace("_", " ");
        String[] splitStr = formatStr.split(" ");
        String resp = "";

        if (splitStr.length > 0) {
            for (String s : splitStr) {
                String sTemp = s.toUpperCase();
                s = s.trim().replaceFirst("" + s.charAt(0), "" + sTemp.charAt(0));
                resp += s + " ";
            }
        }
        return resp.trim();
    }

    public String formatValCampoDB(Object o) {
        String sVal = "";
        if (o != null) {
            if (o.getClass().equals(Double.class)) {
                sVal = String.valueOf(((Double) o).intValue());
            } else if (o.getClass().equals(Long.class)) {
                Date date = new Date(Long.valueOf(o.toString()));
                sVal = date.toGMTString();
            } else {
                sVal = o.toString();
            }
        }
        return sVal;
    }

    public StringBuilder getStringByAttrClass(int numBusq, Map<String, Object> oAtrr) {
        String LSP = System.getProperty("line.separator");
        StringBuilder outStr = new StringBuilder();
        Map<String, Object> oAttrAbrev = new HashMap<>();

        for (Map.Entry<String, Object> oKeyVal : oAtrr.entrySet()) {
            oAttrAbrev.put(formatCampoDB(oKeyVal.getKey()), formatValCampoDB(oKeyVal.getValue()));
        }

        if (numBusq == 0) {

            outStr.append("CLIENTE");
            if (oAttrAbrev.containsKey("nis") && !oAttrAbrev.get("nis").toString().trim().isEmpty())
                outStr.append(": " + oAttrAbrev.get("nis").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"nm_estado_suministro", "zona", "nm_tarifa", "categoria", "oficina", "nm_comuna", "empalme", "cd_sector", "cd_area", "consumidor",
                    "resp_rotulo_nodo", "resp_id_sed", "direccion_resu"};

            String sTemp = setValuesByKey(keys, oAttrAbrev);
            sTemp = sTemp.replace("Nm ", "");
            sTemp = sTemp.replace("Cd ", "");
            sTemp = sTemp.replace("Resp ", "");
            sTemp = sTemp.replace("Id ", "");
            sTemp = sTemp.replace(" Nodo", "");
            sTemp = sTemp.replace(" Resu", "");
            sTemp = sTemp.replace(" Suministro", "");
            sTemp = sTemp.replace("Sed", "SED");

            outStr.append(sTemp);

        } else if (numBusq == 1) {

            outStr.append("SED");
            if (oAttrAbrev.containsKey("codigo") && !oAttrAbrev.get("codigo").toString().trim().isEmpty())
                outStr.append(": " + oAttrAbrev.get("codigo").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"nombre", "montaje", "alimentador", "comuna", "propiedad", "kva", "fecha"};
            outStr.append(setValuesByKey(keys, oAttrAbrev));
        } else if (numBusq == 2) {

            outStr.append("POSTE");
            if (oAttrAbrev.containsKey("rotulo") && !oAttrAbrev.get("rotulo").toString().trim().isEmpty())
                outStr.append(": " + oAttrAbrev.get("rotulo").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"id_nodo", "tipo_nodo", "alimentador", "comuna", "tipo", "propiedad", "catalogo", "cudn", "fecha", "fabricante", "año_poste", "sed"};
            outStr.append(setValuesByKey(keys, oAttrAbrev));
        } else if (numBusq == 5) {

            outStr.append("EQUIPO");
            if (oAttrAbrev.containsKey("id_equipo") && !oAttrAbrev.get("id_equipo").toString().trim().isEmpty())
                outStr.append(": " + oAttrAbrev.get("id_equipo").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"nombre", "alimentador", "descripcion", "tipo", "propiedad", "catalogo", "cudn", "fecha", "es_sed", "id_nodo", "id_sed"};
            outStr.append(setValuesByKey(keys, oAttrAbrev));
        } else if (numBusq == -1) {

            outStr.append("INSPECCION LECTURA");
            outStr.append(LSP);
            outStr.append(LSP);
            if (oAttrAbrev.containsKey("OBJECTID") && !oAttrAbrev.get("OBJECTID").toString().trim().isEmpty())
                outStr.append("OBJECT ID: " + oAttrAbrev.get("OBJECTID").toString());
            outStr.append(LSP);

            String[] keys = {"nro_medidor", "estado", "tipo_edificacion", "poste", "direccion", "lectura_actual", "inspeccion", "ot", "inspector"};
            outStr.append(setValuesByKey(keys, oAttrAbrev));
            outStr.append(LSP);
            outStr.append(LSP);
            outStr.append("Presione para cerrar");
        } else if (numBusq == -2) {

            outStr.append("INSPECCION OT");
            outStr.append(LSP);
            outStr.append(LSP);
            if (oAttrAbrev.containsKey("OT") && !oAttrAbrev.get("OT").toString().trim().isEmpty())
                outStr.append("OT: " + oAttrAbrev.get("OT").toString());
            outStr.append(LSP);

            String[] keys = {"ZONA", "COMUNA"};
            outStr.append(setValuesByKey(keys, oAttrAbrev));
            outStr.append(LSP);
            outStr.append(LSP);
            outStr.append("Presione para cerrar");
        } else if (numBusq == -3) {

            outStr.append("INSPECCION OT");
            outStr.append(LSP);
            outStr.append(LSP);
            if (oAttrAbrev.containsKey("ot") && !oAttrAbrev.get("ot").toString().trim().isEmpty())
                outStr.append("OT: " + oAttrAbrev.get("ot").toString());
            outStr.append(LSP);
            outStr.append("Tipo: " + sCapa);
            outStr.append(LSP);
            outStr.append(LSP);
            outStr.append("Presione para cerrar");
        } else {
            //GENERICOS
            for (Map.Entry<String, Object> oKeyVal : oAtrr.entrySet()) {
                String sKey = formatCampoDB(oKeyVal.getKey());

                if (!sKey.contains("id_") &&
                        !sKey.contains("ID_") &&
                        !sKey.equalsIgnoreCase("OBJECTID") &&
                        !sKey.contains("SHAPE")) {

                    outStr.append(String.format("%s: %s", formatCapitalize(sKey), oKeyVal.getValue()));
                    outStr.append(LSP);
                }
            }

            if (outStr.length() > 0) outStr.deleteCharAt(outStr.length() - 1);
        }

        return outStr;
    }

    public StringBuilder getStringByClassAttr(IdentifyResult identResult) {
        String LSP = System.getProperty("line.separator");
        StringBuilder outStr = new StringBuilder();
        boolean isOrdenable = true;
        Map<String, Object> oAtrr = identResult.getAttributes();

        if (identResult.getLayerName().equalsIgnoreCase("Nodos")) {

            if (oAtrr.containsKey("tipo_nodo")) {
                outStr.append("NODO " + oAtrr.get("tipo_nodo").toString().replace("ele!", "").toUpperCase());
                outStr.append(LSP);
                outStr.append(LSP);
            }

            String[] keys = {"rotulo", "id_nodo", "tipo_nodo", "alimentador", "comuna", "tipo", "propiedad", "catalogo", "cudn", "fecha", "fabricante", "año_poste", "sed"};
            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().equalsIgnoreCase("RED BT")) {

            outStr.append("RED BT");
            if (oAtrr.containsKey("id_tramo") && !oAtrr.get("id_tramo").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("id_tramo").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"alimentador", "comuna", "tipo", "propiedad", "sed", "catalogo", "descripcion", "fecha"};
            String sTemp = setValuesByKey(keys, oAtrr);
            sTemp = sTemp.replace("Sed:", "SED:");

            outStr.append(sTemp);

        } else if (identResult.getLayerName().equalsIgnoreCase("RED AP")) {

            outStr.append("RED AP");
            if (oAtrr.containsKey("id_tramo") && !oAtrr.get("id_tramo").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("id_tramo").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"sed", "alimentador", "comuna", "tipo", "tipo_red", "propiedad", "catalogo", "descripcion", "fecha", "id_equipo_ap"};
            String sTemp = setValuesByKey(keys, oAtrr);
            sTemp = sTemp.replace("Sed:", "SED:");

            outStr.append(sTemp);

        } else if (identResult.getLayerName().equalsIgnoreCase("RED MT")) {

            outStr.append("RED MT");
            if (oAtrr.containsKey("id") && !oAtrr.get("id").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("id").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"alimentador", "comuna", "tipo", "propiedad", "catalogo", "descripcion", "tension", "fecha", "color"};
            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().equalsIgnoreCase("Clientes")) {

            outStr.append("CLIENTE");
            if (oAtrr.containsKey("nis") && !oAtrr.get("nis").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("nis").toString());
            outStr.append(LSP);
            outStr.append(LSP);
            String sTemp;

            if (identResult.getLayerId() == 0) {
                String[] keys = {"nm_estado_suministro", "zona", "nm_tarifa", "categoria", "oficina", "nm_comuna", "empalme", "cd_sector", "cd_area", "consumidor",
                        "resp_rotulo_nodo", "resp_id_sed", "direccion_resu"};

                sTemp = setValuesByKey(keys, oAtrr);
                sTemp = sTemp.replace("Nm ", "");
                sTemp = sTemp.replace("Cd ", "");
                sTemp = sTemp.replace("Resp ", "");
                sTemp = sTemp.replace("Id ", "");
                sTemp = sTemp.replace(" Nodo", "");
                sTemp = sTemp.replace(" Resu", "");
                sTemp = sTemp.replace(" Suministro", "");
                sTemp = sTemp.replace("Sed", "SED");

            } else {
                String[] keys = {"id_orden", "id_incidencia", "causa", "comentario", "estado_orden", "fecha_creacion", "fecha_asignacion", "fecha_despacho", "fecha_ruta", "fecha_llegada", "TIEMPO_TRA", "etr"};

                sTemp = setValuesByKey(keys, oAtrr);
                sTemp = sTemp.replace("Id ", "ID ");
                sTemp = sTemp.replace(" Orden", "");
                sTemp = sTemp.replace("TIEMPO TRA", "Tiempo transcurrido");
                sTemp = sTemp.replace("Etr", "ETR");
            }

            outStr.append(sTemp);

        } else if (identResult.getLayerName().equalsIgnoreCase("STx Torres")) {

            outStr.append("STx TORRE");
            if (oAtrr.containsKey("nombre_obj") && !oAtrr.get("nombre_obj").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("nombre_obj").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"empresa"};
            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().equalsIgnoreCase("STx TRAMOS")) {

            outStr.append("STx TRAMO");
            if (oAtrr.containsKey("nm_linea") && !oAtrr.get("nm_linea").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("nm_linea").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"tension", "nm_tramo_l", "largo", "cable_guar", "empresa"};
            String sTemp = setValuesByKey(keys, oAtrr);

            sTemp = sTemp.replace("Nm Tramo L", "Tramo Linea");
            sTemp = sTemp.replace(" Guar", " Guardia");

            outStr.append(sTemp);

        } else if (identResult.getLayerName().contains("Salida")) {

            outStr.append("Alimentador");
            if (!identResult.getValue().toString().trim().isEmpty())
                outStr.append(": " + identResult.getValue());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"id_alimentador", "color", "tension"};
            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().contains("Subestaciones")) {

            outStr.append("SED");
            if (oAtrr.containsKey("codigo") && !oAtrr.get("codigo").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("codigo").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"nombre", "montaje", "alimentador", "comuna", "propiedad", "kva", "fecha"};
            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().equalsIgnoreCase("SED")) {

            outStr.append("SED");
            if (oAtrr.containsKey("ARCGIS.DBO.SED_006.codigo") && !oAtrr.get("ARCGIS.DBO.SED_006.codigo").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("ARCGIS.DBO.SED_006.codigo").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"id_orden", "id_incidencia", "alimentador", "causa", "comentario", "estado_orden", "fecha_creacion", "fecha_asignacion", "fecha_despacho", "fecha_ruta", "fecha_llegada", "TIEMPO_TRA", "etr"};

            String sTemp = setValuesByKey(keys, oAtrr);
            sTemp = sTemp.replace("Id ", "ID ");
            sTemp = sTemp.replace(" Orden", "");
            sTemp = sTemp.replace("TIEMPO TRA", "Tiempo transcurrido");
            sTemp = sTemp.replace("Etr", "ETR");

            outStr.append(sTemp);

        } else if (identResult.getLayerName().equalsIgnoreCase("Equipos_linea_006")) {

            outStr.append("Equipo Linea");
            if (oAtrr.containsKey("id_equipo") && !oAtrr.get("id_equipo").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("id_equipo").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"tipo", "nombre", "alimentador", "descripcion", "propiedad", "catalogo", "estado", "estado_normal", "tension", "fecha"};

            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().equalsIgnoreCase("Empalmes")) {

            outStr.append("Empalme");
            if (oAtrr.containsKey("empalme") && !oAtrr.get("empalme").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("empalme").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"nis", "tipo_nodo"};

            outStr.append(setValuesByKey(keys, oAtrr));

        } else if (identResult.getLayerName().equalsIgnoreCase("DMPS_DIRECCIONES")) {

            outStr.append("Dirección");
            if (oAtrr.containsKey("id_direccion") && !oAtrr.get("id_direccion").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("id_direccion").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"nombre_calle", "numero", "comuna", "tipo_edificacion"};

            String sTemp = setValuesByKey(keys, oAtrr);
            sTemp = sTemp.replace("Tipo ", "");

            outStr.append(sTemp);

        } else if (identResult.getLayerName().equalsIgnoreCase("LUMINARIAS")) {

            outStr.append("Luminaria");
            if (oAtrr.containsKey("potencia") && !oAtrr.get("potencia").toString().trim().isEmpty())
                outStr.append(": " + oAtrr.get("potencia").toString());
            outStr.append(LSP);
            outStr.append(LSP);

            String[] keys = {"tipo_cnx", "catalogo", "propiedad", "descripcion", "nm_comuna"};

            String sTemp = setValuesByKey(keys, oAtrr);
            sTemp = sTemp.replace(" Cnx", " Conexión");
            sTemp = sTemp.replace("Nm ", "");

            outStr.append(sTemp);
        } else {
            isOrdenable = false;
        }

        if (!isOrdenable) {
            for (Map.Entry<String, Object> oKeyVal : oAtrr.entrySet()) {
                //Log.w("MultiIdentifyResults", String.format("%s : %s", oKeyVal.getKey(), oKeyVal.getValue()));

                if (!oKeyVal.getKey().contains("id_") &&
                        !oKeyVal.getKey().contains("ID_") &&
                        !oKeyVal.getKey().equalsIgnoreCase("OBJECTID") &&
                        !oKeyVal.getKey().contains("SHAPE")) {

                    outStr.append(String.format("%s: %s", formatCapitalize(oKeyVal.getKey()), oKeyVal.getValue()));
                    outStr.append(LSP);
                }
            }

            if (outStr.length() > 0) outStr.deleteCharAt(outStr.length() - 1);
        }

        return outStr;
    }

    @NonNull
    private String setValuesByKey(String[] keys, Map<String, Object> oAtrr) {
        String LSP = System.getProperty("line.separator");
        StringBuilder outStr = new StringBuilder();

        for (String key : keys) {
            //13/03/2017 se muestran tal cual
            //if (oAtrr.containsKey(key) && !oAtrr.get(key).toString().trim().isEmpty()) {
            if (oAtrr.containsKey(key)) {
                outStr.append(String.format("%s: %s", formatCapitalize(key), oAtrr.get(key).toString()));
                outStr.append(LSP);
            }
        }

        if (outStr.length() > 0) outStr.deleteCharAt(outStr.length() - 1);

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

    public Point calculateCenterByPoints(Point oPointIni, Point oPointFin) {
        Point center = new Point();
        double valX, valY;

        valX = (oPointIni.getX() + oPointFin.getX()) / 2;
        valY = (oPointIni.getY() + oPointFin.getY()) / 2;

        center.setXY(valX, valY);

        return center;
    }

    public static String getVersionPackage() {
        return String.format(" v%sc%s", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
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
                    Log.w("permissionCheck", "permision null");
                }
                return sImei;
            } catch (Exception ex) {
                Log.w("permissionCheck", "permision error: " + ex.getMessage());
                return "null";
            }

        } else {
            return getImei(activity.getApplicationContext());
        }
    }

    public static String getImei(Context c) {
        TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return telephonyManager.getDeviceId();
        }
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

    public static void showConfirmation(Context ctx, String txt) {
        AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
        alertDialog.setTitle("Confirmación");
        alertDialog.setMessage(txt);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
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

    public static String getMapTelemedida(int res) {
        String sValue;
        switch (res) {
            case R.id.txtNis:
                sValue = "PRODUCTO";
                break;
            case R.id.txtNumOlca:
                sValue = "NUMERO_OLCA";
                break;
            case R.id.txtNomCliente:
                sValue = "NOMBRE_CLIENTE";
                break;
            case R.id.txtNumMedidor:
                sValue = "NUMERO_MEDIDOR";
                break;
            case R.id.spinnerMarca:
                sValue = "MARCA";
                break;
            case R.id.spinnerModelo:
                sValue = "MODELO";
                break;
            case R.id.spinnerTipoEmpalme:
                sValue = "TIPO_EMPALME";
                break;
            case R.id.txtPoste:
                sValue = "ROTULO";
                break;
            case R.id.txtDireccion:
                sValue = "DIRECCION";
                break;
            case R.id.estEmpalme:
                sValue = "REV_EMPALME";
                break;
            case R.id.estCaja:
                sValue = "REV_CAJA_MEDIDOR";
                break;
            case R.id.estMedidor:
                sValue = "REV_MEDIDOR";
                break;
            case R.id.chkCCP:
                sValue = "LOP_CORR_PARTIDA";
                break;
            case R.id.chkGEV:
                sValue = "LOP_GIRO_VACIO";
                break;
            case R.id.txtPatronPc110:
                sValue = "POT1PCPE1";
                break;
            case R.id.txtPatronPc210:
                sValue = "POT1PCPE2";
                break;
            case R.id.txtPatronPc310:
                sValue = "POT1PCPE3";
                break;
            case R.id.txtMedidorPc110:
                sValue = "POT1PCME1";
                break;
            case R.id.txtMedidorPc210:
                sValue = "POT1PCME2";
                break;
            case R.id.txtMedidorPc310:
                sValue = "POT1PCME3";
                break;
            case R.id.txtPatronDc110:
                sValue = "POT1DCPE1";
                break;
            case R.id.txtPatronDc210:
                sValue = "POT1DCPE2";
                break;
            case R.id.txtPatronDc310:
                sValue = "POT1DCPE3";
                break;
            case R.id.txtMedidorDc110:
                sValue = "POT1DCME1";
                break;
            case R.id.txtMedidorDc210:
                sValue = "POT1DCME2";
                break;
            case R.id.txtMedidorDc310:
                sValue = "POT1DCME3";
                break;
            case R.id.txtPatronPc105:
                sValue = "POT05PCPE1";
                break;
            case R.id.txtPatronPc205:
                sValue = "POT05PCPE21";
                break;
            case R.id.txtPatronPc305:
                sValue = "POT05PCPE3";
                break;
            case R.id.txtMedidorPc105:
                sValue = "POT05PCME1";
                break;
            case R.id.txtMedidorPc205:
                sValue = "POT05PCME2";
                break;
            case R.id.txtMedidorPc305:
                sValue = "POT05PCME3";
                break;
            case R.id.txtPatronDc105:
                sValue = "POT05DCPE1";
                break;
            case R.id.txtPatronDc205:
                sValue = "POT05DCPE2";
                break;
            case R.id.txtPatronDc305:
                sValue = "POT05DCPE3";
                break;
            case R.id.txtMedidorDc105:
                sValue = "POT05DCME1";
                break;
            case R.id.txtMedidorDc205:
                sValue = "POT05DCME2";
                break;
            case R.id.txtMedidorDc305:
                sValue = "POT05DCME3";
                break;
            case R.id.txtPercPc110:
                sValue = "POT1PCPTJ1";
                break;
            case R.id.txtPercPc210:
                sValue = "POT1PCPTJ2";
                break;
            case R.id.txtPercPc310:
                sValue = "POT1PCPTJ3";
                break;
            case R.id.txtPercDc110:
                sValue = "POT1DCPTJ1";
                break;
            case R.id.txtPercDc210:
                sValue = "POT1DCPTJ2";
                break;
            case R.id.txtPercDc310:
                sValue = "POT1DCPTJ3";
                break;
            case R.id.txtPercPc105:
                sValue = "POT05PCPTJ1";
                break;
            case R.id.txtPercPc205:
                sValue = "POT05PCPTJ2";
                break;
            case R.id.txtPercPc305:
                sValue = "POT05PCPTJ3";
                break;
            case R.id.txtPercDc105:
                sValue = "POT05DCPTJ1";
                break;
            case R.id.txtPercDc205:
                sValue = "POT05DCPTJ2";
                break;
            case R.id.txtPercDc305:
                sValue = "POT05DCPTJ3";
                break;
            case R.id.chkVerif1:
                sValue = "REV_CAMARA_REGISTRO";
                break;
            case R.id.chkVerif2:
                sValue = "REV_TIERRA_SERVICIO";
                break;
            case R.id.chkVerif3:
                sValue = "REV_TIERRA_PROTECCION";
                break;
            case R.id.chkVerif4:
                sValue = "REV_DIFERENCIAL";
                break;
            case R.id.chkVerif5:
                sValue = "REV_PROTECCION_AUTO";
                break;
            default:
                sValue = null;
                break;
        }
        return sValue;
    }

    public static void QueryNavigation(Context oCtx, final Point point) {
        try
        {
            String url = String.format("geo:%s,%s", point.getY(), point.getX());
            Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
            oCtx.startActivity( intent );
        }
        catch ( ActivityNotFoundException ex  )
        {
            try {
                String url = String.format("google.navigation:q=%s,%s", point.getY(), point.getX());
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
                oCtx.startActivity( intent );
            }
            catch (Exception e)
            {
                Intent intent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                oCtx.startActivity(intent);
            }
        }
    }

    public static void QueryWazeDef(Context oCtx, final Point point) {
        try
        {
            String url = String.format("waze://?ll=%s,%s&navigate=yes", point.getY(), point.getX());
            Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
            oCtx.startActivity( intent );
        }
        catch ( ActivityNotFoundException ex  )
        {
            try {
                String url = String.format("google.navigation:q=%s,%s", point.getY(), point.getX());
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
                oCtx.startActivity( intent );
            }
            catch (Exception e)
            {
                Intent intent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                oCtx.startActivity(intent);
            }
        }
    }

    public static String extraerNum(String cadena){

        String num = "";

        char[] arreglo = cadena.toCharArray();
        for (char caracter : arreglo){
            if ( Character.isDigit(caracter) )
                num += caracter;
        }

        return num;
    }
}
