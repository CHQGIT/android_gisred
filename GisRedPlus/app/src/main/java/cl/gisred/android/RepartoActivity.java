package cl.gisred.android;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.LocationDisplayManager.AutoPanMode;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.bing.BingMapsLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.Graphic;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import cl.gisred.android.entity.RepartoClass;
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

    ArcGISDynamicMapServiceLayer LyMapabase;
    ArcGISFeatureLayer LyReparto;

    //Set bing Maps
    String BingKey = "Asrn2IMtRwnOdIRPf-7q30XVUrZuOK7K2tzhCACMg7QZbJ4EPsOcLk6mE9-sNvUe";
    final BingMapsLayer mAerialBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.AERIAL);
    final BingMapsLayer mAerialWLabelBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.AERIAL_WITH_LABELS);
    final BingMapsLayer mRoadBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.ROAD);

    //Sets
    ArrayList<String> arrayWidgets;

    private static final String CLIENT_ID = "ZWIfL6Tqb4kRdgZ4";
    private MyLocationListener oLocList;

    // Spatial references used for projecting points
    final SpatialReference wm = SpatialReference.create(102100);
    final SpatialReference egs = SpatialReference.create(4326);

    EditText txtListen;
    TextView txtContador;
    TextView txtContSesion;
    int iContRep = 0;
    int iContRepSesion = 0;
    boolean bGpsActive = false;
    boolean bBlueActive = true;

    public RepartoSQLiteHelper sqlReparto;
    final String dbName = "DbRepartos.db";

    ArrayList<RepartoClass> arrayDatos;

    RepartoService mService;
    boolean mBound = false;

    private BluetoothAdapter bAdapter;
    private ArrayList<BluetoothDevice> arrayDevices;

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
        setLayersURL(this.getResources().getString(R.string.srv_Repartos), "SRV_REPARTO");

        LyMapabase = new ArcGISDynamicMapServiceLayer(din_urlMapaBase, null, credenciales);
        LyMapabase.setVisible(true);

        LyReparto = new ArcGISFeatureLayer(srv_reparto, ArcGISFeatureLayer.MODE.ONDEMAND, credenciales);
        LyReparto.setDefinitionExpression(String.format("empresa = '%s'", empresa));
        LyReparto.setMinScale(8000);
        LyReparto.setVisible(true);

        myMapView.addLayer(mRoadBaseMaps, 0);
        myMapView.addLayer(LyReparto, 1);

        sqlReparto =  new RepartoSQLiteHelper(RepartoActivity.this, dbName, null, 2);

        txtContador = (TextView) findViewById(R.id.tvContador);
        txtContSesion = (TextView) findViewById(R.id.tvContadorSesion);

        txtListen = (EditText) findViewById(R.id.txtListen);
        txtListen.setEnabled(bGpsActive);
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
                    guardarRegistro(s.toString().trim());
                    s.clear();
                }
            }
        });

        final FloatingActionButton btnGps = (FloatingActionButton) findViewById(R.id.action_gps);
        btnGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    myMapView.setExtent(ldm.getPoint());
                    myMapView.setScale(4000, true);
                }
            }
        });

        myMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
            @Override
            public void onStatusChanged(Object o, STATUS status) {
                if (ldm != null) {
                    Point oPoint = ldm.getPoint();
                    myMapView.centerAndZoom(oPoint.getX(), oPoint.getY(), 0.003f);
                    myMapView.zoomin(true);
                }
            }
        });

        readCountData();
        updDashboard();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        if (iContRep > 0) {
                            Toast.makeText(getApplicationContext(), "Sincronizando datos...", Toast.LENGTH_SHORT).show();
                            readData();
                            enviarDatos();
                        }
                    }
                });

            }
        }, 0, 120000);
    }

    private void updDashboard(){

        String sTextCont = getResources().getString(R.string.tvCont);
        sTextCont = sTextCont + " " + String.valueOf(iContRep);

        txtContador.setText(sTextCont);

        String sTextSesion = getResources().getString(R.string.tvContSesion);
        sTextSesion = sTextSesion + " " + String.valueOf(iContRepSesion);

        txtContSesion.setText(sTextSesion);
    }

    private void guardarRegistro(String sValue) {
        if (RepartoClass.valCode(sValue)) {
            if (insertData(sValue)){
                iContRep++;
                iContRepSesion++;
                updDashboard();
            }
            else
                Toast.makeText(RepartoActivity.this, "Error: registro " + sValue+ " no guardado", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(RepartoActivity.this, "Lectura con valor no válido: " + sValue, Toast.LENGTH_SHORT).show();
            alertFail();
        }
    }

    private void alertFail() {
        ToneGenerator tgFail = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        tgFail.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 200);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
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

        Intent intent = new Intent(this, RepartoService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
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
                //copiarBaseDatos();
                readData();
                enviarDatos();
                //queryService();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to RepartoService, cast the IBinder and get RepartoService instance
            RepartoService.RepartoBinder binder = (RepartoService.RepartoBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void queryService() {
        if (mBound) {
            // Call a method from the RepartoService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.
            int num = mService.getRandomNumber();
        }
    }

    private void startGPS() {
        oLocList = new MyLocationListener();
        ldm = myMapView.getLocationDisplayManager();
        ldm.setLocationListener(new MyLocationListener());
        ldm.start();
        ldm.setAutoPanMode(AutoPanMode.LOCATION);

        setStateGPS();
    }

    private boolean verifGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertNoGps();
            return false;
        }
        return true;
    }

    private void setStateGPS() {
        bGpsActive = verifGPS();
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

    private void toogleBlockReader(boolean bActive, int iReason) {
        txtListen.setEnabled(bActive);
        if (!bActive) {
            txtListen.setHint(iReason);
            txtListen.setHintTextColor(Color.RED);
        }
        else if (bGpsActive && bBlueActive){
            txtListen.setHint(iReason);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                txtListen.setHintTextColor(getResources().getColor(R.color.green, getTheme()));
            else
                txtListen.setHintTextColor(getResources().getColor(R.color.green));
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

        if (iContRep > 0) {
            iContRep--;
            updDashboard();
        }

    }

    public void enviarDatos() {
        final AtomicReference<String> resp = new AtomicReference<>("");

        for (RepartoClass rep: arrayDatos) {

            final RepartoClass repActual = rep;
            Map<String, Object> objectMap = new HashMap<>();

            objectMap.put("nis", rep.getNis());
            objectMap.put("valor_captura", rep.getCodigo());
            objectMap.put("empresa", empresa);
            objectMap.put("modulo", modulo);

            Point oUbicacion = new Point(rep.getX(), rep.getY());

            Graphic newFeatureGraphic = new Graphic(oUbicacion, null, objectMap);
            Graphic[] adds = {newFeatureGraphic};
            LyReparto.applyEdits(adds, null, null, new CallbackListener<FeatureEditResult[][]>() {
                @Override
                public void onCallback(FeatureEditResult[][] featureEditResults) {
                    if (featureEditResults[0] != null) {
                        if (featureEditResults[0][0] != null && featureEditResults[0][0].isSuccess()) {

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    deleteData(repActual.getId());
                                }
                            });
                        }
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    resp.set("Error al ingresar: " + throwable.getLocalizedMessage());

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(RepartoActivity.this, resp.get(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    public void readCountData() {

        SQLiteDatabase db = sqlReparto.getReadableDatabase();
        String[] sValues = {"id", "codigo", "x", "y"};
        Cursor cData = db.query("repartos", sValues, null, null, null, null, null, null);

        if (cData != null && cData.getCount() >= 0) {
            iContRep = cData.getCount();
            cData.close();
        }

        db.close();
    }

    public void readData() {

        SQLiteDatabase db = sqlReparto.getReadableDatabase();
        String[] sValues = {"id", "codigo", "x", "y"};
        Cursor cData = db.query("repartos", sValues, null, null, null, null, null, null);
        arrayDatos = new ArrayList<>();

        if (cData != null && cData.getCount() > 0) {
            iContRep = cData.getCount();

            cData.moveToFirst();

            do {
                RepartoClass oRep = new RepartoClass(cData.getInt(0), cData.getString(1), cData.getDouble(2), cData.getDouble(3));

                arrayDatos.add(oRep);
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

            boolean zoomToMe = (mLocation == null);
            mLocation = new Point(loc.getLongitude(), loc.getLatitude());
            oUbicActual = (Point) GeometryEngine.project(mLocation, egs, wm);
            if (zoomToMe)
                myMapView.zoomToResolution(oUbicActual, 5.0);
            else
                myMapView.centerAt(oUbicActual, true);
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS Deshabilitado", Toast.LENGTH_SHORT).show();
            bGpsActive = false;
            toogleBlockReader(bGpsActive, R.string.txtListenGps);
            alertNoGps();
        }

        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS Habilitado", Toast.LENGTH_SHORT).show();
            bGpsActive = true;
            toogleBlockReader(bGpsActive, R.string.txtListen);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private final RepartoReceiver bReceiver = new RepartoReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            // Filtramos por la accion. Nos interesa detectar BluetoothAdapter.ACTION_STATE_CHANGED
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                Toast.makeText(getApplicationContext(), "Action: " + action, Toast.LENGTH_SHORT).show();
            }
            // Nuevo dispositivo por Bluetooth, se ejecutara este fragmento de codigo
            else if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Acciones a realizar al descubrir un nuevo dispositivo
            }

            // Codigo que se ejecutara cuando el Bluetooth finalice la busqueda de dispositivos.
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                // Acciones a realizar al finalizar el proceso de descubrimiento
            }
        }
    };

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
