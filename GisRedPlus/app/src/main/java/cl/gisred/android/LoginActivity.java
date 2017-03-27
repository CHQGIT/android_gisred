package cl.gisred.android;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.AsyncTask;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.io.EsriSecurityException;
import com.esri.core.io.EsriServiceException;
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
    Bundle bundle;

    private ProgressDialog progress;

    private ArcGISFeatureLayer oLayerAccess;
    private String sNomEquipo;
    private String sImei;
    private String sFecha;

    private String sError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button btnIngresar = (Button) findViewById(R.id.btnLogin);
        final EditText txtUsuario = (EditText) findViewById(R.id.usuario);
        final EditText txtPassword = (EditText) findViewById(R.id.password);

        sNomEquipo = Util.getDeviceName();

        if (Build.VERSION.SDK_INT >= 23) verifPermisos();
        else sImei = Util.getImei(getApplicationContext());

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
            sError = "Ocurrió un problema al ingresar";
            AsyncQueryTask queryTask = new AsyncQueryTask();
            queryTask.execute(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(LoginActivity.this, sError, Toast.LENGTH_SHORT).show();
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

                if (results != null && results.featureCount() == 0)
                    sError = "Usuario ingresado no tiene permiso móvil";

                return results;
            } catch (EsriSecurityException esec) {
                esec.printStackTrace();
                sError = "Hubo un problema con credenciales en dominio " + domain;
                return null;
            } catch (EsriServiceException eser){
                eser.printStackTrace();
                sError = "Ocurrió un error en el servidor GISRED";
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                sError = "Existe un problema de conectividad, intente nuevamente";
                return null;
            }
        }

        protected void onPostExecute(FeatureResult results) {

            ArrayList arrayModulos = new ArrayList();
            ArrayList arrayEmpresas = new ArrayList();
            ArrayList arrayWidgets = new ArrayList();
            String userDominio = new String();

            if (results != null && results.featureCount() > 0) {
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

                        arrayWidgets.add(empModule + "@" + feature.getAttributeValue("widget"));
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
                bundle.putStringArrayList("widgets", arrayWidgets);
                bundle.putString("imei", sImei);

                Map<String, Object> attributes = new HashMap<>();

                attributes.put("usuario", credenciales.getUserName());
                attributes.put("fecha", sFecha);
                attributes.put("pagina", "Mobile");
                attributes.put("modulo", "GISRED 2.0" + Util.getVersionPackage());
                attributes.put("nom_equipo", sNomEquipo);
                attributes.put("ip", sImei);

                Graphic newFeature = new Graphic(null, null, attributes);
                Graphic[] addsLogin = {newFeature};

                oLayerAccess.applyEdits(addsLogin, null, null, callBackUnion());

                progress.dismiss();
                Intent intent = new Intent(getApplicationContext(), EmpActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);

            } else {
                progress.dismiss();
                Toast.makeText(getApplicationContext(), sError, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private CallbackListener<FeatureEditResult[][]> callBackUnion() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //Toast.makeText(LoginActivity.this, "Registrando ingreso", Toast.LENGTH_SHORT).show();
            }
        });

        return null;
    }

    private void verifPermisos() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        Util.REQUEST_READ_PHONE_STATE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            sImei = Util.getImei(getApplicationContext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Util.REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    sImei = Util.getImei(getApplicationContext());

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Log.w("LoginActivity", "No hay permisos de READ_PHONE_STATE");
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
