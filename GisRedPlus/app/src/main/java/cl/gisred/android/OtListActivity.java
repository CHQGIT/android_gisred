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

import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import cl.gisred.android.entity.InspLectClass;


public class OtListActivity extends AppCompatActivity {


    private InspLectClass[] datos;
    private Feature[] features;
    private ListView lstOpciones;
    ArrayList<String> aForms;
    private String sEmpresa;
    private Bundle bundle;

    private ProgressDialog progress;

    String usuario, password;
    UserCredentials credenciales;

    //ArcGISFeatureLayer LyAddOt;
    ArcGISDynamicMapServiceLayer LyAddOt;

    // Variables de acceso

    public void setCredenciales(String usuario , String password) {
        credenciales = new UserCredentials();
        credenciales.setUserAccount(usuario, password);
    }

    public void getOT()
    {
        try {
            lstOpciones = (ListView) findViewById(R.id.LstOpciones);
            //lstOpciones.removeAllViews();

            AsyncQueryTask queryTask = new AsyncQueryTask();
            String sBackSlash = String.valueOf("\\\\");
            String sUserDom = usuario.replace("\\", sBackSlash);
            String sUserNoDom = sUserDom.split(sBackSlash)[2];

            queryTask.execute(sUserNoDom);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Usted no tiene órdenes asignadas", Toast.LENGTH_SHORT).show();
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
        getOT();

        //Set Layer
        //LyAddOt = new ArcGISFeatureLayer(getResources().getString(R.string.url_OT), ArcGISFeatureLayer.MODE.ONDEMAND, credenciales);
        int array1[]; //declaracion arreglo de tipo numerico
        array1 = new int[1];
        array1[0] = 0;
        LyAddOt = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.url_OT), array1, credenciales);

        lstOpciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Object oItem = parent.getItemAtPosition(position);

                Intent oIntent = new Intent(OtListActivity.this, OtRouteActivity.class);
                bundle.putString("objID", datos[position].getObjectId());
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
                                cierraOT(features[pos]);
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

        getOT();
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
                getOT();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void cierraOT(final Feature oFeature) {

        final AtomicReference<String> resp = new AtomicReference<>("");

        /*Map<String, Object> objectMap = oFeature.getAttributes();
        Map<String, Object> updMap = new HashMap<>();

        updMap.put("OBJECTID", objectMap.get("OBJECTID"));
        updMap.put("ESTADO", "atendida");

        Graphic newFeatureGraphic = new Graphic(oFeature.getGeometry(), null, updMap);
        Graphic[] upds = {newFeatureGraphic};
        LyAddOt.applyEdits(null, null, upds, new CallbackListener<FeatureEditResult[][]>() {
            @Override
            public void onCallback(FeatureEditResult[][] featureEditResults) {
                if (featureEditResults[2] != null) {
                    if (featureEditResults[2][0] != null && featureEditResults[2][0].isSuccess()) {

                        resp.set("OT id " + oFeature.getId() + " atendida");

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), resp.get(), Toast.LENGTH_SHORT).show();
                                getOT();
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
        });*/

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
            lblDescripcion.setText("Zona: " + datos[position].getEstado());

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

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(OtListActivity.this);

            progress = ProgressDialog.show(OtListActivity.this, "",
                    "Consultando asignaciones");
        }

        @Override
        protected FeatureResult doInBackground(String... params) {
            //String whereClause = "INSPECTOR = '" + params[0] + "' AND ESTADO in ('asignada', 'leida')";
            String whereClause = "INSPECTOR = '" + params[0] + "'";
            QueryParameters myParameters = new QueryParameters();
            myParameters.setWhere(whereClause);
            myParameters.setReturnGeometry(false);
            String[] outfields = new String[]{"OBJECTID", "ESTADO", "ZONA", "OT"};
            myParameters.setOutFields(outfields);

            FeatureResult results;
            try {
                QueryTask queryTask = new QueryTask(getResources().getString(R.string.url_OT), credenciales);
                results = queryTask.execute(myParameters);
                return results;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(FeatureResult results) {

            if (results != null && results.featureCount() > 0) {
                int size = (int) results.featureCount();

                datos = new InspLectClass[size];
                features = new Feature[size];

                int cont = 0;

                for (Object element : results) {
                    progress.incrementProgressBy(size / 100);
                    if (element instanceof Feature) {
                        Feature feature = (Feature) element;

                        features[cont] = feature;

                        String sObjId = feature.getAttributeValue("OBJECTID").toString();
                        String sEstado = feature.getAttributeValue("ZONA").toString();
                        String sRev = feature.getAttributeValue("ZONA").toString();
                        String sOt = feature.getAttributeValue("OT").toString();

                        InspLectClass oInsp = new InspLectClass(sObjId, sEstado, sRev, sOt);
                        datos[cont] = oInsp;
                        cont++;
                    }
                }

                if (datos != null && datos.length > 0) {
                    AdaptadorOTList adaptador;
                    adaptador = new AdaptadorOTList(OtListActivity.this, datos);

                    lstOpciones.setAdapter(adaptador);
                }

                progress.dismiss();

            } else {
                Toast.makeText(getApplicationContext(), "Usted no tiene OT asignadas", Toast.LENGTH_LONG).show();
                if (lstOpciones != null && lstOpciones.getCount() > 0)
                    lstOpciones.setAdapter(null);
                progress.dismiss();
            }
        }
    }
}




