package cl.gisred.android;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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


public class SplashActivity extends AppCompatActivity {


    private View mContentView;
    public String usuario, password, domain;
    UserCredentials credenciales;
    private ProgressDialog progress;

    Bundle bundle;

    private ArcGISFeatureLayer oLayerAccess;
    private String sNomEquipo;
    private String sImei;
    private String sFecha;

    private String sError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        mContentView = findViewById(R.id.imageView);

        if (Build.VERSION.SDK_INT >= 23) verifPermisos();

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Autenticación en progreso...",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifPermisos() {

        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(SplashActivity.this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        Util.REQUEST_READ_PHONE_STATE);
            }
        } else {
            verifLogin();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        domain = getResources().getString(R.string.domainDef);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT < 23) verifLogin();
        else verifPermisos();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    public void setCredenciales(String user , String pass) {
        credenciales = new UserCredentials();
        credenciales.setUserAccount(domain + "\\" + user, pass);
        oLayerAccess = new ArcGISFeatureLayer(getResources().getString(R.string.srv_LogAccess), ArcGISFeatureLayer.MODE.ONDEMAND, credenciales);
    }

    private void verifLogin() {
        boolean validacion = true;

        SharedPreferences oPref = getSharedPreferences("GisRedPrefs", Context.MODE_PRIVATE);
        if (oPref.contains("username") && oPref.contains("password")) {
            usuario = oPref.getString("username", "");
            password = oPref.getString("password", "");

            if (usuario.isEmpty() || password.isEmpty()) {
                validacion = false;
            } else {
                setCredenciales(usuario, password);

                sNomEquipo = Util.getDeviceName();
                sImei = Util.getImei(getApplicationContext());
                sFecha = DateFormat.format("dd-MM-yyyy HH:mm:ss", new java.util.Date()).toString();

                try {
                    AsyncQueryTask queryTask = new AsyncQueryTask();
                    queryTask.execute(usuario);
                } catch (Exception e) {
                    e.printStackTrace();
                    validacion = false;
                }
            }
        } else {
            validacion = false;
        }

        if (!validacion) {
            Intent oIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(oIntent);
        }
    }

    private class AsyncQueryTask extends AsyncTask<String, Void, FeatureResult> {

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(SplashActivity.this);
            progress = ProgressDialog.show(SplashActivity.this, "", "Recuperando credenciales");
        }

        @Override
        protected FeatureResult doInBackground(String... params) {

            String whereClause = "usuario = '"+ domain +"\\" + params[0] + "' AND plataforma = 'MOVIL'";
            QueryParameters myParameters = new QueryParameters();
            myParameters.setWhere(whereClause);
            myParameters.setReturnGeometry(false);
            String[] outfields = new String[]{"usuario", "modulo", "widget", "insert_", "delete_", "update_", "select_", "empresa"};
            myParameters.setOutFields(outfields);

            FeatureResult results;
            try {
                QueryTask queryTask = new QueryTask(getResources().getString(R.string.url_permisos), credenciales);
                results = queryTask.execute(myParameters);

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
                sError = "Existe un problema de conectividad, verifique";
                return null;
            }
        }

        protected void onPostExecute(FeatureResult results) {

            ArrayList arrayEmpresas = new ArrayList();
            ArrayList arrayModulos = new ArrayList();
            ArrayList arrayWidgets = new ArrayList();
            String userDominio = new String();

            if (results != null && results.featureCount() > 0) {
                int size = (int) results.featureCount();

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

                oLayerAccess.applyEdits(addsLogin, null, null, callBackIngreso());

                progress.dismiss();
                Intent intent = new Intent(getApplicationContext(), EmpActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);

            } else {
                progress.dismiss();
                Intent oIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(oIntent);
            }
        }
    }


    private CallbackListener<FeatureEditResult[][]> callBackIngreso() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //Toast.makeText(SplashActivity.this, "Registrando ingreso", Toast.LENGTH_SHORT).show();
            }
        });

        return null;
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

                    verifLogin();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Log.w("SplashActivity", "No hay permisos de READ_PHONE_STATE");
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
