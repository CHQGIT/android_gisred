package cl.gisred.android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.Toast;

import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.LocationDisplayManager.AutoPanMode;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.bing.BingMapsLayer;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.io.UserCredentials;
import com.esri.core.runtime.LicenseLevel;
import com.esri.core.runtime.LicenseResult;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import cl.gisred.android.util.Util;

public class RepartoActivity extends AppCompatActivity {

    MapView myMapView = null;

    LocationDisplayManager ldm;
    public static Point mLocation = null;
    private Point oUbicActual;

    //INSTANCES
    UserCredentials credenciales;
    String usuar, passw, modulo, empresa;

    //url para token srv
    String urlToken;
    String din_urlMapaBase;

    //url para feature layers
    String srv_reparto;

    ArcGISDynamicMapServiceLayer LyReparto, LyMapabase;

    //Set bing Maps
    String BingKey = "Asrn2IMtRwnOdIRPf-7q30XVUrZuOK7K2tzhCACMg7QZbJ4EPsOcLk6mE9-sNvUe";
    final BingMapsLayer mAerialBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.AERIAL);
    final BingMapsLayer mAerialWLabelBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.AERIAL_WITH_LABELS);
    final BingMapsLayer mRoadBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.ROAD);

    //Sets
    ArrayList<String> arrayWidgets;

    boolean bAlertGps = false;

    private static final String CLIENT_ID = "ZWIfL6Tqb4kRdgZ4";
    private MyLocationListener oLocList;

    //set Extent inicial
    Polygon mCurrentMapExtent = null;
    // Spatial references used for projecting points
    final SpatialReference wm = SpatialReference.create(102100);
    final SpatialReference egs = SpatialReference.create(4326);

    EditText txtListen;
    int iContRep = 0;

    public RepartoSQLiteHelper sqlReparto;
    final String dbName = "DbRepartos.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LicenseResult licenseResult = ArcGISRuntime.setClientId(CLIENT_ID);
        LicenseLevel licenseLevel = ArcGISRuntime.License.getLicenseLevel();

        if (licenseResult == LicenseResult.VALID && licenseLevel == LicenseLevel.BASIC) {
            //Toast.makeText(getApplicationContext(), "Licencia básica válida", Toast.LENGTH_SHORT).show();
        } else if (licenseResult == LicenseResult.VALID && licenseLevel == LicenseLevel.STANDARD) {
            //Toast.makeText(getApplicationContext(), "Licencia standard válida", Toast.LENGTH_SHORT).show();
        }

        setContentView(R.layout.activity_reparto);

        Toolbar toolbar = (Toolbar) findViewById(R.id.apptool);
        setSupportActionBar(toolbar);

        myMapView = (MapView) findViewById(R.id.map);
        myMapView.enableWrapAround(true);
    
        /*Get Credenciales String*/
        Bundle bundle = getIntent().getExtras();
        usuar = bundle.getString("usuario");
        passw = bundle.getString("password");
        modulo = bundle.getString("modulo");
        empresa = bundle.getString("empresa");

        //Set Credenciales
        setCredenciales(usuar, passw);

        if (Build.VERSION.SDK_INT >= 23) verifPermisos();
        else startGPS();

        setLayersURL(this.getResources().getString(R.string.url_Mapabase), "MAPABASE");
        setLayersURL(this.getResources().getString(R.string.url_token), "TOKENSRV");
        setLayersURL(this.getResources().getString(R.string.url_EquiposLinea), "SRV_REPARTO");

        LyMapabase = new ArcGISDynamicMapServiceLayer(din_urlMapaBase, null, credenciales);
        LyMapabase.setVisible(true);

        myMapView.addLayer(mRoadBaseMaps, 0);

        sqlReparto =  new RepartoSQLiteHelper(RepartoActivity.this, dbName, null, 2);

        txtListen = (EditText) findViewById(R.id.txtListen);
        if (!txtListen.hasFocus()) txtListen.requestFocus();
        txtListen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().contains("\n")) {
                    iContRep++;
                    guardarRegistro(s.toString().trim());
                    s.clear();
                }
            }
        });

        Toast.makeText(RepartoActivity.this, getDatabasePath(dbName).toString(), Toast.LENGTH_LONG).show();
    }

    private void guardarRegistro(String sValue) {
        if (insertData(sValue))
            Toast.makeText(RepartoActivity.this, iContRep + " value: " + sValue, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(RepartoActivity.this, "Error: registro " + sValue+ " no guardado", Toast.LENGTH_SHORT).show();
    }

    private void verifPermisos() {
        if (ContextCompat.checkSelfPermission(RepartoActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(RepartoActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(RepartoActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Util.REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            //Marcar como encendido
            startGPS();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                copiarBaseDatos();
                readData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startGPS() {
        oLocList = new MyLocationListener();
        ldm = myMapView.getLocationDisplayManager();
        ldm.setLocationListener(oLocList);
        ldm.start();
        ldm.setAutoPanMode(AutoPanMode.LOCATION);

        setStateGPS();
    }

    private void verifGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertNoGps();
        }
    }

    private void setStateGPS() {
        verifGPS();
        //Marcar estado
    }

    public void setCredenciales(String usuario, String password) {
        credenciales = new UserCredentials();
        credenciales.setUserAccount(usuario, password);
    }

    private String getValueByEmp(String s) {

        switch (empresa) {
            case "litoral":
                s = s.replace("006", "009");
                break;
            case "casablanca":
                s = s.replace("006", "028");
                break;
            case "linares":
                s = s.replace("006", "031");
                break;
            case "parral":
                s = s.replace("006", "032");
                break;
        }

        return s;
    }

    public void setLayersURL(String layerURL, String tipo) {
        layerURL = getValueByEmp(layerURL);

        switch (tipo) {
            case "MAPABASE":
                din_urlMapaBase = layerURL;
                break;
            case "TOKENSRV":
                urlToken = layerURL;
                break;
            case "SRV_REPARTO":
                srv_reparto = layerURL;
                break;
            default:
                Toast.makeText(RepartoActivity.this, "Problemas inicializando layers url", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void alertNoGps() {
        final AlertDialog alertGps;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alertGps = builder.create();
        alertGps.show();
    }

    private boolean insertData(String sValue) {

        SQLiteDatabase db = sqlReparto.getWritableDatabase();
        long nIns = -1;

        if(db != null) {

            ContentValues valores = new ContentValues();
            valores.put("codigo", sValue);
            valores.put("x", oUbicActual.getX());
            valores.put("y", oUbicActual.getY());
            nIns = db.insert("repartos", null, valores);

            db.close();
        }

        return nIns > 0;
    }

    public void deleteData(int id) {
        SQLiteDatabase db = sqlReparto.getWritableDatabase();
        db.delete("repartos", "id=" + id, null);
        db.close();
    }

    public void readData() {
        SQLiteDatabase db = sqlReparto.getReadableDatabase();
        String[] sValues = {"id", "codigo", "x", "y"};
        Cursor cData = db.query("repartos", sValues, null, null, null, null, null, null);

        if (cData != null && cData.getCount() > 0) {

            Toast.makeText(RepartoActivity.this, "Count: "+cData.getCount(), Toast.LENGTH_SHORT).show();
            cData.moveToFirst();

            do {
                Toast.makeText(RepartoActivity.this, "id: " + cData.getInt(0) + " value: " + cData.getString(1), Toast.LENGTH_SHORT).show();
            } while (cData.moveToNext());

            cData.close();
        }

        db.close();
    }

    private void copiarBaseDatos() {
        String ruta = String.format("%s/db/", getExternalCacheDir().getAbsolutePath());
        String archivo = dbName;

        File tempDir = new File(ruta);
        if (!tempDir.exists()) {
            tempDir.mkdir();
        } else {
            File[] archivos = tempDir.listFiles();
            for (File arch : archivos)
                arch.delete();
        }

        File archivoDB = new File(ruta + archivo);
        if (!archivoDB.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(getDatabasePath(dbName));
                InputStream IS = fileInputStream;
                OutputStream OS = new FileOutputStream(archivoDB);
                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = IS.read(buffer)) > 0) {
                    OS.write(buffer, 0, length);
                }
                OS.flush();
                OS.close();
                IS.close();
            } catch (FileNotFoundException e) {
                Log.e("ERROR", "Archivo no encontrado, " + e.toString());
            } catch (IOException e) {
                Log.e("ERROR", "Error al copiar la Base de Datos, " + e.toString());
            }
        }
    }

    private class RepartoSQLiteHelper extends SQLiteOpenHelper {

        //Sentencia SQL para crear la tabla de Usuarios
        String sqlCreate = "CREATE TABLE repartos (id INTEGER PRIMARY KEY AUTOINCREMENT, codigo TEXT, x NUMERIC, y NUMERIC)";

        public RepartoSQLiteHelper(Context contexto, String nombre,
                                    SQLiteDatabase.CursorFactory factory, int version) {
            super(contexto, nombre, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //Se ejecuta la sentencia SQL de creación de la tabla
            db.execSQL(sqlCreate);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {

            //Se elimina la versión anterior de la tabla
            db.execSQL("DROP TABLE IF EXISTS repartos");

            //Se crea la nueva versión de la tabla
            db.execSQL(sqlCreate);
        }
    }

    private class MyLocationListener implements LocationListener {

        public MyLocationListener() {
            super();
        }

        public void onLocationChanged(Location loc) {

            if (loc == null)
                return;

            mLocation = new Point(loc.getLongitude(), loc.getLatitude());
            oUbicActual = (Point) GeometryEngine.project(mLocation, egs, wm);
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS Deshabilitado", Toast.LENGTH_SHORT).show();
            alertNoGps();
        }

        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS Habilitado", Toast.LENGTH_SHORT).show();
            bAlertGps = false;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Util.REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    setStateGPS();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    //SIN PERMISOS
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
