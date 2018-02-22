package cl.gisred.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.query.Order;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import cl.gisred.android.entity.InspLectClass;
import cl.gisred.android.util.Util;


public class OtListActivity extends AppCompatActivity {


    private InspLectClass[] datos;
    private ArrayList<InspLectClass> datosTotales;
    private int iCantDatosTot = 0;

    private Feature[] features;
    private ArrayList<Feature> featuresTotales;

    private ListView lstOpciones;
    ArrayList<String> aForms;
    private String sEmpresa;
    private Bundle bundle;

    private ProgressDialog progress;

    String usuario, password;
    UserCredentials credenciales;

    ArcGISFeatureLayer LyAddMicroOt;
    ArcGISFeatureLayer LyAddOpenOt;
    ArcGISFeatureLayer LyAddDenuncioOt;

    private ArcGISFeatureLayer LySelectOt;

    int iCapaOt = 0;

    // Variables de acceso

    public void setCredenciales(String usuario , String password) {
        credenciales = new UserCredentials();
        credenciales.setUserAccount(usuario, password);
    }

    private void getOrdenesByTipo() {

        datosTotales = new ArrayList<>();
        featuresTotales = new ArrayList<>();

        try {
            AsyncQueryTask queryTask = new AsyncQueryTask();
            String sUser = Util.getUserWithoutDomain(usuario);
            queryTask.execute(sUser);
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(getApplicationContext(), "Usted no tiene órdenes asignadas", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawListaOT() {
        try {
            datos = new InspLectClass[datosTotales.size()];
            datos = datosTotales.toArray(datos);

            features = new Feature[featuresTotales.size()];
            features = featuresTotales.toArray(features);

            AdaptadorOTList adaptador;
            adaptador = new AdaptadorOTList(OtListActivity.this, datos);

            lstOpciones.setAdapter(adaptador);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Usted no tiene OT asignadas", Toast.LENGTH_LONG).show();
            if (lstOpciones != null && lstOpciones.getCount() > 0)
                lstOpciones.setAdapter(null);
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        TextView txtTitulo = (TextView) findViewById(R.id.LblEtiqueta);
        txtTitulo.setText("Manten presionado para marcar como atendida");

        bundle = getIntent().getExtras();
        aForms = bundle.getStringArrayList("widgets");
        usuario = bundle.getString("usuario");
        password = bundle.getString("password");
        sEmpresa = bundle.getString("empresa");

        setCredenciales(usuario, password);

        lstOpciones = (ListView) findViewById(R.id.LstOpciones);
        getOrdenesByTipo();

        //Set Layer
        LyAddMicroOt = new ArcGISFeatureLayer(getResources().getString(R.string.srv_micromed_OT), ArcGISFeatureLayer.MODE.ONDEMAND, credenciales);
        LyAddOpenOt = new ArcGISFeatureLayer(getResources().getString(R.string.srv_viaopen_OT), ArcGISFeatureLayer.MODE.ONDEMAND, credenciales);
        LyAddDenuncioOt = new ArcGISFeatureLayer(getResources().getString(R.string.srv_denuncio_OT), ArcGISFeatureLayer.MODE.ONDEMAND, credenciales);

        lstOpciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Object oItem = parent.getItemAtPosition(position);

                Intent oIntent = new Intent(OtListActivity.this, OtRouteActivity.class);
                bundle.putString("objID", datos[position].getObjectId());
                bundle.putString("typFeat", datos[position].getTipo());
                oIntent.putExtras(bundle);
                startActivity(oIntent);
            }
        });

        lstOpciones.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                String sMsj = "¿OT id " + datos[position].getObjectId() + " fue atendida?";
                AlertDialog.Builder builder = new AlertDialog.Builder(OtListActivity.this);
                final int pos = position;

                builder.setMessage(sMsj)
                        .setTitle("Confirmacion")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cierraOT(datos[pos]);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                builder.create().show();
                return true;
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        getOrdenesByTipo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.insp_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                getOrdenesByTipo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void cierraOT(final InspLectClass oDato) {

        final AtomicReference<String> resp = new AtomicReference<>("");

        Map<String, Object> updMap = new HashMap<>();

        updMap.put("OBJECTID", Integer.valueOf(oDato.getObjectId()));
        updMap.put("estado_revision", "atendida");

        if (oDato.getTipo().equals("Micromedicion")) {
            LySelectOt = LyAddMicroOt;
        } else if (oDato.getTipo().equals("Denuncio")) {
            LySelectOt = LyAddDenuncioOt;
        } else {
            LySelectOt = LyAddOpenOt;
        }

        Graphic newFeatureGraphic = new Graphic(null, null, updMap);
        Graphic[] upds = {newFeatureGraphic};
        LySelectOt.applyEdits(null, null, upds, new CallbackListener<FeatureEditResult[][]>() {
            @Override
            public void onCallback(FeatureEditResult[][] featureEditResults) {
                if (featureEditResults[2] != null) {
                    if (featureEditResults[2][0] != null && featureEditResults[2][0].isSuccess()) {

                        resp.set("OT " + oDato.getOt() + " atendida");

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), resp.get(), Toast.LENGTH_SHORT).show();
                                getOrdenesByTipo();
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error al cerrar OT", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    class AdaptadorOTList extends ArrayAdapter<InspLectClass> {

        public AdaptadorOTList(Context context, InspLectClass[] datos) {
            super(context, R.layout.list_item_insp_lect, datos);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View item = inflater.inflate(R.layout.list_item_insp_lect, null);

            TextView lblTitulo = (TextView)item.findViewById(R.id.LblTitulo);
            lblTitulo.setText("OT: " + datos[position].getOt());

            datos[position] = getDataByState(datos[position]);

            TextView lblDescripcion = (TextView)item.findViewById(R.id.LblEstDenuncio);
            lblDescripcion.setText("Tipo: " + datos[position].getTipo());

            TextView lblEstado = (TextView)item.findViewById(R.id.LblEstado);
            lblEstado.setText("Estado: " + datos[position].getEstado());

            TextView lblOt = (TextView)item.findViewById(R.id.LblOT);
            lblOt.setText("Id: " + datos[position].getObjectId());

            ImageView oImage = (ImageView) item.findViewById(R.id.imageMenu);
            oImage.setImageResource(datos[position].getRes());

            return(item);
        }

        private InspLectClass getDataByState(InspLectClass dato) {

            dato.setRes((dato.isLeida()) ? R.mipmap.ic_menu_ing_lectores : R.mipmap.ic_menu_ing_lectores_g);
            return dato;
        }
    }

    private class AsyncQueryTask extends AsyncTask<String, Void, FeatureResult> {

        String sCapa;
        FeatureResult oResultOpen;
        FeatureResult oResultDenuncio;
        ArrayList<FeatureResult> aFeaturesOT;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(OtListActivity.this);

            progress = ProgressDialog.show(OtListActivity.this, "",
                    "Consultando asignaciones");

            progress.setCancelable(true);
            progress.setCanceledOnTouchOutside(true);
        }

        @Override
        protected FeatureResult doInBackground(String... params) {

            FeatureResult results;
            try {

                results = getFeaturesOT(params[0], "0");
                oResultDenuncio = getFeaturesOT(params[0], "1");
                oResultOpen = getFeaturesOT(params[0], "2");

                aFeaturesOT = new ArrayList<>();
                aFeaturesOT.add(results);
                aFeaturesOT.add(oResultDenuncio);
                aFeaturesOT.add(oResultOpen);

                return results;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(FeatureResult results) {

            if (results != null && aFeaturesOT != null && aFeaturesOT.size() >= 0) {

                for (FeatureResult featureResult : aFeaturesOT) {

                    for (Object element : featureResult) {

                        if (element instanceof Feature) {
                            Feature feature = (Feature) element;

                            featuresTotales.add(feature);

                            String sObjId, sRev, sTipo;
                            String sEstado = "";
                            String sOt = "";
                            int iSec = 0;

                            if (feature.getAttributes().containsKey("estado_ot")) {
                                sTipo = "Micromedicion";
                            } else if (feature.getAttributes().containsKey("estado_open")) {
                                sTipo = "Via Open";
                            } else {
                                sTipo = "Denuncio";
                            }

                            sRev = feature.getAttributeValue("estado_revision").toString();
                            sObjId = feature.getAttributeValue("OBJECTID").toString();

                            try {
                                if (feature.getAttributeValue("ot") != null)
                                    sOt = feature.getAttributeValue("ot").toString();

                                if (feature.getAttributeValue("estado") != null)
                                    sEstado = feature.getAttributeValue("estado").toString();

                                iSec = (int) feature.getAttributeValue("secuencia");

                            } catch (Exception e) {
                                Log.e("iSecuencia", "Null Value");
                            }

                            InspLectClass oInsp = new InspLectClass(sObjId, sEstado, sRev, sOt, sTipo, iSec);
                            datosTotales.add(oInsp);
                        }
                    }
                }

                try {
                    Collections.sort(datosTotales, InspLectClass.InspSec);
                } catch (Exception e) {
                    Log.e("SortArray", "Error al ordenar por secuencia");
                }

                drawListaOT();

                progress.dismiss();

            } else {
                //Error en la consulta
                progress.dismiss();
            }
        }
    }

    private FeatureResult getFeaturesOT(String sInspector, String sCapa) {
        String whereClause;
        String[] outfields;

        whereClause = "inspector = '" + sInspector + "' AND inspeccion = 'SI' AND estado_denuncio = 'EN GESTION' AND estado_revision in ('asignada', 'leida')";
        outfields = new String[]{"*"};

        Map<String, Order> orderFields = new LinkedHashMap<>();
        orderFields.put("secuencia", Order.ASC);
        QueryParameters myParameters = new QueryParameters();
        myParameters.setOrderByFields(orderFields);
        myParameters.setWhere(whereClause);
        myParameters.setReturnGeometry(false);
        myParameters.setOutFields(outfields);

        FeatureResult results;
        try {
            QueryTask queryTask;

            if (sCapa.equals("0"))
                queryTask = new QueryTask(getResources().getString(R.string.srv_micromed_OT), credenciales);
            else if (sCapa.equals("2"))
                queryTask = new QueryTask(getResources().getString(R.string.srv_viaopen_OT), credenciales);
            else
                queryTask = new QueryTask(getResources().getString(R.string.srv_denuncio_OT), credenciales);

            results = queryTask.execute(myParameters);
            return results;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}




