package cl.gisred.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.AsyncTask;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cl.gisred.android.util.Util;

public class LoginActivity extends AppCompatActivity {

    public String usuario, password, domain;
    UserCredentials credenciales;
    private int[] listSpinners = {1, 4, 5, 6, 7, 10, 11};
    Bundle bundleSpinner = null;
    int contValuesSpinner;
    Bundle bundle;

    private ProgressDialog progress;

    private ArcGISFeatureLayer oLayerAccess;
    private String sNomEquipo;
    private String sImei;
    private String sFecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button btnIngresar = (Button) findViewById(R.id.btnLogin);
        final EditText txtUsuario = (EditText) findViewById(R.id.usuario);
        final EditText txtPassword = (EditText) findViewById(R.id.password);

        sNomEquipo = Util.getDeviceName();
        sImei = Util.getImei(getApplicationContext());
        sFecha = DateFormat.format("dd-MM-yyyy HH:mm:ss", new java.util.Date()).toString();

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{

                    if (txtUsuario != null) {
                        usuario = txtUsuario.getText().toString();
                    }
                    if (txtPassword != null) {
                        password = txtPassword.getText().toString();
                    }
                    //Set Credenciales
                    setCredenciales(usuario, password);

                    if (usuario.isEmpty() || password.isEmpty()){
                        Toast.makeText(LoginActivity.this, "Ingrese credenciales", Toast.LENGTH_SHORT).show();

                    } else {
                        Consulta_permisos_usuario();
                    }
                }
                catch (Exception e) {
                    Log.e("[LoginActivity]", e.getMessage());
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        domain = getResources().getString(R.string.domainDef);
    }

    public void setCredenciales(String user , String pass) {
        credenciales = new UserCredentials();
        credenciales.setUserAccount(domain + "\\" + user, pass);
        oLayerAccess = new ArcGISFeatureLayer(getResources().getString(R.string.srv_LogAccess), ArcGISFeatureLayer.MODE.ONDEMAND, credenciales);
    }

    public void Consulta_permisos_usuario()
    {
        try {
            AsyncQueryTask queryTask = new AsyncQueryTask();
            queryTask.execute(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(LoginActivity.this, "Ocurrió un problema al ingresar", Toast.LENGTH_SHORT).show();
        }
    }

    private class AsyncQueryTask extends AsyncTask<String, Void, FeatureResult> {

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(LoginActivity.this);

            progress = ProgressDialog.show(LoginActivity.this, "",
                    "Espere por favor... Verificando usuario.");
        }

        @Override
        protected FeatureResult doInBackground(String... params) {
            String whereClause = "usuario = '" + domain + "\\" + params[0] + "' AND plataforma = 'MOVIL'";
            QueryParameters myParameters = new QueryParameters();
            myParameters.setWhere(whereClause);
            myParameters.setReturnGeometry(false);
            String[] outfields = new String[]{"usuario", "modulo", "widget", "insert_", "delete_", "update_", "select_", "empresa"};
            myParameters.setOutFields(outfields);

            FeatureResult results;
            try {
                QueryTask queryTask = new QueryTask("http://gisred.chilquinta.cl:5555/arcgis/rest/services/Admin/LogAccesos/MapServer/2", credenciales);
                results = queryTask.execute(myParameters);

                if (results != null && results.featureCount() == 0) results = null;

                return results;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(FeatureResult results) {

            ArrayList arrayModulos = new ArrayList();
            ArrayList arrayEmpresas = new ArrayList();
            String userDominio = new String();
            boolean isIngClientes = false;

            if (results != null) {
                int size = (int) results.featureCount();

                SharedPreferences prefs = getSharedPreferences("GisRedPrefs",Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("username", usuario);
                editor.putString("password", password);
                editor.apply();

                for (Object element : results) {
                    progress.incrementProgressBy(size / 100);
                    if (element instanceof Feature) {
                        Feature feature = (Feature) element;

                        String empModule = "";

                        if (feature.getAttributeValue("empresa") != null) {
                            if (!arrayEmpresas.contains(feature.getAttributeValue("empresa"))) {
                                arrayEmpresas.add(feature.getAttributeValue("empresa"));
                            }
                            empModule = feature.getAttributeValue("empresa").toString();
                        }

                        if (empModule.isEmpty()) empModule = "chilquinta"; //for default

                        arrayModulos.add(empModule + "@" + feature.getAttributeValue("modulo"));
                        userDominio = (String) feature.getAttributeValue("usuario");

                        if (((String) feature.getAttributeValue("modulo")).contains("CLIENTES")) {
                            isIngClientes = true;
                        }
                    }
                }

                CharSequence Cs1 = domain + "\\";
                boolean retval = userDominio.contains(Cs1);

                bundle = new Bundle();

                if (retval) bundle.putString("usuarioLogin", domain + "\\" + usuario);
                else bundle.putString("usuarioLogin", usuario);

                bundle.putString("passwordLogin", password);
                bundle.putStringArrayList("modulos", arrayModulos);
                bundle.putStringArrayList("empresas", arrayEmpresas);

                Map<String, Object> attributes = new HashMap<>();

                attributes.put("usuario", credenciales.getUserName());
                attributes.put("fecha", sFecha);
                attributes.put("pagina", "Mobile");
                attributes.put("modulo", "GISRED 2.0");
                attributes.put("nom_equipo", sNomEquipo);
                attributes.put("ip", sImei);

                Graphic newFeature = new Graphic(null, null, attributes);
                Graphic[] addsLogin = {newFeature};

                oLayerAccess.applyEdits(addsLogin, null, null, callBackUnion());

                if (isIngClientes) {
                    getSpinnerValues(listSpinners);
                } else {
                    progress.dismiss();
                    Intent intent = new Intent(getApplicationContext(), EmpActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

            } else {
                progress.dismiss();
                Toast.makeText(getApplicationContext(), "Login Incorrecto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getSpinnerValues(int[] values) {
        bundleSpinner = new Bundle();
        contValuesSpinner = values.length;

        if (progress != null) progress.dismiss();
        progress = ProgressDialog.show(LoginActivity.this, "",
                "Espere por favor... Cargando opciones adicionales.");

        for (int value : values) {
            String op = "";

            switch (value) {
                case 1:
                    op="tipoCnr";
                    break;
                case 4:
                    op="tipoPoste";
                    break;
                case 5:
                    op="tipoTension";
                    break;
                case 6:
                    op="tipoEdif";
                    break;
                case 7:
                    op="tipoMedidor";
                    break;
                case 10:
                    op="tipoEmpalme";
                    break;
                case 11:
                    op="tecMedidor";
                    break;
            }

            SpinnerQuerytask qTask = new SpinnerQuerytask();
            qTask.execute("" + value, op);
        }
    }

    private class SpinnerQuerytask extends AsyncTask<String, Void, FeatureResult> {

        String sOpcion = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected FeatureResult doInBackground(String... params) {

            QueryParameters myParameters = new QueryParameters();
            myParameters.setWhere("1 = 1");
            myParameters.setReturnGeometry(false);
            String[] outfields = new String[]{"*"};
            myParameters.setOutFields(outfields);

            FeatureResult results;
            try {
                if (credenciales.getPassword().isEmpty()) return null;
                int resUrl = (Integer.valueOf(params[0]) >= 4) ? R.string.url_tipos_spinner : R.string.url_cnr_spinner;
                String url = getResources().getString(resUrl) + "/" + params[0];
                QueryTask queryTask = new QueryTask(url , credenciales);
                results = queryTask.execute(myParameters);

                sOpcion = params[1];

                return results;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(FeatureResult results) {
            super.onPostExecute(results);

            if (results != null) {
                int size = (int) results.featureCount();
                String[] strings = new String[size];
                int cont = 0;

                for (Object element : results) {
                    //progress.incrementProgressBy(size / 100);
                    if (element instanceof Feature) {
                        Feature feature = (Feature) element;

                        for (Map.Entry<String, Object> entry : feature.getAttributes().entrySet()) {

                            if (entry.getValue().getClass().equals(String.class)) {
                                strings[cont] = entry.getValue().toString();
                            }
                        }
                    }
                    cont++;
                }

                bundleSpinner.putStringArray(sOpcion, strings);
            }

            if (contValuesSpinner--<=1) {
                progress.dismiss();

                bundle.putBundle("options", bundleSpinner);
                Intent intent = new Intent(getApplicationContext(), EmpActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    private CallbackListener<FeatureEditResult[][]> callBackUnion() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, "Registrando ingreso", Toast.LENGTH_SHORT).show();
            }
        });

        return null;
    }

}
