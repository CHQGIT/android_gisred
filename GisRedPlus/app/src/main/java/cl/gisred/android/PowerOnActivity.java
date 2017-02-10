package cl.gisred.android;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.LocationDisplayManager.AutoPanMode;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.bing.BingMapsLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.io.EsriSecurityException;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.runtime.LicenseLevel;
import com.esri.core.runtime.LicenseResult;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.identify.IdentifyParameters;
import com.esri.core.tasks.identify.IdentifyResult;
import com.esri.core.tasks.identify.IdentifyTask;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import cl.gisred.android.classes.GisEditText;
import cl.gisred.android.classes.GisTextView;
import cl.gisred.android.entity.CalloutTvClass;
import cl.gisred.android.util.Util;

public class PowerOnActivity extends AppCompatActivity {

    ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();
    MapView myMapView = null;

    LocationDisplayManager ldm;
    public static Point mLocation = null;

    //INSTANCES
    UserCredentials credenciales;
    String usuar, passw, modulo, empresa;

    //url para token srv
    String urlToken;

    //variable para guardar spinner seleccionado en Dialog  de Busqueda
    int SpiBusqueda;
    String txtBusqueda;

    //ArrayList MapsType
    public String[] tipoMapas = {"Carreteras", "Aerea", "Aerea Detalles", "Chilquinta"};

    //ArrayList SearchFilter
    public String[] searchArray = {"Clientes", "SED", "Poste", "Medidor", "Dirección", "ID Orden", "ID Incidencia"};

    //ArrayList Layer
    public String[] listadoCapas = {"SED", "SSEE", "Salida Alimentador", "Red MT", "Red BT", "Red AP", "Postes", "Equipos Linea", "Equipos Puntos", "Luminarias", "Clientes", "Medidores",
            "Concesiones", "Direcciones", "Empalmes", "Red sTX", "Torres sTX", "ECSE Encuestados", "ECSE Reemplazos", "PO SED", "PO Tramos", "PO Clientes"};

    public boolean fool[] = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true};

    //url para dinamyc layers
    String din_urlMapaBase, din_urlEquiposPunto, din_urlEquiposLinea, din_urlTramos, din_urlNodos, din_urlLuminarias, din_urlClientes, din_urlConcesiones, din_urlMedidores, din_urlDirecciones, din_urlStx, din_urlInterrupciones, din_urlECSE;

    //Set bing Maps
    String BingKey = "Asrn2IMtRwnOdIRPf-7q30XVUrZuOK7K2tzhCACMg7QZbJ4EPsOcLk6mE9-sNvUe";
    final BingMapsLayer mAerialBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.AERIAL);
    final BingMapsLayer mAerialWLabelBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.AERIAL_WITH_LABELS);
    final BingMapsLayer mRoadBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.ROAD);

    ArcGISDynamicMapServiceLayer LySED, LySSEE, LySALIDAALIM, LyREDMT, LyREDBT, LyREDAP, LyPOSTES, LyEQUIPOSLINEA, LyEQUIPOSPTO, LyLUMINARIAS, LyCLIENTES, LyMEDIDORES, LyCONCESIONES, LyDIRECCIONES, LyEMPALMES, LyMapabase, LyREDSTX, LyTORRESSTX, LyENCUESTA, LyREEMPLAZO, LyPOSED, LyPOTRAMO, LyPOCLIENTES;

    //set Extent inicial
    Polygon mCurrentMapExtent = null;
    // Spatial references used for projecting points
    final SpatialReference wm = SpatialReference.create(102100);
    final SpatialReference egs = SpatialReference.create(4326);

    //Constantes
    private static final String modPowerOn = "INTERRUPCIONES";

    //Sets
    ArrayList<String> arrayWidgets;
    ArrayList<String> arrayModulos;
    private int choices;
    ProgressDialog progress;

    boolean bAlertGps = false;

    GraphicsLayer mBusquedaLayer;
    GraphicsLayer mSeleccionLayer;
    GraphicsLayer mUbicacionLayer;
    private double iBusqScale;

    ShapeDrawable drawOk;
    ShapeDrawable drawNo;
    private boolean bVerData = false;
    private boolean bVerCapas = false;
    private boolean bMapTap = false;
    private boolean bCallOut = false;
    private int nIndentify = 0;
    private GisEditText oTxtAsoc;
    private GraphicsLayer oLyViewGraphs;
    private ArcGISFeatureLayer oLySelectAsoc;
    private ArcGISDynamicMapServiceLayer oLyExistAsoc;
    private ArcGISFeatureLayer oLyAddGraphs;
    private int idResLayoutSelect;
    private Point oUbicacion;

    Dialog dialogCrear;
    Dialog formCrear;
    Dialog dialogCur;
    FloatingActionsMenu menuPowerActions;
    FloatingActionButton fabShowForm;
    FloatingActionButton fabNavRoute;

    private static final String CLIENT_ID = "ZWIfL6Tqb4kRdgZ4";

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

        setContentView(R.layout.activity_power);

        Toolbar toolbar = (Toolbar) findViewById(R.id.apptool);
        setSupportActionBar(toolbar);
    
        /*Get Credenciales String*/
        Bundle bundle = getIntent().getExtras();
        usuar = bundle.getString("usuario");
        passw = bundle.getString("password");
        modulo = bundle.getString("modulo");
        empresa = bundle.getString("empresa");

        //Set Credenciales
        setCredenciales(usuar, passw);

        //Set Mapa
        setMap(R.id.map, 0xffffff, 0xffffff, 10, 10, false, true);
        choices = 0;

        if (Build.VERSION.SDK_INT >= 23) verifPermisos();
        else initGeoposition();

        setLayersURL(this.getResources().getString(R.string.url_Mapabase), "MAPABASE");
        setLayersURL(this.getResources().getString(R.string.url_token), "TOKENSRV");
        setLayersURL(this.getResources().getString(R.string.url_EquiposLinea), "EQUIPOS_LINEA");
        setLayersURL(this.getResources().getString(R.string.url_TRAMOS), "TRAMOS");
        setLayersURL(this.getResources().getString(R.string.url_EquiposPTO), "EQUIPOS_PTO");
        setLayersURL(this.getResources().getString(R.string.url_Nodos), "NODOS");
        setLayersURL(this.getResources().getString(R.string.url_Luminarias), "LUMINARIAS");
        setLayersURL(this.getResources().getString(R.string.url_Clientes), "CLIENTES");
        setLayersURL(this.getResources().getString(R.string.url_Concesiones), "CONCESIONES");
        setLayersURL(this.getResources().getString(R.string.url_Direcciones), "DIRECCIONES");
        setLayersURL(this.getResources().getString(R.string.url_medidores), "MEDIDORES");
        setLayersURL(this.getResources().getString(R.string.url_Stx), "STX");
        setLayersURL(this.getResources().getString(R.string.url_interrupciones), "PO");
        setLayersURL(this.getResources().getString(R.string.url_ECSE_varios), "ECSE");

        //Agrega layers dinámicos.
        addLayersToMap(credenciales, "DYNAMIC", "MAPABASECHQ", din_urlMapaBase, null, true);
        addLayersToMap(credenciales, "DYNAMIC", "SED", din_urlEquiposPunto, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "SSEE", din_urlEquiposPunto, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "SALIDAALIM", din_urlEquiposPunto, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "REDMT", din_urlTramos, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "REDBT", din_urlTramos, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "REDAP", din_urlTramos, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "POSTES", din_urlNodos, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "EQUIPOS_LINEA", din_urlEquiposLinea, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "EQUIPOS_PTO", din_urlEquiposPunto, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "LUMINARIAS", din_urlLuminarias, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "CLIENTES", din_urlClientes, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "MEDIDORES", din_urlMedidores, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "CONCESIONES", din_urlConcesiones, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "DIRECCIONES", din_urlDirecciones, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "EMPALMES", din_urlClientes, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "REDSTX", din_urlStx, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "TORRESSTX", din_urlStx, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "ENCUESTADO", din_urlECSE, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "REEMPLAZO", din_urlECSE, null, false);
        addLayersToMap(credenciales, "DYNAMIC", "POSED", din_urlInterrupciones, null, true);
        addLayersToMap(credenciales, "DYNAMIC", "POTRAMO", din_urlInterrupciones, null, true);
        addLayersToMap(credenciales, "DYNAMIC", "POCLIENTES", din_urlInterrupciones, null, true);

        //Añade Layer al Mapa
        myMapView.addLayer(mRoadBaseMaps, 0);
        myMapView.addLayer(LySED, 1);
        myMapView.addLayer(LySSEE, 2);
        myMapView.addLayer(LySALIDAALIM, 3);
        myMapView.addLayer(LyREDMT, 4);
        myMapView.addLayer(LyREDBT, 5);
        myMapView.addLayer(LyREDAP, 6);
        myMapView.addLayer(LyPOSTES, 7);
        myMapView.addLayer(LyEQUIPOSLINEA, 8);
        myMapView.addLayer(LyEQUIPOSPTO, 9);
        myMapView.addLayer(LyLUMINARIAS, 10);
        myMapView.addLayer(LyCLIENTES, 11);
        myMapView.addLayer(LyMEDIDORES, 12);
        myMapView.addLayer(LyCONCESIONES, 13);
        myMapView.addLayer(LyDIRECCIONES, 14);
        myMapView.addLayer(LyEMPALMES, 15);
        myMapView.addLayer(LyREDSTX, 16);
        myMapView.addLayer(LyTORRESSTX, 17);
        myMapView.addLayer(LyENCUESTA, 18);
        myMapView.addLayer(LyREEMPLAZO, 19);
        myMapView.addLayer(LyPOSED, 20);
        myMapView.addLayer(LyPOTRAMO, 21);
        myMapView.addLayer(LyPOCLIENTES, 22);


        final FloatingActionButton btnGps = (FloatingActionButton) findViewById(R.id.action_gps);
        btnGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    alertNoGps();
                }
                toogleGps(v);
            }
        });

        btnGps.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Función Gps", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        final FloatingActionButton btnVerData = (FloatingActionButton) findViewById(R.id.action_ver_data);
        btnVerData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogleData(v);
            }
        });

        btnVerData.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Ver Datos", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        drawOk = new ShapeDrawable(new OvalShape());
        drawOk.getPaint().setColor(getResources().getColor(R.color.colorPrimary));

        drawNo = new ShapeDrawable(new OvalShape());
        drawNo.getPaint().setColor(getResources().getColor(R.color.black_overlay));

        menuPowerActions = (FloatingActionsMenu) findViewById(R.id.power_actions);

        fabShowForm = (FloatingActionButton) findViewById(R.id.action_show_form);
        if (fabShowForm != null) fabShowForm.setVisibility(View.GONE);

        fabNavRoute = (FloatingActionButton) findViewById(R.id.action_nav_route);
        if (fabNavRoute != null) {
            fabNavRoute.setVisibility(View.GONE);
            fabNavRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myMapView != null && myMapView.getCallout().isShowing()) {
                        Point p = (Point) GeometryEngine.project(myMapView.getCallout().getCoordinates(), wm, egs);
                        Util.QueryWaze(PowerOnActivity.this, p);
                    }
                }
            });

            fabNavRoute.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(getApplicationContext(), "Ir a Ruta", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        if (modulo.replace(" ", "_").equals(modPowerOn)) {

            arrayWidgets = bundle.getStringArrayList("widgets");
            arrayModulos = bundle.getStringArrayList("modulos");

            FloatingActionButton oFabView = (FloatingActionButton) findViewById(R.id.action_view);
            oFabView.setIconDrawable(drawOk);
            oFabView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirLeyenda();
                }
            });

        } else {
            menuPowerActions.setVisibility(View.GONE);
        }
    }

    private void abrirLeyenda() {
        ImageView image = new ImageView(this);
        image.setImageResource(R.drawable.leyenda_power);
        image.setAdjustViewBounds(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this).
                setPositiveButton("CERRAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).
                setView(image);

        builder.create().show();
    }

    private void verifPermisos() {
        if (ContextCompat.checkSelfPermission(PowerOnActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(PowerOnActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(PowerOnActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Util.REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            initGeoposition();
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
        getMenuInflater().inflate(R.menu.map_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        boolean retVal;
        switch (item.getItemId()) {

            case R.id.action_layers:
                alertMultipleChoiceItems();
                return true;

            case R.id.action_search:
                dialogBusqueda();
                return true;

            case R.id.action_maps:
                choiceMaps(choices);
                return true;

            default:
                retVal = super.onOptionsItemSelected(item);
                break;
        }

        return retVal;
    }

    private View getLayoutContenedor(View v) {
        if (v.getClass().equals(LinearLayout.class))
            return v;
        else return getLayoutContenedor((View) v.getParent());
    }

    private void setValueToAsoc(View v) {
        for (View view : v.getTouchables()) {
            if (view.getClass().getGenericSuperclass().equals(EditText.class)) {
                oTxtAsoc = (GisEditText) view;
            }
        }
    }

    public void getTramoToDialog(Point oSelect) {

        if (oSelect != null) {
            IdentifyParameters params = new IdentifyParameters();
            // Add to Identify Parameters based on tapped location
            params.setTolerance(20);
            params.setDPI(98);
            params.setLayers(new int[] { 1 });
            params.setLayerMode(IdentifyParameters.VISIBLE_LAYERS);
            params.setGeometry(oSelect);
            params.setSpatialReference(myMapView.getSpatialReference());
            params.setMapHeight(myMapView.getHeight());
            params.setMapWidth(myMapView.getWidth());
            params.setReturnGeometry(false);

            // add the area of extent to identify parameters
            Envelope env = new Envelope();
            myMapView.getExtent().queryEnvelope(env);
            params.setMapExtent(env);

            String urlTest = oLySelectAsoc.getUrl();

            // execute the identify task off UI thread
            IdentifyResults mTask = new IdentifyResults("id_tramo", oSelect, urlTest, credenciales);
            mTask.execute(params);
        }
    }

    public void getInfoObject(Point oSelect, ArcGISDynamicMapServiceLayer[] aLayers) {

        if (oSelect != null) {
            IdentifyParameters params = new IdentifyParameters();
            // Add to Identify Parameters based on tapped location
            params.setTolerance(20);
            params.setDPI(98);
            params.setLayerMode(IdentifyParameters.VISIBLE_LAYERS);
            params.setGeometry(oSelect);
            params.setSpatialReference(myMapView.getSpatialReference());
            params.setMapHeight(myMapView.getHeight());
            params.setMapWidth(myMapView.getWidth());
            params.setReturnGeometry(true);

            // add the area of extent to identify parameters
            Envelope env = new Envelope();
            myMapView.getExtent().queryEnvelope(env);
            params.setMapExtent(env);

            // execute the identify task off UI thread
            IdentifyResults mTask = new IdentifyResults(oSelect, aLayers, credenciales);
            mTask.execute(params);
        }
    }

    public void getAsocObject(Point oSelect) {

        if (oSelect != null) {
            IdentifyParameters params = new IdentifyParameters();
            // Add to Identify Parameters based on tapped location
            params.setTolerance(20);
            params.setDPI(98);
            params.setLayerMode(IdentifyParameters.ALL_LAYERS);
            params.setGeometry(oSelect);
            params.setSpatialReference(myMapView.getSpatialReference());
            params.setMapHeight(myMapView.getHeight());
            params.setMapWidth(myMapView.getWidth());
            params.setReturnGeometry(true);

            // add the area of extent to identify parameters
            Envelope env = new Envelope();
            myMapView.getExtent().queryEnvelope(env);
            params.setMapExtent(env);

            // execute the identify task off UI thread
            IdentifyResults mTask = new IdentifyResults(oSelect, new String[]{oLyExistAsoc.getUrl()}, credenciales, true);
            mTask.execute(params);
        }
    }

    public void getCalleToDialog(Point oSelect) {
        if (oSelect != null) {
            IdentifyParameters params = new IdentifyParameters();
            params.setTolerance(20);
            params.setDPI(98);
            params.setLayers(new int[]{2});
            params.setLayerMode(IdentifyParameters.ALL_LAYERS);
            params.setGeometry(oSelect);
            params.setSpatialReference(myMapView.getSpatialReference());
            params.setMapHeight(myMapView.getHeight());
            params.setMapWidth(myMapView.getWidth());
            params.setReturnGeometry(false);

            Envelope env = new Envelope();
            myMapView.getExtent().queryEnvelope(env);
            params.setMapExtent(env);

            String urlTest = oLySelectAsoc.getUrl();

            // execute the identify task off UI thread
            IdentifyResults mTask = new IdentifyResults("nombre", oSelect, urlTest, credenciales);
            mTask.execute(params);
        }
    }

    private void toogleGps(View view) {
        if (ldm != null) {
            if (ldm.isStarted()) {
                ldm.stop();
                ((FloatingActionButton) view).setIcon(R.drawable.ic_gps_off_white_24dp);
            } else {
                ldm.start();
                ((FloatingActionButton) view).setIcon(R.drawable.ic_gps_fixed_white_24dp);
                myMapView.setExtent(ldm.getPoint());
                myMapView.setScale(4000, true);
            }

        } else {
            initGeoposition();
        }
    }

    private void toogleData(View view) {
        if (bVerData) {
            ((FloatingActionButton) view).setIcon(R.drawable.ic_pencil_off_white_24dp);
        } else {
            ((FloatingActionButton) view).setIcon(R.drawable.ic_pencil_white_24dp);
        }

        bVerData = !bVerData;
        fabNavRoute.setVisibility(View.GONE);
        myMapView.getCallout().hide();
    }

    private void initGeoposition() {
        ldm = myMapView.getLocationDisplayManager();
        ldm.setLocationListener(new MyLocationListener());
        ldm.start();
        ldm.setAutoPanMode(AutoPanMode.LOCATION);
    }

    private int calcRadio() {
        if (myMapView.getScale() < 1000) {
            return 50;
        } else if (myMapView.getScale() < 5000) {
            return 40;
        } else if (myMapView.getScale() < 8000) {
            return 35;
        } else if (myMapView.getScale() < 10000) {
            return 30;
        } else if (myMapView.getScale() < 20000) {
            return 20;
        } else if (myMapView.getScale() < 30000) {
            return 10;
        } else if (myMapView.getScale() < 50000) {
            return 5;
        } else if (myMapView.getScale() < 200000) {
            return 3;
        } else if (myMapView.getScale() < 1000000) {
            return 2;
        } else {
            return 1;
        }
    }

    public void setMap(int idMapa, int color1, int color2, int gridSize, int gridLine, boolean logoVisible, boolean wrapAround) {

        try {
            myMapView = (MapView) findViewById(idMapa);
            myMapView.setMapBackground(color1, color2, gridSize, gridLine);
            myMapView.setEsriLogoVisible(logoVisible);
            myMapView.enableWrapAround(wrapAround);

            //Set eventos mapa
            singleTapOnMap();
            changesOnMap();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCredenciales(String usuario, String password) {
        credenciales = new UserCredentials();
        credenciales.setUserAccount(usuario, password);
    }

    public void dialogBusqueda() {

        AlertDialog.Builder dialogBusqueda = new AlertDialog.Builder(this);
        dialogBusqueda.setTitle("Busqueda");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.dialog_busqueda, null);
        dialogBusqueda.setView(v);

        Spinner spinner = (Spinner) v.findViewById(R.id.spinnerBusqueda);
        final LinearLayout llBuscar = (LinearLayout) v.findViewById(R.id.llBuscar);
        final LinearLayout llDireccion = (LinearLayout) v.findViewById(R.id.llBuscarDir);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, searchArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpiBusqueda = position;

                if (position != 4) {
                    if (llDireccion != null) llDireccion.setVisibility(View.GONE);
                    if (llBuscar != null) llBuscar.setVisibility(View.VISIBLE);
                } else {
                    if (llDireccion != null) llDireccion.setVisibility(View.VISIBLE);
                    if (llBuscar != null) llBuscar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "Nada seleccionado", Toast.LENGTH_SHORT).show();
            }
        });

        final EditText eSearch = (EditText) v.findViewById(R.id.txtBuscar);
        final EditText eStreet = (EditText) v.findViewById(R.id.txtCalle);
        final EditText eNumber = (EditText) v.findViewById(R.id.txtNum);

        dialogBusqueda.setPositiveButton("Buscar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (SpiBusqueda == 4) {
                    txtBusqueda = new String();
                    if (!eStreet.getText().toString().isEmpty())
                        txtBusqueda = (eNumber.getText().toString().trim().isEmpty()) ? "0 " : eNumber.getText().toString().trim() + " ";
                    txtBusqueda = txtBusqueda + eStreet.getText().toString();
                } else {
                    txtBusqueda = eSearch.getText().toString();
                }

                if (txtBusqueda.trim().isEmpty()) {
                    Toast.makeText(myMapView.getContext(), "Debe ingresar un valor", Toast.LENGTH_SHORT).show();
                } else {
                    // Escala de calle para busquedas por default
                    // TODO Asignar a res values o strings
                    iBusqScale = 4000;
                    switch (SpiBusqueda) {
                        case 0:
                            callQuery(txtBusqueda, getValueByEmp("CLIENTES_XY_006.nis"), LyCLIENTES.getUrl().concat("/0"));
                            if (LyCLIENTES.getLayers() != null && LyCLIENTES.getLayers().length > 0)
                                iBusqScale = LyCLIENTES.getLayers()[0].getLayerServiceInfo().getMinScale();
                            break;
                        case 1:
                            callQuery(txtBusqueda, "codigo", LySED.getUrl().concat("/1"));
                            if (LySED.getLayers() != null && LySED.getLayers().length > 1)
                                iBusqScale = LySED.getLayers()[1].getLayerServiceInfo().getMinScale();
                            break;
                        case 2:
                            callQuery(txtBusqueda, "rotulo", LyPOSTES.getUrl().concat("/0"));
                            if (LyPOSTES.getLayers() != null && LyPOSTES.getLayers().length > 0)
                                iBusqScale = LyPOSTES.getLayers()[0].getLayerServiceInfo().getMinScale();
                            break;
                        case 3:
                            //Pendiente
                            //callQuery(txtBusqueda, "numero", din_urlMedidores);
                            break;
                        case 4:
                            iBusqScale = 5000;
                            String[] sBuscar = {eStreet.getText().toString(), eNumber.getText().toString()};
                            String[] sFields = {"nombre_calle", "numero"};
                            callQuery(sBuscar, sFields, LyDIRECCIONES.getUrl().concat("/0"));
                            break;
                        case 5:
                            callQuery(txtBusqueda, getValueByEmp("ARCGIS.dbo.POWERON_CLIENTES.id_orden"), LyPOCLIENTES.getUrl().concat("/1"));
                            if (LyPOCLIENTES.getLayers() != null && LyPOCLIENTES.getLayers().length > 0)
                                if(LyPOCLIENTES.getLayers()[1].getLayerServiceInfo().getMinScale()>0)
                                    iBusqScale = LyPOCLIENTES.getLayers()[1].getLayerServiceInfo().getMinScale();
                            break;
                        case 6:
                            callQueryInt(txtBusqueda, getValueByEmp("ARCGIS.dbo.POWERON_CLIENTES.id_incidencia"), LyPOCLIENTES.getUrl().concat("/1"));
                            if (LyPOCLIENTES.getLayers() != null && LyPOCLIENTES.getLayers().length > 0)
                                if(LyPOCLIENTES.getLayers()[1].getLayerServiceInfo().getMinScale()>0)
                                    iBusqScale = LyPOCLIENTES.getLayers()[1].getLayerServiceInfo().getMinScale();
                            break;
                    }
                }
            }
        });

        dialogBusqueda.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialogBusqueda.show();
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

    public void callQuery(String txtBusqueda, String nomCampo, String dirUrl) {
        String sWhere = String.format("%s = '%s'", nomCampo, txtBusqueda);

        AsyncQueryTask queryTask = new AsyncQueryTask();
        queryTask.execute(sWhere, dirUrl);
    }

    public void callQueryInt(String txtBusqueda, String nomCampo, String dirUrl) {
        String sWhere = String.format("%s = %s", nomCampo, txtBusqueda);

        AsyncQueryTask queryTask = new AsyncQueryTask();
        queryTask.execute(sWhere, dirUrl);
    }

    public void callQuery(String[] sBusqueda, String[] sCampos, String dirUrl) {
        String sWhere = String.format("%s = '%s'",
                sCampos[0], sBusqueda[0]);
        if (!sBusqueda[1].isEmpty()) {
            sWhere += String.format(" AND %s = '%s'", sCampos[1], sBusqueda[1]);
        }

        AsyncQueryTask queryTask = new AsyncQueryTask();
        queryTask.execute(sWhere, dirUrl);
    }

    public void choiceMaps(int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PowerOnActivity.this);

        // set the dialog title
        builder.setTitle("Mapas")
                .setSingleChoiceItems(tipoMapas, pos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case 0:
                                myMapView.removeLayer(0);
                                myMapView.addLayer(mRoadBaseMaps, 0);
                                break;
                            case 1:
                                myMapView.removeLayer(0);
                                myMapView.addLayer(mAerialBaseMaps, 0);
                                break;
                            case 2:
                                myMapView.removeLayer(0);
                                myMapView.addLayer(mAerialWLabelBaseMaps, 0);
                                break;
                            case 3:
                                myMapView.removeLayer(0);
                                myMapView.addLayer(LyMapabase, 0);
                                break;
                        }

                        choices = which;
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void alertMultipleChoiceItems() {

        mSelectedItems = new ArrayList<>();

        for (int i = 0; i < listadoCapas.length; i++) {
            fool[i] = myMapView.getLayer(i + 1).isVisible();

            if (fool[i]) {
                mSelectedItems.add(i);
            } else if (mSelectedItems.contains(i)) {
                mSelectedItems.remove(Integer.valueOf(i));
            }

            Log.w("LayerVisible", (i + 1) + " = " + fool[i] + " " + listadoCapas[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(PowerOnActivity.this);

        builder.setTitle("Capas")

                .setMultiChoiceItems(listadoCapas, fool, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (isChecked) {
                            mSelectedItems.add(which);
                        } else if (mSelectedItems.contains(which)) {
                            mSelectedItems.remove(Integer.valueOf(which));
                        }
                    }
                })

                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        setLayerOff();

                        for (Integer i : mSelectedItems) {

                            Log.w("mSelectedItems", "Visible " + i + " " + listadoCapas[i]);
                            myMapView.getLayer(i + 1).setVisible(true);
                        }
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // removes the AlertDialog in the screen
                    }
                })

                .show();
    }

    public void setLayerOff() {

        for (int i = 0; i < listadoCapas.length; i++) {
            myMapView.getLayer(i + 1).setVisible(false);
        }
    }

    public void setLayersURL(String layerURL, String tipo) {
        layerURL = getValueByEmp(layerURL);

        switch (tipo) {
            case "MAPABASE":
                din_urlMapaBase = layerURL;
                break;
            //token srv
            case "TOKENSRV":
                urlToken = layerURL;
                break;
            case "EQUIPOS_LINEA":
                din_urlEquiposLinea = layerURL;
                break;
            case "TRAMOS":
                din_urlTramos = layerURL;
                break;
            case "EQUIPOS_PTO":
                din_urlEquiposPunto = layerURL;
                break;
            case "NODOS":
                din_urlNodos = layerURL;
                break;
            case "LUMINARIAS":
                din_urlLuminarias = layerURL;
                break;
            case "CLIENTES":
                din_urlClientes = layerURL;
                break;
            case "DIRECCIONES":
                din_urlDirecciones = layerURL;
                break;
            case "MEDIDORES":
                din_urlMedidores = layerURL;
                break;
            case "CONCESIONES":
                din_urlConcesiones = layerURL;
                break;
            case "STX":
                din_urlStx = layerURL;
                break;
            case "PO":
                din_urlInterrupciones = layerURL;
                break;
            case "ECSE":
                din_urlECSE = layerURL;
                break;
            default:
                Toast.makeText(PowerOnActivity.this, "Problemas inicializando layers url", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void addLayersToMap(UserCredentials credencial, String tipoLayer, String nombreCapa, String url, String mode, boolean visibilidad) {

        // tipo layer feature
        if (tipoLayer.equals("FEATURE")) {

            switch (nombreCapa) {
                case "ALIMENTADORES":

                    if (mode.equals("SNAPSHOT")) {
                        // LyAlimentadores = new ArcGISFeatureLayer(url, ArcGISFeatureLayer.MODE.SNAPSHOT, credencial);
                    } else if (mode.equals("ONDEMAND")) {
                        // LyAlimentadores = new ArcGISFeatureLayer(url, ArcGISFeatureLayer.MODE.ONDEMAND, credencial);
                    } else if (mode.equals("SELECTION")) {
                        // LyAlimentadores = new ArcGISFeatureLayer(url, ArcGISFeatureLayer.MODE.SELECTION, credencial);
                    } else {
                        Toast.makeText(PowerOnActivity.this, "FeatureLayer debe tener un modo.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                default:
                    Toast.makeText(PowerOnActivity.this, "Problemas agregando layers url", Toast.LENGTH_SHORT).show();
                    break;
            }

        } else {
            if (mode != null) {
                Toast.makeText(PowerOnActivity.this, "Layer dinámico no tiene mode, debe ser null", Toast.LENGTH_SHORT).show();
            } else {
                switch (nombreCapa) {
                    case "SED":
                        int array1[]; //declaracion arreglo de tipo numerico
                        array1 = new int[1];
                        array1[0] = 1;
                        LySED = new ArcGISDynamicMapServiceLayer(url, array1, credencial);
                        LySED.setVisible(visibilidad);
                        break;
                    case "SSEE":
                        int array2[]; //declaracion arreglo de tipo numerico
                        array2 = new int[1];
                        array2[0] = 0;
                        LySSEE = new ArcGISDynamicMapServiceLayer(url, array2, credencial);
                        LySSEE.setVisible(false);
                        break;
                    case "REDMT":
                        int array3[]; //declaracion arreglo de tipo numerico
                        array3 = new int[1];
                        array3[0] = 0;
                        LyREDMT = new ArcGISDynamicMapServiceLayer(url, array3, credencial);
                        LyREDMT.setVisible(visibilidad);
                        break;
                    case "REDBT":
                        int array4[]; //declaracion arreglo de tipo numerico
                        array4 = new int[1];
                        array4[0] = 1;
                        LyREDBT = new ArcGISDynamicMapServiceLayer(url, array4, credencial);
                        LyREDBT.setVisible(visibilidad);
                        break;
                    case "REDAP":
                        int array5[]; //declaracion arreglo de tipo numerico
                        array5 = new int[1];
                        array5[0] = 2;
                        LyREDAP = new ArcGISDynamicMapServiceLayer(url, array5, credencial);
                        LyREDAP.setVisible(visibilidad);
                        break;
                    case "POSTES":
                        int array6[]; //declaracion arreglo de tipo numerico
                        array6 = new int[1];
                        array6[0] = 2;
                        LyPOSTES = new ArcGISDynamicMapServiceLayer(url, null, credencial);
                        LyPOSTES.setVisible(visibilidad);
                        break;
                    case "EQUIPOS_LINEA":
                        int array7[]; //declaracion arreglo de tipo numerico
                        array7 = new int[1];
                        array7[0] = 0;
                        LyEQUIPOSLINEA = new ArcGISDynamicMapServiceLayer(url, array7, credencial);
                        LyEQUIPOSLINEA.setVisible(visibilidad);
                        break;
                    case "EQUIPOS_PTO":
                        int array8[]; //declaracion arreglo de tipo numerico
                        array8 = new int[1];
                        array8[0] = 3;
                        LyEQUIPOSPTO = new ArcGISDynamicMapServiceLayer(url, array8, credencial);
                        LyEQUIPOSPTO.setVisible(visibilidad);
                        break;
                    case "LUMINARIAS":
                        int array9[]; //declaracion arreglo de tipo numerico
                        array9 = new int[1];
                        array9[0] = 0;
                        LyLUMINARIAS = new ArcGISDynamicMapServiceLayer(url, array9, credencial);
                        LyLUMINARIAS.setVisible(visibilidad);
                        break;
                    case "CLIENTES":
                        int array10[]; //declaracion arreglo de tipo numerico
                        array10 = new int[1];
                        array10[0] = 0;
                        LyCLIENTES = new ArcGISDynamicMapServiceLayer(url, array10, credencial);
                        LyCLIENTES.setVisible(visibilidad);
                        break;
                    case "MEDIDORES":
                        int array11[]; //declaracion arreglo de tipo numerico
                        array11 = new int[1];
                        array11[0] = 0;
                        LyMEDIDORES = new ArcGISDynamicMapServiceLayer(url, array11, credencial);
                        LyMEDIDORES.setVisible(visibilidad);
                        break;
                    case "CONCESIONES":
                        int array12[]; //declaracion arreglo de tipo numerico
                        array12 = new int[2];
                        array12[0] = 0;
                        array12[1] = 1;
                        LyCONCESIONES = new ArcGISDynamicMapServiceLayer(url, array12, credencial);
                        LyCONCESIONES.setVisible(visibilidad);
                        LyCONCESIONES.setOpacity(0.4f);
                        break;
                    case "DIRECCIONES":
                        int array13[];
                        array13 = new int[1];
                        array13[0] = 0;
                        LyDIRECCIONES = new ArcGISDynamicMapServiceLayer(url, array13, credencial);
                        LyDIRECCIONES.setVisible(visibilidad);
                        break;
                    case "EMPALMES":
                        int array14[]; //declaracion arreglo de tipo numerico
                        array14 = new int[1];
                        array14[0] = 1;
                        LyEMPALMES = new ArcGISDynamicMapServiceLayer(url, array14, credencial);
                        LyEMPALMES.setVisible(visibilidad);
                        break;
                    case "MAPABASECHQ":
                        LyMapabase = new ArcGISDynamicMapServiceLayer(url, null, credencial);
                        LyMapabase.setVisible(visibilidad);
                        break;
                    case "SALIDAALIM":
                        int array15[]; //declaracion arreglo de tipo numerico
                        array15 = new int[1];
                        array15[0] = 2;
                        LySALIDAALIM = new ArcGISDynamicMapServiceLayer(url, array15, credencial);
                        LySALIDAALIM.setVisible(visibilidad);
                        break;
                    case "REDSTX":
                        int array16[];
                        array16 = new int[1];
                        array16[0] = 1;
                        LyREDSTX = new ArcGISDynamicMapServiceLayer(url, array16, credencial);
                        LyREDSTX.setVisible(visibilidad);
                        break;
                    case "TORRESSTX":
                        int array17[];
                        array17 = new int[1];
                        array17[0] = 0;
                        LyTORRESSTX = new ArcGISDynamicMapServiceLayer(url, array17, credencial);
                        LyTORRESSTX.setVisible(visibilidad);
                        break;
                    case "ENCUESTADO":
                        int array18[];
                        array18 = new int[1];
                        array18[0] = 0;
                        LyENCUESTA = new ArcGISDynamicMapServiceLayer(url, array18, credencial);
                        LyENCUESTA.setVisible(visibilidad);
                        break;
                    case "REEMPLAZO":
                        int array19[];
                        array19 = new int[1];
                        array19[0] = 1;
                        LyREEMPLAZO = new ArcGISDynamicMapServiceLayer(url, array19, credencial);
                        LyREEMPLAZO.setVisible(visibilidad);
                        break;
                    case "POSED":
                        int array20[];
                        array20 = new int[1];
                        array20[0] = 0;
                        LyPOSED = new ArcGISDynamicMapServiceLayer(url, array20, credencial);
                        LyPOSED.setVisible(visibilidad);
                        break;
                    case "POTRAMO":
                        int array21[];
                        array21 = new int[1];
                        array21[0] = 2;
                        LyPOTRAMO = new ArcGISDynamicMapServiceLayer(url, array21, credencial);
                        LyPOTRAMO.setVisible(visibilidad);
                        break;
                    case "POCLIENTES":
                        int array22[];
                        array22 = new int[1];
                        array22[0] = 1;
                        LyPOCLIENTES = new ArcGISDynamicMapServiceLayer(url, array22, credencial);
                        LyPOCLIENTES.setVisible(visibilidad);
                        break;
                    default:
                        Toast.makeText(PowerOnActivity.this, "Problemas agregando layers dinámicos.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    private void singleTapOnMap() {
        myMapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float x, float y) {

                if (bMapTap) {

                    Point oPoint = myMapView.toMapPoint(x, y);

                    if (mBusquedaLayer != null && myMapView.getLayerByID(mBusquedaLayer.getID()) != null)
                        myMapView.removeLayer(mBusquedaLayer);

                    if (mSeleccionLayer != null && myMapView.getLayerByID(mSeleccionLayer.getID()) != null)
                        myMapView.removeLayer(mSeleccionLayer);

                    if (bCallOut) {

                        if (nIndentify > 0) {

                            mSeleccionLayer = new GraphicsLayer();
                            SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.GREEN, 12, SimpleMarkerSymbol.STYLE.CIRCLE);
                            Graphic resultLocGraphic = new Graphic(oPoint, resultSymbol);
                            mSeleccionLayer.addGraphic(resultLocGraphic);

                            myMapView.addLayer(mSeleccionLayer);

                            switch (nIndentify) {
                                case 1:
                                    getCalleToDialog(oPoint);
                                    break;
                                case 2:
                                    getTramoToDialog(oPoint);
                                    break;
                            }

                        } else {
                            mSeleccionLayer = new GraphicsLayer();
                            int[] selectedFeatures = oLySelectAsoc.getGraphicIDs(x, y, calcRadio(), 1000);

                            // select the features
                            oLySelectAsoc.clearSelection();
                            oLySelectAsoc.setSelectedGraphics(selectedFeatures, true);
                            Log.w("PowerOnActivity", "Selected Graphics " + selectedFeatures.length);

                            if (selectedFeatures.length > 0) {
                                Graphic[] results = oLySelectAsoc.getSelectedFeatures();
                                Callout mapCallout = myMapView.getCallout();
                                mapCallout.hide();

                                for (Graphic graphic : results) {

                                    Map<String, Object> attr = graphic.getAttributes();
                                    Util oUtil = new Util();
                                    CalloutTvClass oCall = oUtil.getCalloutValues(attr);

                                    GisTextView tv = new GisTextView(PowerOnActivity.this);
                                    tv.setText(oCall.getVista());
                                    tv.setHint(oCall.getValor());
                                    tv.setIdObjeto(oCall.getIdObjeto());
                                    tv.setPoint((Point) graphic.getGeometry());
                                    tv.setTipo("nueva");
                                    tv.setTextColor(Color.WHITE);

                                    tv.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            oTxtAsoc.setIdObjeto(((GisTextView) v).getIdObjeto());
                                            oTxtAsoc.setText(((GisTextView) v).getHint());
                                            oTxtAsoc.setTipo(((GisTextView) v).getTipo());
                                            oTxtAsoc.setPoint(myMapView.getCallout().getCoordinates());
                                            bCallOut = false;
                                            bMapTap = false;
                                            myMapView.getCallout().hide();
                                            oLySelectAsoc.clearSelection();
                                            dialogCur.show();
                                            if (mSeleccionLayer != null && myMapView.getLayerByID(mSeleccionLayer.getID()) != null)
                                                myMapView.removeLayer(mSeleccionLayer);

                                            // LINE PRINT
                                            if (mUbicacionLayer != null && oUbicacion != null) {
                                                SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.BLUE, 4, SimpleLineSymbol.STYLE.DASH);
                                                Polyline oLine = new Polyline();
                                                oLine.startPath(oUbicacion);
                                                oLine.lineTo(oTxtAsoc.getPoint());
                                                Graphic graphicDireccion = new Graphic(oLine, lineSymbol, null);
                                                mUbicacionLayer.addGraphic(graphicDireccion);
                                            }
                                        }
                                    });

                                    Point point = (Point) graphic.getGeometry();

                                    mapCallout.setOffset(0, -3);
                                    mapCallout.setCoordinates(point);
                                    mapCallout.setMaxHeight(100);
                                    mapCallout.setMaxWidth(400);
                                    mapCallout.setStyle(R.xml.mycalloutprefs);
                                    mapCallout.setContent(tv);

                                    mapCallout.show();
                                }
                            } else {
                                getAsocObject(oPoint);
                            }

                            SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.GREEN, 12, SimpleMarkerSymbol.STYLE.CIRCLE);
                            Graphic resultLocGraphic = new Graphic(oPoint, resultSymbol);
                            mSeleccionLayer.addGraphic(resultLocGraphic);

                            myMapView.addLayer(mSeleccionLayer);
                        }

                    } else {
                        if (mUbicacionLayer != null && myMapView.getLayerByID(mUbicacionLayer.getID()) != null)
                            myMapView.removeLayer(mUbicacionLayer);

                        oUbicacion = oPoint;
                        mUbicacionLayer = new GraphicsLayer();

                        SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.RED, 12, SimpleMarkerSymbol.STYLE.DIAMOND);
                        Graphic resultLocGraphic = new Graphic(oPoint, resultSymbol);
                        mUbicacionLayer.addGraphic(resultLocGraphic);

                        myMapView.addLayer(mUbicacionLayer);

                        if (R.layout.dialog_poste == idResLayoutSelect) {
                            LyPOSTES.setVisible(true);
                            if (LyPOSTES.getMinScale() < myMapView.getScale())
                                myMapView.zoomToScale(oPoint, LyPOSTES.getMinScale() * 0.9);
                        } else if (R.layout.dialog_direccion == idResLayoutSelect) {
                            LyDIRECCIONES.setVisible(true);
                            if (LyDIRECCIONES.getMinScale() < myMapView.getScale())
                                myMapView.zoomToScale(oPoint, LyDIRECCIONES.getMinScale() * 0.9);
                        } else if (R.layout.dialog_cliente == idResLayoutSelect || R.layout.dialog_cliente_cnr == idResLayoutSelect) {
                            LyPOSTES.setVisible(true);
                            LyDIRECCIONES.setVisible(true);
                            LyCLIENTES.setVisible(true);

                            if (idResLayoutSelect == R.layout.dialog_cliente_cnr)
                                LyREDBT.setVisible(true);

                            if (LyPOSTES.getMinScale() < myMapView.getScale())
                                myMapView.zoomToScale(oPoint, LyPOSTES.getMinScale() * 0.9);
                        } else if (R.layout.form_lectores == idResLayoutSelect){
                            if (LyPOSTES.getMinScale() < myMapView.getScale())
                                myMapView.zoomToScale(oPoint, LyPOSTES.getMinScale() * 0.9);
                        }
                    }
                } else {
                    if (bVerData) {
                        double nExtendScale = myMapView.getScale();
                        double layerScala = 0;

                        ArrayList<ArcGISDynamicMapServiceLayer> arrayLay = new ArrayList<>();
                        Point oPoint = myMapView.toMapPoint(x, y);

                        if (oLyViewGraphs != null && myMapView.getLayerByID(oLyViewGraphs.getID()) != null)
                            myMapView.removeLayer(oLyViewGraphs);

                        oLyViewGraphs = new GraphicsLayer();
                        Graphic oGraph = new Graphic(oPoint, new SimpleMarkerSymbol(R.color.green, 12, SimpleMarkerSymbol.STYLE.CIRCLE));
                        oLyViewGraphs.addGraphic(oGraph);

                        myMapView.addLayer(oLyViewGraphs);

                        for (Layer oLayer : myMapView.getLayers()) {

                            if ((oLayer.getName() != null && !oLayer.getName().equalsIgnoreCase("MapaBase"))
                                    && oLayer.isVisible()) {

                                if (oLayer.getClass().equals(ArcGISDynamicMapServiceLayer.class)) {
                                    for (ArcGISLayerInfo arcGISLayerInfo : ((ArcGISDynamicMapServiceLayer) oLayer).getLayers()) {
                                        if (arcGISLayerInfo.isVisible()) {
                                            layerScala = (arcGISLayerInfo.getMinScale() > 0) ? arcGISLayerInfo.getMinScale() : 0;
                                            break;
                                        }
                                    }

                                    layerScala = (layerScala > 0) ? layerScala : nExtendScale;

                                    Log.w("layerScala " + layerScala, "nExtendScale " + nExtendScale);

                                    if (nExtendScale <= layerScala) {
                                        arrayLay.add((ArcGISDynamicMapServiceLayer) oLayer);
                                    }
                                }
                            }
                        }

                        if (arrayLay.size() > 0) {
                            ArcGISDynamicMapServiceLayer[] aLay = new ArcGISDynamicMapServiceLayer[arrayLay.size()];
                            aLay = arrayLay.toArray(aLay);
                            getInfoObject(oPoint, aLay);
                        } else {
                            Toast.makeText(getApplicationContext(), "No hay capas visibles", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private void changesOnMap() {
        //Cambios en el mapa
        myMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
            @Override
            public void onStatusChanged(Object o, STATUS status) {

                if (STATUS.LAYER_LOADED == status)
                    myMapView.setExtent(mCurrentMapExtent);

                if (STATUS.INITIALIZED.equals(status)) {
                    Point oPointEmp = getPointByEmp();
                    myMapView.centerAndZoom(oPointEmp.getX(), oPointEmp.getY(), 0.003f);
                    myMapView.zoomin(true);
                }

                if (status == STATUS.LAYER_LOADING_FAILED) {
                    // Check if a layer is failed to be loaded due to security
                    if ((status.getError()) instanceof EsriSecurityException) {
                        EsriSecurityException securityEx = (EsriSecurityException) status
                                .getError();
                        if (securityEx.getCode() == EsriSecurityException.AUTHENTICATION_FAILED)
                            Toast.makeText(myMapView.getContext(),
                                    "Su cuenta tiene permisos limitados, contacte con el administrador para solicitar permisos faltantes",
                                    Toast.LENGTH_SHORT).show();
                        else if (securityEx.getCode() == EsriSecurityException.TOKEN_INVALID)
                            Toast.makeText(myMapView.getContext(),
                                    "Invalid Token! Resubmit!",
                                    Toast.LENGTH_SHORT).show();
                        else if (securityEx.getCode() == EsriSecurityException.TOKEN_SERVICE_NOT_FOUND)
                            Toast.makeText(myMapView.getContext(),
                                    "Token Service Not Found! Resubmit!",
                                    Toast.LENGTH_SHORT).show();
                        else if (securityEx.getCode() == EsriSecurityException.UNTRUSTED_SERVER_CERTIFICATE)
                            Toast.makeText(myMapView.getContext(),
                                    "Untrusted Host! Resubmit!",
                                    Toast.LENGTH_SHORT).show();

                        if (o instanceof ArcGISFeatureLayer) {
                            // Set user credential through username and password
                            UserCredentials creds = new UserCredentials();
                            creds.setUserAccount(usuar, passw);

                            LyMapabase.reinitializeLayer(creds);
                            LySED.reinitializeLayer(creds);
                            LySSEE.reinitializeLayer(creds);
                            LySALIDAALIM.reinitializeLayer(creds);
                            LyREDMT.reinitializeLayer(creds);
                            LyREDBT.reinitializeLayer(creds);
                            LyREDAP.reinitializeLayer(creds);
                            LyPOSTES.reinitializeLayer(creds);
                            LyEQUIPOSLINEA.reinitializeLayer(creds);
                            LyEQUIPOSPTO.reinitializeLayer(creds);
                            LyLUMINARIAS.reinitializeLayer(creds);
                            LyCLIENTES.reinitializeLayer(creds);
                            LyMEDIDORES.reinitializeLayer(creds);
                            LyCONCESIONES.reinitializeLayer(creds);
                            LyDIRECCIONES.reinitializeLayer(creds);
                            LyEMPALMES.reinitializeLayer(creds);
                            LyENCUESTA.reinitializeLayer(creds);
                            LyREEMPLAZO.reinitializeLayer(creds);

                            LyPOCLIENTES.reinitializeLayer(creds);
                            LyPOSED.reinitializeLayer(creds);
                            LyPOTRAMO.reinitializeLayer(creds);
                        }
                    }
                }
            }
        });
    }

    private Point getPointByEmp() {
        Point point = new Point(-33.035580, -71.626953);

        switch (empresa) {
            case "litoral":
                point = new Point(-33.398667, -71.698279);
                break;
            case "casablanca":
                point = new Point(-33.319037, -71.407631);
                break;
            case "linares":
                point = new Point(-35.846450, -71.599672);
                break;
            case "parral":
                point = new Point(-36.139971, -71.824429);
                break;
        }

        return point;
    }

    private class AsyncQueryTask extends AsyncTask<String, Void, FeatureResult> {

        FeatureResult oResultTramos;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(PowerOnActivity.this);
            progress = ProgressDialog.show(PowerOnActivity.this, "",
                    "Espere por favor... Obteniendo resultados.");
        }

        @Override
        protected FeatureResult doInBackground(String... params) {

            try {
                String whereClause = params[0];
                QueryParameters myParameters = new QueryParameters();
                myParameters.setWhere(whereClause);

                myParameters.setReturnGeometry(true);
                String[] outfields = new String[]{"*"};
                myParameters.setOutFields(outfields);

                String url = params[1];
                FeatureResult results;

                QueryTask queryTask = new QueryTask(url, credenciales);
                results = queryTask.execute(myParameters);

                if (SpiBusqueda == 1) {
                    String cod = whereClause.replace("codigo", "sed");

                    QueryParameters oParam = new QueryParameters();
                    oParam.setWhere(cod);

                    oParam.setReturnGeometry(true);
                    oParam.setOutFields(new String[]{"sed"});

                    String urlTramos = LyREDBT.getUrl().concat("/1");

                    QueryTask oQueryTramos = new QueryTask(urlTramos, credenciales);
                    oResultTramos = oQueryTramos.execute(oParam);
                }

                return results;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(FeatureResult results) {
            if (results != null && results.featureCount() > 0) {
                int numResult = (int) results.featureCount();

                if (mBusquedaLayer != null && myMapView.getLayerByID(mBusquedaLayer.getID()) != null) {
                    myMapView.removeLayer(mBusquedaLayer);
                }

                mBusquedaLayer = new GraphicsLayer();
                myMapView.addLayer(mBusquedaLayer);

                myMapView.setScale(iBusqScale);

                for (Object element : results) {
                    progress.incrementProgressBy(numResult / 100);

                    if (element instanceof Feature) {

                        Feature feature = (Feature) element;
                        myMapView.setExtent(feature.getGeometry(), 0, true);

                        if (feature.getSymbol() == null) {
                            SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.RED, 16, SimpleMarkerSymbol.STYLE.CROSS);
                            Graphic resultLocGraphic = new Graphic(feature.getGeometry(), resultSymbol);
                            mBusquedaLayer.addGraphic(resultLocGraphic);

                            try {

                                for (Object tramo : oResultTramos) {
                                    Feature oTramo = (Feature) tramo;

                                    SimpleLineSymbol oLine = new SimpleLineSymbol(Color.RED, 1f, SimpleLineSymbol.STYLE.SOLID);
                                    Graphic resGraph = new Graphic(oTramo.getGeometry(), oLine);

                                    mBusquedaLayer.addGraphic(resGraph);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            Graphic resultLocGraphic = new Graphic(feature.getGeometry(), feature.getSymbol());
                            mBusquedaLayer.addGraphic(resultLocGraphic);
                        }

                        Callout mapCallout = myMapView.getCallout();
                        fabNavRoute.setVisibility(View.GONE);
                        mapCallout.hide();

                        StringBuilder outStr;
                        Util oUtil = new Util();
                        outStr = oUtil.getStringByAttrClass(feature.getAttributes());
                        GisTextView tv = new GisTextView(PowerOnActivity.this);
                        tv.setText(outStr.toString());
                        tv.setPoint((Point) feature.getGeometry());
                        tv.setTextColor(Color.WHITE);


                        mapCallout.setOffset(0, -3);
                        mapCallout.setCoordinates(tv.getPoint());
                        mapCallout.setMaxHeight(100);
                        mapCallout.setMaxWidth(400);
                        mapCallout.setStyle(R.xml.mycalloutprefs);
                        mapCallout.setContent(tv);

                        mapCallout.show();
                        fabNavRoute.setVisibility(View.VISIBLE);
                    }
                }

                myMapView.zoomin(true);
            } else {
                Toast.makeText(PowerOnActivity.this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
            }
            progress.dismiss();
        }
    }

    private class IdentifyResults extends AsyncTask<IdentifyParameters, Void, IdentifyResult[]> {

        Point oPoint;
        String[] aUrls;
        String sAttr;
        ProgressDialog progress;
        private ArcGISDynamicMapServiceLayer[] oCapas;
        private IdentifyResult[] oResult;
        UserCredentials oCredentials;
        boolean bMulticapa = false;
        boolean bAsoc = false;

        public IdentifyResults(String mAttr, Point mLocation, String mUrl, UserCredentials mCred) {
            oPoint = mLocation;
            aUrls = new String[]{mUrl};
            oCredentials = mCred;
            sAttr = mAttr;
        }

        public IdentifyResults(Point mLocation, String[] mUrls, UserCredentials mCred) {
            oPoint = mLocation;
            oCredentials = mCred;
            aUrls = mUrls;
            bMulticapa = true;
        }

        public IdentifyResults(Point mLocation, ArcGISDynamicMapServiceLayer[] mCapas, UserCredentials mCred) {
            oPoint = mLocation;
            oCredentials = mCred;
            //aUrls = mUrls;
            bMulticapa = true;
            oCapas = mCapas;
        }

        public IdentifyResults(Point mLocation, String[] mUrls, UserCredentials mCred, boolean mAsoc) {
            oPoint = mLocation;
            oCredentials = mCred;
            aUrls = mUrls;
            bMulticapa = true;
            bAsoc = mAsoc;
        }

        private int[] getLayersVisibles(ArcGISDynamicMapServiceLayer lay) {
            ArrayList<Integer> iCapas = new ArrayList<>();

            for (ArcGISLayerInfo gisLayerInfo : lay.getLayers()) {
                if (gisLayerInfo.isVisible())
                    iCapas.add(gisLayerInfo.getId());
            }

            int[] result = new int[iCapas.size()];

            for (int i = 0; i < iCapas.size(); i++) {
                result[i] = iCapas.get(i);
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(PowerOnActivity.this, "Identificando sector", "Espere un momento ...");
            progress.setCancelable(true);
            progress.setCanceledOnTouchOutside(true);
        }

        @Override
        protected IdentifyResult[] doInBackground(IdentifyParameters... params) {

            if (params != null && params.length > 0) {
                IdentifyParameters mParams = params[0];

                if (oCapas != null) {
                    for (ArcGISDynamicMapServiceLayer lay : oCapas) {

                        int[] vis = getLayersVisibles(lay);
                        mParams.setLayers(vis);

                        try {
                            IdentifyTask oTask = new IdentifyTask(lay.getUrl(), oCredentials);
                            oResult = oTask.execute(mParams);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Log.w("MultiIdentifyResults", String.format("Results: %s Url: %s", oResult.length, lay.getUrl()));
                        if (oResult.length > 0) break;
                    }

                } else {
                    for (String url : aUrls) {

                        try {
                            IdentifyTask oTask = new IdentifyTask(url, oCredentials);
                            oResult = oTask.execute(mParams);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Log.w("MultiIdentifyResults", String.format("Results: %s Url: %s", oResult.length, url));
                        if (oResult.length > 0) break;
                    }
                }
            }
            return getResultSet();
        }

        @Override
        protected void onPostExecute(IdentifyResult[] identifyResults) {
            //super.onPostExecute(identifyResults);
            String txt = "";
            if (identifyResults != null && identifyResults.length > 0) {
                if (bMulticapa) {
                    Util oUtil = new Util();

                    Callout mapCallout = myMapView.getCallout();
                    fabNavRoute.setVisibility(View.GONE);
                    mapCallout.hide();

                    for (IdentifyResult identifyResult : identifyResults) {

                        if (!bAsoc) {
                            StringBuilder outStr;
                            Log.w("identifyResult layer " + identifyResult.getLayerName() + " " + identifyResult.getLayerId(), " size Attr: " + identifyResult.getAttributes().size());
                            outStr = oUtil.getStringByClassAttr(identifyResult);

                            GisTextView tv = new GisTextView(PowerOnActivity.this);
                            tv.setText(outStr.toString());
                            tv.setTextColor(Color.WHITE);

                            if (identifyResult.getGeometry().getClass() != Point.class) {
                                if (identifyResult.getGeometry().getClass() == Polyline.class) {
                                    Polyline oPolyline = (Polyline) identifyResult.getGeometry();
                                    tv.setPoint(oUtil.calculateCenterPolyline(oPolyline));
                                } else tv.setPoint(oPoint);
                            } else tv.setPoint((Point) identifyResult.getGeometry());

                            mapCallout.setOffset(0, -3);
                            mapCallout.setCoordinates(tv.getPoint());
                            mapCallout.setMaxHeight(400);
                            mapCallout.setMaxWidth(400);
                            mapCallout.setStyle(R.xml.mycalloutprefs);
                            mapCallout.setContent(tv);

                            mapCallout.show();
                            fabNavRoute.setVisibility(View.VISIBLE);
                        } else {
                            Map<String, Object> attr = identifyResult.getAttributes();
                            CalloutTvClass oCall = oUtil.getCalloutValues(attr);

                            GisTextView tv = new GisTextView(PowerOnActivity.this);
                            tv.setText(oCall.getVista());
                            tv.setHint(oCall.getValor());
                            tv.setIdObjeto(oCall.getIdObjeto());
                            tv.setPoint((Point) identifyResult.getGeometry());
                            tv.setTipo("existente");
                            tv.setTextColor(Color.WHITE);

                            tv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    oTxtAsoc.setIdObjeto(((GisTextView) v).getIdObjeto());
                                    oTxtAsoc.setText(((GisTextView) v).getHint());
                                    oTxtAsoc.setTipo(((GisTextView) v).getTipo());
                                    oTxtAsoc.setPoint(myMapView.getCallout().getCoordinates());
                                    bCallOut = false;
                                    bMapTap = false;
                                    myMapView.getCallout().hide();
                                    oLySelectAsoc.clearSelection();
                                    dialogCur.show();
                                    if (mSeleccionLayer != null && myMapView.getLayerByID(mSeleccionLayer.getID()) != null)
                                        myMapView.removeLayer(mSeleccionLayer);

                                    // LINE PRINT
                                    if (mUbicacionLayer != null && oUbicacion != null) {
                                        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.BLUE, 4, SimpleLineSymbol.STYLE.DASH);
                                        Polyline oLine = new Polyline();
                                        oLine.startPath(oUbicacion);
                                        oLine.lineTo(oTxtAsoc.getPoint());
                                        Graphic graphicDireccion = new Graphic(oLine, lineSymbol, null);
                                        mUbicacionLayer.addGraphic(graphicDireccion);
                                    }
                                }
                            });

                            Point point = (Point) identifyResult.getGeometry();

                            mapCallout.setOffset(0, -3);
                            mapCallout.setCoordinates(point);
                            mapCallout.setMaxHeight(100);
                            mapCallout.setMaxWidth(400);
                            mapCallout.setStyle(R.xml.mycalloutprefs);
                            mapCallout.setContent(tv);

                            mapCallout.show();
                        }

                        break;
                    }
                } else {
                    for (IdentifyResult identifyResult : identifyResults) {
                        Log.w("IdentifyResults", identifyResult.getValue().toString());
                        if (identifyResult.getAttributes().get(sAttr) != null) {
                            Log.w("IdentifyResults", identifyResult.getAttributes().get(sAttr).toString());
                            txt = identifyResult.getAttributes().get(sAttr).toString();
                        }
                    }

                    oTxtAsoc.setText(txt);

                    bCallOut = false;
                    bMapTap = false;
                    myMapView.getCallout().hide();
                    oLySelectAsoc.clearSelection();
                    dialogCrear.show();

                    if (mSeleccionLayer != null && myMapView.getLayerByID(mSeleccionLayer.getID()) != null)
                        myMapView.removeLayer(mSeleccionLayer);

                    nIndentify = 0;
                }
            } else {
                Toast.makeText(PowerOnActivity.this, "No hay datos", Toast.LENGTH_SHORT).show();
            }

            progress.dismiss();
        }

        public IdentifyResult[] getResultSet() {
            return oResult;
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

    private class MyLocationListener implements LocationListener {

        public MyLocationListener() {
            super();
        }

        public void onLocationChanged(Location loc) {

            if (loc == null)
                return;

            boolean zoomToMe = (mLocation == null);
            mLocation = new Point(loc.getLongitude(), loc.getLatitude());
            Point p = (Point) GeometryEngine.project(mLocation, egs, wm);
            if (zoomToMe) {
                myMapView.zoomToResolution(p, 5.0);
            } else {
                if (loc.hasSpeed()){
                    int speed = (int) ((loc.getSpeed() * 3600) / 1000);
                    if (speed > 10 && speed < 150) {
                        myMapView.centerAt(p, true);
                        if (speed > 120) {
                            Toast.makeText(getApplicationContext(), String.format("Velocidad max superada: %s Km/h", speed),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }

        public void onProviderDisabled(String provider) {
            if (!bAlertGps) {
                Toast.makeText(getApplicationContext(), "GPS Deshabilitado", Toast.LENGTH_SHORT).show();
                alertNoGps();
                bAlertGps = true;
            }

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

                    initGeoposition();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Log.w("PowerOnActivity", "No hay permisos de ACCESS_FINE_LOCATION");
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
