package cl.gisred.android;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.coatedmoose.customviews.SignatureView;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import cl.gisred.android.classes.GisEditText;
import cl.gisred.android.classes.GisTextView;
import cl.gisred.android.entity.BusqClass;
import cl.gisred.android.entity.CalloutTvClass;
import cl.gisred.android.util.HtmlUtils;
import cl.gisred.android.util.PhotoUtils;
import cl.gisred.android.util.Util;

public class InspActivity extends AppCompatActivity {

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
    public String[] searchArray = {"Clientes", "SED", "Poste", "Medidor", "Dirección"};

    //ArrayList Layer
    public String[] listadoCapas = {"SED", "SSEE", "Salida Alimentador", "Red MT", "Red BT", "Red AP", "Postes", "Equipos Linea", "Equipos Puntos", "Luminarias", "Clientes", "Medidores",
            "Concesiones", "Direcciones", "Empalmes", "Red sTX", "Torres sTX", "ECSE Encuestados", "ECSE Reemplazos", "Electro Dependientes"};

    public String[] arrayTipoEdif = {};
    public String[] arrayTipoPoste = {};
    public String[] arrayTension = {};
    public String[] arrayMedidor = {};
    public String[] arrayEmpalme = {};
    public String[] arrayTecMedidor = {};
    public String[] arrayTipoCnr = {};
    public String[] arrayTipoFase = {};
    public String[] arrayMarca = {};
    public String[] arrayTipoMarca = {};
    public String[] arrayFirmante = {};
    public String[] arrayTipoFaseInsp = {};

    public String[] arrayMarcaTM = {};
    public String[] arrayTipoMarcaTM = {};

    public boolean fool[] = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};

    //url para dinamyc layers
    String din_urlMapaBase, din_urlEquiposPunto, din_urlEquiposLinea, din_urlTramos, din_urlNodos, din_urlLuminarias, din_urlClientes, din_urlConcesiones, din_urlMedidores, din_urlDirecciones, din_urlStx, din_urlInterrupciones, din_urlECSE, din_urlElectroDep;
    //url para feature layers
    String srv_urlPostes, srv_urlDireccion, srv_urlClientes, srv_urlClientesCnr, srv_urlUnion012, srv_calles;

    //Set bing Maps
    String BingKey = "Asrn2IMtRwnOdIRPf-7q30XVUrZuOK7K2tzhCACMg7QZbJ4EPsOcLk6mE9-sNvUe";
    final BingMapsLayer mAerialBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.AERIAL);
    final BingMapsLayer mAerialWLabelBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.AERIAL_WITH_LABELS);
    final BingMapsLayer mRoadBaseMaps = new BingMapsLayer(BingKey, BingMapsLayer.MapStyle.ROAD);

    ArcGISDynamicMapServiceLayer LySED, LySSEE, LySALIDAALIM, LyREDMT, LyREDBT, LyREDAP, LyPOSTES, LyEQUIPOSLINEA, LyEQUIPOSPTO, LyLUMINARIAS, LyCLIENTES, LyMEDIDORES, LyCONCESIONES, LyDIRECCIONES, LyEMPALMES, LyMapabase, LyREDSTX, LyTORRESSTX, LyENCUESTA, LyREEMPLAZO, LyELECTRODEP;
    ArcGISFeatureLayer LyAddPoste, LyAddDireccion, LyAddCliente, LyAddClienteCnr, LyAddUnion, LyAsocTramo, LyAsocCalle;

    //set Extent inicial
    Polygon mCurrentMapExtent = null;
    // Spatial references used for projecting points
    final SpatialReference wm = SpatialReference.create(102100);
    final SpatialReference egs = SpatialReference.create(4326);

    //Constantes
    private static final String modIngreso = "INGRESO_CLIENTES";
    private static final String modInspeccion = "PROTOCOLO_INSPECCION";

    //Sets
    ArrayList<String> arrayWidgets;
    ArrayList<String> arrayModulos;
    private int choices;
    ProgressDialog progress;

    BusqClass[] datosBusq;
    ListView lstBusqMedidores;

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

    private Graphic[] addsUnion = {};

    Dialog dialogCrear;
    Dialog dialogBusq;
    Dialog formCrear;
    Dialog dialogCur;
    boolean bInspRotulo = true;
    ArrayList<View> arrayTouchs = null;
    ImageButton btnUbicacion = null;
    FloatingActionsMenu menuMultipleActions;
    FloatingActionsMenu menuInspeccionActions;
    FloatingActionButton fabShowDialog;
    FloatingActionButton fabShowForm;
    FloatingActionButton fabVerCapas;
    FloatingActionButton fabNavRoute;
    //var inspeccion
    public ImageView imgFirmaTec;
    public ImageView imgFirmaInsp;
    public ImageView imgTemp;
    public ImageView imgPhoto1;
    public ImageView imgPhoto2;
    public ImageView imgPhoto3;
    private Uri mImageUri;
    private String sImgUri;
    private static final int ACTIVITY_SELECT_IMAGE = 1020, ACTIVITY_SELECT_FROM_CAMERA = 1040, ACTIVITY_SHARE = 1030;
    private SimpleDateFormat dateFormatter;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private EditText txtHora;

    private static final String CLIENT_ID = "ZWIfL6Tqb4kRdgZ4";
    private boolean bFallo = false;

    HashMap<Integer, String> layerDefs;

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

        setContentView(R.layout.activity_insp);

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
        setLayersURL(this.getResources().getString(R.string.url_ECSE_varios), "ECSE");
        setLayersURL(this.getResources().getString(R.string.url_Electrodependientes), "ELECTRODEP");

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
        addLayersToMap(credenciales, "DYNAMIC", "ELECTRODEP", din_urlElectroDep, null, false);

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
        myMapView.addLayer(LyELECTRODEP, 20);


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

        menuInspeccionActions = (FloatingActionsMenu) findViewById(R.id.inspection_actions);
        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

        fabShowDialog = (FloatingActionButton) findViewById(R.id.action_show_dialog);
        if (fabShowDialog != null) fabShowDialog.setVisibility(View.GONE);

        fabShowForm = (FloatingActionButton) findViewById(R.id.action_show_form);
        if (fabShowForm != null) fabShowForm.setVisibility(View.GONE);

        fabVerCapas = (FloatingActionButton) findViewById(R.id.action_ver_capa);
        if (fabVerCapas != null) fabVerCapas.setVisibility(View.GONE);

        fabNavRoute = (FloatingActionButton) findViewById(R.id.action_nav_route);
        if (fabNavRoute != null) {
            fabNavRoute.setVisibility(View.GONE);
            fabNavRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myMapView != null && myMapView.getCallout().isShowing()) {
                        Point p = (Point) GeometryEngine.project(myMapView.getCallout().getCoordinates(), wm, egs);
                        Util.QueryNavigation(InspActivity.this, p);
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
        if (modulo.replace(" ", "_").equals(modInspeccion)) {
            dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

            arrayTipoFaseInsp = getResources().getStringArray(R.array.fase_conexion_insp);
            arrayMarca = getResources().getStringArray(R.array.marca);
            arrayTipoMarca = getResources().getStringArray(R.array.tipo_marca);
            arrayFirmante = getResources().getStringArray(R.array.situacion_firmante);

            arrayWidgets = bundle.getStringArrayList("widgets");
            arrayModulos = bundle.getStringArrayList("modulos");
            final String sForm = bundle.getString("form");

            if (sForm.contains("TELEMEDIDA")) {
                arrayMarcaTM = getResources().getStringArray(R.array.marca_tm);
                arrayTipoMarcaTM = getResources().getStringArray(R.array.tipo_marca_tm);
            }

            formCrear = new Dialog(InspActivity.this);
            fabShowForm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //bMapTap = false;
                    //bCallOut = false;
                    //myMapView.getCallout().hide();
                    formCrear.show();
                }
            });

            FloatingActionButton oFabForm = (FloatingActionButton) findViewById(R.id.action_form);
            oFabForm.setIconDrawable(drawOk);
            oFabForm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirFormInsp(v, sForm);
                }
            });

            FloatingActionButton oFabView = (FloatingActionButton) findViewById(R.id.action_view);
            oFabView.setIconDrawable(drawOk);
            oFabView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirEsquema(v);
                }
            });

            if (arrayModulos != null && arrayModulos.size() > 0 && arrayModulos.contains(empresa + "@" + modIngreso)) {

                arrayTipoPoste = getResources().getStringArray(R.array.tipo_poste);
                arrayTension = getResources().getStringArray(R.array.tipo_tension);
                arrayTipoEdif = getResources().getStringArray(R.array.tipo_edificacion);
                arrayMedidor = getResources().getStringArray(R.array.tipo_medidor);
                arrayEmpalme = getResources().getStringArray(R.array.tipo_empalme);
                arrayTecMedidor = getResources().getStringArray(R.array.tec_medidor);
                arrayTipoCnr = getResources().getStringArray(R.array.tipo_cnr);
                arrayTipoFase = getResources().getStringArray(R.array.fase_conexion);

                dialogCrear = new Dialog(InspActivity.this);

                fabShowDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bMapTap = false;
                        bCallOut = false;
                        myMapView.getCallout().hide();
                        //TODO Restringir datos dialog
                        if (oUbicacion != null) {
                            btnUbicacion.setColorFilter(Color.BLACK);
                            setEnabledDialog(true);
                        }
                        dialogCrear.show();
                        if (mSeleccionLayer != null && myMapView.getLayerByID(mSeleccionLayer.getID()) != null)
                            myMapView.removeLayer(mSeleccionLayer);
                    }
                });

                fabVerCapas.setVisibility(View.VISIBLE);
                fabVerCapas.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toogleCapas(v);
                    }
                });

                fabVerCapas.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(getApplicationContext(), "Ver Capas de Ingreso", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.action_a);
                final FloatingActionButton actionB = (FloatingActionButton) findViewById(R.id.action_b);
                final FloatingActionButton actionC = (FloatingActionButton) findViewById(R.id.action_c);
                final FloatingActionButton actionD = (FloatingActionButton) findViewById(R.id.action_d);

                setOpcion(actionA, null);
                setOpcion(actionB, null);
                setOpcion(actionC, modIngreso + "_TECNO");
                setOpcion(actionD, modIngreso + "_CNR");

                setLayersURL(this.getResources().getString(R.string.srv_Postes), "SRV_POSTES");
                setLayersURL(this.getResources().getString(R.string.srv_Direcciones), "SRV_DIRECCIONES");
                setLayersURL(this.getResources().getString(R.string.srv_Clientes), "SRV_CLIENTES");
                setLayersURL(this.getResources().getString(R.string.srv_Union_012), "SRV_UNIONES");
                setLayersURL(din_urlTramos, "TRAMOS");
                setLayersURL(this.getResources().getString(R.string.url_Mapabase), "SRV_CALLES");
                setLayersURL(this.getResources().getString(R.string.srv_ClientesCnr), "SRV_CLIENTESCNR");

                addLayersToMap(credenciales, "FEATURE", "ADDPOSTE", srv_urlPostes, null, true);
                addLayersToMap(credenciales, "FEATURE", "ADDADDRESS", srv_urlDireccion, null, true);
                addLayersToMap(credenciales, "FEATURE", "ADDCLIENTE", srv_urlClientes, null, true);
                addLayersToMap(credenciales, "FEATURE", "ADDUNION", srv_urlUnion012, null, true);
                addLayersToMap(credenciales, "FEATURE", "ASOCTRAMO", LyREDBT.getUrl(), null, false);
                addLayersToMap(credenciales, "FEATURE", "ASOCCALLE", srv_calles, null, false);
                addLayersToMap(credenciales, "FEATURE", "ADDCLIENTECNR", srv_urlClientesCnr, null, true);

                myMapView.addLayer(LyAddPoste, 21);
                myMapView.addLayer(LyAddDireccion, 22);
                myMapView.addLayer(LyAddCliente, 23);
                myMapView.addLayer(LyAddUnion, 24);
                myMapView.addLayer(LyAsocTramo, 25);
                myMapView.addLayer(LyAsocCalle, 26);
                myMapView.addLayer(LyAddClienteCnr, 27);

                setLayerAddToggle(false);
            } else {
                menuMultipleActions.setVisibility(View.GONE);
            }

        } else {
            menuInspeccionActions.setVisibility(View.GONE);
        }
    }

    private void verifPermisos() {
        if (ContextCompat.checkSelfPermission(InspActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(InspActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(InspActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Util.REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            initGeoposition();
        }

        if (ContextCompat.checkSelfPermission(InspActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(InspActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(InspActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Util.REQUEST_READ_EXTERNAL_STORAGE);
            }
        }

        if (ContextCompat.checkSelfPermission(InspActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(InspActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(InspActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Util.REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }

    }

    private void verifAccesoExterno(String sFoto) {

        if (ContextCompat.checkSelfPermission(InspActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(InspActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(InspActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Util.REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }

        if (ContextCompat.checkSelfPermission(InspActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(InspActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(InspActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Util.REQUEST_READ_EXTERNAL_STORAGE);
            }
        } else {
            imageGallery(sFoto);
        }
    }

    private void verifCamara(String sFoto) {
        if (ContextCompat.checkSelfPermission(InspActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(InspActivity.this,
                    Manifest.permission.CAMERA)) {

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(InspActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        Util.REQUEST_CAMERA);
            }
        } else {
            tomarFoto(sFoto);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mImageUri != null)
            outState.putString("Uri", mImageUri.toString());
        int req = getRequestedOrientation();
        outState.putInt("req", req);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("Uri")) {
            mImageUri = Uri.parse(savedInstanceState.getString("Uri"));
        }
        if (savedInstanceState.containsKey("req")) {
            int req = (int) savedInstanceState.get("req");
            if (req == ActivityInfo.SCREEN_ORIENTATION_LOCKED)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }
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

    private void setOpcion(Object o, String widget) {
        if (o != null) {
            if (widget != null) {
                FloatingActionButton oFab = (FloatingActionButton) o;
                if (arrayWidgets.contains(empresa + "@" + widget)) {
                    oFab.setIconDrawable(drawOk);
                    oFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            abrirDialogCrear(v);
                        }
                    });
                } else {
                    oFab.setIconDrawable(drawNo);
                    oFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Snackbar.make(v, "No tiene acceso a ésta opción", Snackbar.LENGTH_SHORT).show();
                            menuMultipleActions.collapse();
                        }
                    });
                }
            } else {
                FloatingActionButton oFab = (FloatingActionButton) o;
                oFab.setIconDrawable(drawOk);
                oFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        abrirDialogCrear(v);
                    }
                });
            }
        }
    }

    private void setEnabledDialog(boolean bEnable) {
        View vDialog = getLayoutValidate(btnUbicacion);

        if (!bEnable) {
            for (View view : vDialog.getTouchables()) {
                if (view.getId() != R.id.btnUbicacion && view.getId() != R.id.btnCancelar && view.getId() != R.id.txtNis && view.getId() != R.id.btnVerifNis)
                    arrayTouchs.add(view);
            }
        }

        for (View touch : arrayTouchs) {
            touch.setEnabled(bEnable);
        }

    }

    private boolean valImage(View image, int res) {
        ImageView valImg = (ImageView) image.findViewById(res);
        if (valImg != null) {
            try {
                int tam = ((BitmapDrawable) valImg.getDrawable()).getBitmap().getByteCount();
                if (tam > 0) return true;
                else return false;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    private int recorrerForm(View v) {
        int contRequeridos = 0;

        for (View view : v.getTouchables()) {

            if (view.getClass().getGenericSuperclass().equals(EditText.class)) {
                EditText oText = (EditText) view;

                TextInputLayout oTextInput = (TextInputLayout) oText.getParentForAccessibility();
                if (oTextInput.getHint() != null && oTextInput.getHint().toString().contains("*")) {
                    if (oText.getText().toString().trim().isEmpty()){
                        contRequeridos++;
                        oText.setError("Campo obligatorio");
                    } else {
                        oText.setError(null);
                        if (oText.getId() == R.id.txtRutInst || oText.getId() == R.id.txtRutTecn){
                            if (!Util.validateRut(oText.getText().toString())) {
                                contRequeridos++;
                                oText.setError("Rut no válido");
                            } else oText.setError(null);
                        }
                    }
                }
            } else if (view.getClass().getGenericSuperclass().equals(CheckBox.class)) {

            } else if (view.getClass().getGenericSuperclass().equals(Spinner.class)) {
                if (view.getId() == R.id.spinnerMarca || view.getId() == R.id.spinnerTipo || view.getId() == R.id.spinnerFase) {
                    Spinner oSpinner = (Spinner) view;
                    if (!bFallo && oSpinner.getSelectedItemPosition() == 0) contRequeridos++;
                }
            }
        }

        //28-09 Se elimina firma como requisito
        //contRequeridos += (valImage(v, R.id.imgFirmaTec)) ? 0 : 1;

        //23-09 Se elimina fotos como requisito
        //contRequeridos += (valImage(v, R.id.imgPhoto1)) ? 0 : 1;
        //contRequeridos += (valImage(v, R.id.imgPhoto2)) ? 0 : 1;
        //contRequeridos += (valImage(v, R.id.imgPhoto3)) ? 0 : 1;

        return contRequeridos;
    }

    private int recorrerDialog(View v) {
        int contRequeridos = 0;

        for (View view : v.getTouchables()) {

            if (view.getClass().getGenericSuperclass().equals(EditText.class)) {
                EditText oText = (EditText) view;
                TextInputLayout oTextInput = (TextInputLayout) oText.getParentForAccessibility();
                if (oTextInput.getHint() != null && oTextInput.getHint().toString().contains("*")) {
                    if (oText.getText().toString().trim().isEmpty())
                        contRequeridos++;
                }
            } else if (view.getClass().getGenericSuperclass().equals(Spinner.class)) {
            }
        }

        return contRequeridos;
    }

    private View getLayoutChkNis(View v) {
        if (v.getId() == R.id.llCliente) {
            return (View) v.getParent();
        } else {
            return getLayoutChkNis((View) v.getParent());
        }
    }

    private View getLayoutValidate(View v) {
        if (v.getId() == R.id.actionDialog) {
            return (View) v.getParent();
        } else {
            return getLayoutValidate((View) v.getParent());
        }
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

    private String getValueNis(View v) {
        String sResp = "";
        for (View view : v.getTouchables()) {
            if (view.getClass().getGenericSuperclass().equals(EditText.class)) {
                sResp = ((EditText) view).getText().toString();
                break;
            }
        }
        return sResp;
    }

    private void setValuesByNis(View v) {
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

    private void cerrarFormCrear(boolean bSave, View v, int idR){
        if (bSave) {
            if (!validarForm(v)) {
                DialogoConfirmacion oDialog = new DialogoConfirmacion();
                oDialog.show(getFragmentManager(), "tagAlert");
                return;
            } else {

                Resources res = getResources();
                InputStream in_s = res.openRawResource(R.raw.index);
                try {
                    View vAction = getLayoutValidate(v);
                    byte[] b = new byte[in_s.available()];
                    in_s.read(b);
                    String sHtml = new String(b);
                    HtmlUtils oHtml = new HtmlUtils(getApplicationContext(), sHtml);

                    String sValueNumMed = "null";

                    for (View view : vAction.getTouchables()) {

                        if (view.getClass().getGenericSuperclass().equals(EditText.class)) {
                            EditText oText = (EditText) view;
                            if (!oText.getText().toString().trim().isEmpty()){
                                String sMapvalue = HtmlUtils.getMapvalue(oText.getId());
                                String sValorChr = oText.getText().toString();
                                oHtml.setValueById(sMapvalue, "txt", sValorChr);
                                if (oText.getId() == R.id.txtOT) {
                                    sValueNumMed = sValorChr;
                                }

                                if (sMapvalue != null && sMapvalue.contains("txt_trabajo_cant")) {
                                    try {
                                        double dValue = Double.valueOf(sValorChr);
                                        oHtml.sumHH += dValue;
                                    } catch (Exception ex) {
                                        Log.e("Double Convert", "error: " + ex.getMessage());
                                    }
                                }
                            }
                        } else if (view.getClass().getGenericSuperclass().equals(CheckBox.class)) {
                            CheckBox oCheck = (CheckBox) view;
                            String sCheck = HtmlUtils.getMapvalue(oCheck.getId());
                            sCheck += oCheck.isChecked() ? "si" : "no";
                            oHtml.setValueById(sCheck, "chk", "");
                        } else if (view.getClass().getGenericSuperclass().equals(Spinner.class)) {
                            Spinner oSpinner = (Spinner) view;
                            oHtml.setValueById(HtmlUtils.getMapvalue(oSpinner.getId()), "txt", oSpinner.getSelectedItem().toString());
                        } else if (view.getClass().getGenericSuperclass().equals(RadioButton.class)) {
                            RadioButton oRadioButton = (RadioButton) view;
                            if (oRadioButton.isChecked()) {
                                String sRadio = HtmlUtils.getMapvalue(((RadioGroup) oRadioButton.getParent()).getId());
                                sRadio += oRadioButton.getText().toString().toLowerCase().replace(" ", "");
                                oHtml.setValueById(sRadio, "rad", "");
                            }
                        } else if (view.getClass().getGenericSuperclass().equals(ImageView.class)) {
                        }
                    }

                    //SUMA TOTAL HH
                    oHtml.setValueById("txt_trabajo_cant_tot", "txt", ""+oHtml.sumHH);

                    //VALIDAR FIRMAS
                    if (valImage(vAction, R.id.imgFirmaIns)) oHtml.setValueById("firm_prop", "img", String.format("%s.jpg", R.id.imgFirmaIns));
                    if (valImage(vAction, R.id.imgFirmaTec)) oHtml.setValueById("firm_tecn", "img", String.format("%s.jpg", R.id.imgFirmaTec));

                    if (valImage(vAction, R.id.imgPhoto1)) oHtml.setValueById("foto_1", "img", "foto1.jpg");
                    if (valImage(vAction, R.id.imgPhoto2)) oHtml.setValueById("foto_2", "img", "foto2.jpg");
                    if (valImage(vAction, R.id.imgPhoto3)) oHtml.setValueById("foto_3", "img", "foto3.jpg");

                    oHtml.setTitleHtml(sValueNumMed);

                    String sHtmlFinal = oHtml.getHtmlFinal();

                    //guardar en disco
                    oHtml.createHtml(sHtmlFinal);

                    //VIA CustomTabs
                    /*String url = oHtml.getPathHtml();
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(this, Uri.parse(url));*/

                    //VIA CHROME scheme
                    if (Util.isPackageExisted("com.android.chrome", this)) {
                        String url = oHtml.getPathHtml();

                        File f = new File(url);

                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                        String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                        String type = mime.getMimeTypeFromExtension(ext);

                        Uri uri = Uri.parse("googlechrome://navigate?url=" + url);

                        Intent in = new Intent(Intent.ACTION_VIEW);
                        //in.setDataAndType(uri, "text/html");

                        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        in.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        //Uri uriExternal = FileProvider.getUriForFile(getApplicationContext(), "cl.gisred.android", f);

                        in.setDataAndType(uri, type);

                        //startActivity(in);
                        startActivity(Intent.createChooser(in, "Escoja Chrome"));
                    } else {
                        Toast.makeText(this, "Debe instalar Chrome, solo preview disponible", Toast.LENGTH_LONG).show();
                        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(oHtml.getPathHtml()));
                        startActivity(myIntent);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        menuMultipleActions.setVisibility(View.VISIBLE);
        menuInspeccionActions.setVisibility(View.VISIBLE);
        fabShowForm.setVisibility(View.GONE);
        formCrear.dismiss();
    }

    private void cerrarDialogCrear(boolean bSave, @Nullable View viewDialog) {
        final AtomicReference<String> resp = new AtomicReference<>("");

        if (bSave) {
            if (!validarVista(viewDialog)) {
                DialogoConfirmacion oDialog = new DialogoConfirmacion();
                oDialog.show(getFragmentManager(), "tagAlert");
                return;
            } else {
                switch (idResLayoutSelect) {
                    case R.layout.dialog_poste:
                        oLyAddGraphs = LyAddPoste;
                        break;
                    case R.layout.dialog_direccion:
                        oLyAddGraphs = LyAddDireccion;
                        break;
                    case R.layout.dialog_cliente:
                        oLyAddGraphs = LyAddCliente;
                        break;
                    case R.layout.dialog_cliente_cnr:
                        oLyAddGraphs = LyAddClienteCnr;
                        break;
                }

                if (oLyAddGraphs != null) {
                    View oView = getLayoutValidate(viewDialog);
                    Util oUtil = new Util(oUbicacion);

                    ArrayList<Map<String, Object>> oAttrToSave = oUtil.getAttrAddByView(oView, idResLayoutSelect, empresa);

                    Map<String, Object> attributes = oAttrToSave.get(0);
                    Graphic newFeatureGraphic = new Graphic(oUbicacion, null, attributes);
                    Graphic[] adds = {newFeatureGraphic};

                    if (idResLayoutSelect == R.layout.dialog_cliente_cnr || idResLayoutSelect == R.layout.dialog_cliente) {
                        addsUnion = oUtil.addAttrUnionPoint(oAttrToSave, oUbicacion);
                    }

                    oLyAddGraphs.applyEdits(adds, null, null, new CallbackListener<FeatureEditResult[][]>() {

                        @Override
                        public void onCallback(FeatureEditResult[][] featureEditResults) {
                            if (featureEditResults[0] != null) {
                                if (featureEditResults[0][0] != null && featureEditResults[0][0].isSuccess()) {

                                    resp.set("Guardado Correctamente Id: " + featureEditResults[0][0].getObjectId());

                                    if (idResLayoutSelect == R.layout.dialog_cliente_cnr || idResLayoutSelect == R.layout.dialog_cliente)
                                        LyAddUnion.applyEdits(addsUnion, null, null, callBackUnion());

                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            Util.showConfirmation(InspActivity.this, resp.get());
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            resp.set("Error al grabar: " + throwable.getLocalizedMessage());
                            Log.w("onError", resp.get());

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(InspActivity.this, resp.get(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    });
                }
            }
        } else {
            resp.set("Cancelado");
            Toast.makeText(InspActivity.this, resp.get(), Toast.LENGTH_LONG).show();
        }

        bMapTap = false;

        if (mBusquedaLayer != null && myMapView.getLayerByID(mBusquedaLayer.getID()) != null)
            myMapView.removeLayer(mBusquedaLayer);

        if (mUbicacionLayer != null && myMapView.getLayerByID(mUbicacionLayer.getID()) != null)
            myMapView.removeLayer(mUbicacionLayer);

        if (mSeleccionLayer != null && myMapView.getLayerByID(mSeleccionLayer.getID()) != null)
            myMapView.removeLayer(mSeleccionLayer);

        oUbicacion = null;
        if (bVerCapas) toogleCapas(fabVerCapas);
        //setLayerAddToggle(false);
        menuMultipleActions.setVisibility(View.VISIBLE);
        menuInspeccionActions.setVisibility(View.VISIBLE);
        fabShowDialog.setVisibility(View.GONE);
        dialogCrear.dismiss();
        if (oLyAddGraphs != null) oLyAddGraphs.setVisible(true);
    }

    private CallbackListener<FeatureEditResult[][]> callBackUnion() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(InspActivity.this, "Aplicando uniones", Toast.LENGTH_SHORT).show();
            }
        });

        return null;
    }

    class AdaptBusqMedidor extends ArrayAdapter<BusqClass> {

        public AdaptBusqMedidor(Context context, BusqClass[] datos) {
            super(context, R.layout.list_item_busq, datos);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View item = inflater.inflate(R.layout.list_item_busq, null);

            TextView lblNis = (TextView) item.findViewById(R.id.LblSerieNis);
            lblNis.setText(datosBusq[position].getSerieNis());

            TextView lblMarca = (TextView) item.findViewById(R.id.LblMarca);
            lblMarca.setText(datosBusq[position].getMarca());

            TextView lblModelo = (TextView) item.findViewById(R.id.LblModelo);
            lblModelo.setText(datosBusq[position].getModelo());

            return (item);
        }
    }

    private void abrirBusqMedidores() {

        lstBusqMedidores.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpiBusqueda = 0;
                callQuery(datosBusq[position].getNis(), getValueByEmp("CLIENTES_XY_006.nis"), LyCLIENTES.getUrl().concat("/0"));
                if (LyCLIENTES.getLayers() != null && LyCLIENTES.getLayers().length > 0)
                    iBusqScale = LyCLIENTES.getLayers()[0].getLayerServiceInfo().getMinScale();
                dialogBusq.dismiss();
            }
        });

        dialogBusq = new Dialog(this);
        dialogBusq.setTitle("Lista Búsqueda");
        dialogBusq.addContentView(lstBusqMedidores, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        dialogBusq.show();
    }

    private void abrirEsquema(View view) {
        ImageView image = new ImageView(this);
        image.setImageResource(R.drawable.esquema_conexion);

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

    private void abrirFormInsp(View view, String form) {

        try {
            HtmlUtils.createPathInspeccion(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        FloatingActionButton fabTemp = (FloatingActionButton) view;
        menuMultipleActions.collapse();
        menuMultipleActions.setVisibility(View.GONE);

        menuInspeccionActions.collapse();
        menuInspeccionActions.setVisibility(View.GONE);
        fabShowForm.setVisibility(View.VISIBLE);

        if (form.contains("MASIVA")) {
            setActionsForm(R.layout.form_inspec_masiva, fabTemp.getTitle());
        } else if (form.contains("AP")) {
            setActionsForm(R.layout.form_inspec_ap, fabTemp.getTitle());
        } else if (form.contains("ESPECIALES")) {
            setActionsForm(R.layout.form_inspec_clientes_esp, fabTemp.getTitle());
        } else if (form.contains("CORRECTIVO")) {
            setActionsForm(R.layout.form_inspec_mant_correctivo, fabTemp.getTitle());
        } else if (form.contains("EMPALMES")) {
            setActionsForm(R.layout.form_inspec_cons_empalme, fabTemp.getTitle());
        } else if (form.contains("TELEMEDIDA")) {
            setActionsForm(R.layout.form_inspec_telemedida, fabTemp.getTitle());
        }
        //setActionsForm(R.layout.form_inspec_masiva, fabTemp.getTitle());

        if (!bVerCapas) toogleCapas(fabVerCapas);
    }

    private void abrirDialogCrear(View view) {

        FloatingActionButton fabTemp = (FloatingActionButton) view;
        menuMultipleActions.collapse();
        menuMultipleActions.setVisibility(View.GONE);

        menuInspeccionActions.collapse();
        menuInspeccionActions.setVisibility(View.GONE);
        fabShowDialog.setVisibility(View.VISIBLE);

        switch (view.getId()) {
            case R.id.action_a:
                setActionsDialog(R.layout.dialog_poste, fabTemp.getTitle());
                break;
            case R.id.action_b:
                setActionsDialog(R.layout.dialog_direccion, fabTemp.getTitle());
                break;
            case R.id.action_c:
                setActionsDialog(R.layout.dialog_cliente, fabTemp.getTitle());
                break;
            case R.id.action_d:
                setActionsDialog(R.layout.dialog_cliente_cnr, fabTemp.getTitle());
                break;
        }

        if (!bVerCapas) toogleCapas(fabVerCapas);
        //setLayerAddToggle(true);
    }

    private boolean validarForm(View view) {

        View vAction = getLayoutValidate(view);
        int iReq = recorrerForm(vAction);
        return (iReq == 0);
    }

    private boolean validarVista(View view) {
        if (oUbicacion == null) {
            return false;
        }
        View vAction = getLayoutValidate(view);
        return (recorrerDialog(vAction) == 0);
    }

    public static class DialogoFirma extends DialogFragment {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());

            builder.setTitle("Firma")
                    .setView(R.layout.form_firma)
                    .setCancelable(true)
                    .setNeutralButton("Limpiar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SignatureView signView = (SignatureView) getDialog().findViewById(R.id.signatureView);
                            signView.clearSignature();
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.cancel();
                        }
                    })
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SignatureView signView = (SignatureView) getDialog().findViewById(R.id.signatureView);

                            //setear imagen
                            ImageView imgFirma = ((InspActivity) ((AlertDialog) dialog).getOwnerActivity()).imgTemp;

                            if (imgFirma != null) {
                                imgFirma.setAdjustViewBounds(true);
                                imgFirma.setImageBitmap(signView.getImage());
                                imgFirma.setCropToPadding(true);
                            }

                            //guardar firma
                            try {
                                PhotoUtils.createFirma(signView.getImage(), getActivity().getBaseContext(), ""+imgFirma.getId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //cerrar dialogo
                            dialog.cancel();
                        }
                    });

            return builder.create();
        }
    }

    public static class DialogoConfirmacion extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());

            builder.setMessage("Debe ingresar todos los campos obligatorios")
                    .setTitle("Advertencia")
                    .setCancelable(false)
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            return builder.create();
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

    private void toogleCapas(View view) {
        if (bVerCapas) {
            ((FloatingActionButton) view).setIcon(R.drawable.ic_eye_off_white_24dp);
        } else {
            ((FloatingActionButton) view).setIcon(R.drawable.ic_eye_white_24dp);
        }

        bVerCapas = !bVerCapas;
        setLayerAddToggle(bVerCapas);
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

            layerDefs = new HashMap<>();
            layerDefs.put(0, "ARCGIS.DBO.ECSE.ano = " + Calendar.getInstance().get(Calendar.YEAR));
            layerDefs.put(1, "ARCGIS.DBO.ECSE.ano = 2016");

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
                            callQuery(txtBusqueda, "serie_medidor", LyMEDIDORES.getUrl().concat("/1"));
                            if (LyMEDIDORES.getLayers() != null && LyMEDIDORES.getLayers().length > 1)
                                iBusqScale = LyMEDIDORES.getLayers()[1].getLayerServiceInfo().getMinScale();
                            break;
                        case 4:
                            iBusqScale = 5000;
                            String[] sBuscar = {eStreet.getText().toString(), eNumber.getText().toString()};
                            String[] sFields = {"nombre_calle", "numero"};
                            callQuery(sBuscar, sFields, LyDIRECCIONES.getUrl().concat("/0"));
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

    public void setActionsForm(final int idRes, String sNombre) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(idRes, null);

        final int topeWidth = 650;
        ArrayAdapter<CharSequence> adapter;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthSize = displayMetrics.widthPixels;
        int widthScale = (int) ((widthSize * 3) / 4);
        if (topeWidth < widthScale) widthScale = topeWidth;

        v.setMinimumWidth(widthScale);

        formCrear.setTitle(sNombre);
        formCrear.setContentView(v);
        idResLayoutSelect = idRes;

        boolean bSpinnerMedidor = (idRes == R.layout.form_inspec_telemedida) ? true : false;

        Spinner spTipoFase = (Spinner) v.findViewById(R.id.spinnerFase);
        adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayTipoFaseInsp);
        spTipoFase.setAdapter(adapter);

        Spinner spTipoMarca = (Spinner) v.findViewById(R.id.spinnerTipo);
        adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, (bSpinnerMedidor) ? arrayTipoMarcaTM : arrayTipoMarca);
        spTipoMarca.setAdapter(adapter);

        Spinner spMarca = (Spinner) v.findViewById(R.id.spinnerMarca);
        adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, (bSpinnerMedidor) ? arrayMarcaTM : arrayMarca);
        spMarca.setAdapter(adapter);

        final GisEditText txtPoste = (GisEditText) v.findViewById(R.id.txtPoste);
        final EditText txtRotulo = (EditText) v.findViewById(R.id.txtRotulo);

        txtPoste.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i2 > 1) {
                    if (bInspRotulo) {
                        txtRotulo.setText(charSequence);
                        if (!txtRotulo.hasFocus()) txtRotulo.requestFocus();
                        bInspRotulo = false;
                        txtPoste.setText(String.format("%s", txtPoste.getIdObjeto()));
                    } else
                        bInspRotulo = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        ImageButton btnIdentPoste = (ImageButton) v.findViewById(R.id.btnPoste);
        btnIdentPoste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                formCrear.hide();
                bMapTap = true;
                bCallOut = true;
                oLySelectAsoc = LyAddPoste;
                oLyExistAsoc = LyPOSTES;
                oLyExistAsoc.setVisible(true);
                myMapView.zoomToScale(ldm.getPoint(), oLyExistAsoc.getMinScale() * 0.9);
                setValueToAsoc(getLayoutContenedor(view));
            }
        });

        final EditText txtFecha = (EditText) v.findViewById(R.id.txtFechaEjec);
        txtFecha.setText(dateFormatter.format(Calendar.getInstance().getTime()));
        txtFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                txtFecha.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        final EditText txtHoraIni = (EditText) v.findViewById(R.id.txtHoraIni);
        txtHoraIni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerDialog.show();
                txtHora = txtHoraIni;
            }
        });

        final EditText txtHoraFin = (EditText) v.findViewById(R.id.txtHoraFin);
        txtHoraFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerDialog.show();
                txtHora = txtHoraFin;
            }
        });

        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                txtHora.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
            }
        }, newCalendar.get(Calendar.HOUR), newCalendar.get(Calendar.MINUTE), false);

        final EditText txtEjecutor = (EditText) v.findViewById(R.id.txtEjecutor);
        txtEjecutor.setText(Util.getUserWithoutDomain(usuar));

        Spinner spFirmante = (Spinner) v.findViewById(R.id.spinnerFirmante);
        if (spFirmante != null) {
            adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayFirmante);
            spFirmante.setAdapter(adapter);
            spFirmante.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    setRequerido(view, R.id.txtRutInst, i > 0);
                    setRequerido(view, R.id.txtNomInst, i > 0);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });
        }

        Spinner spTipoMarcaRet = (Spinner) v.findViewById(R.id.spinnerTipoRet);
        if (spTipoMarcaRet != null) {
            adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayTipoMarca);
            spTipoMarcaRet.setAdapter(adapter);
        }

        Spinner spMarcaRet = (Spinner) v.findViewById(R.id.spinnerMarcaRet);
        if (spMarcaRet != null) {
            adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayMarca);
            spMarcaRet.setAdapter(adapter);
        }

        final CheckBox chkInspFallo = (CheckBox) v.findViewById(R.id.chkInspFallo);
        if (chkInspFallo != null) {
            chkInspFallo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bFallo = chkInspFallo.isChecked();
                    modoFallo(v, bFallo);
                }
            });
        }

        ImageButton btnClose = (ImageButton) v.findViewById(R.id.btnCancelar);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarFormCrear(false, v, 0);
            }
        });

        ImageButton btnOk = (ImageButton) v.findViewById(R.id.btnConfirmar);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarFormCrear(true, v, idRes);
            }
        });

        imgFirmaTec = (ImageView) v.findViewById(R.id.imgFirmaTec);
        imgFirmaInsp = (ImageView) v.findViewById(R.id.imgFirmaIns);
        imgPhoto1 = (ImageView) v.findViewById(R.id.imgPhoto1);
        imgPhoto2 = (ImageView) v.findViewById(R.id.imgPhoto2);
        imgPhoto3 = (ImageView) v.findViewById(R.id.imgPhoto3);

        final ImageButton btnFirmaTec = (ImageButton) v.findViewById(R.id.btnFirmaTec);
        if (btnFirmaTec != null && imgFirmaTec != null) {
            btnFirmaTec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imgTemp = imgFirmaTec;
                    DialogoFirma oDialog = new DialogoFirma();
                    oDialog.show(getFragmentManager(), "tagFirma");
                }
            });

            imgFirmaTec.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    crearDialogoImg((ImageView) v).show();
                    return false;
                }
            });
        }

        final ImageButton btnFirmaInsp = (ImageButton) v.findViewById(R.id.btnFirmaIns);
        if (btnFirmaInsp != null && imgFirmaInsp != null) {
            btnFirmaInsp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imgTemp = imgFirmaInsp;
                    DialogoFirma oDialog = new DialogoFirma();
                    oDialog.show(getFragmentManager(), "tagFirma");
                }
            });

            imgFirmaInsp.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    crearDialogoImg((ImageView) v).show();
                    return false;
                }
            });
        }


        imgPhoto1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                crearDialogoImg((ImageView) v).show();
                return false;
            }
        });

        imgPhoto2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                crearDialogoImg((ImageView) v).show();
                return false;
            }
        });

        imgPhoto3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                crearDialogoImg((ImageView) v).show();
                return false;
            }
        });

        ImageButton btnPhoto1 = (ImageButton) v.findViewById(R.id.btnPhoto1);
        btnPhoto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgTemp = imgPhoto1;
                if (Build.VERSION.SDK_INT >= 22) verifCamara("foto1");
                else tomarFoto("foto1");

            }
        });

        ImageButton btnPhoto2 = (ImageButton) v.findViewById(R.id.btnPhoto2);
        btnPhoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgTemp = imgPhoto2;
                if (Build.VERSION.SDK_INT >= 22) verifCamara("foto2");
                else tomarFoto("foto2");
            }
        });

        ImageButton btnPhoto3 = (ImageButton) v.findViewById(R.id.btnPhoto3);
        btnPhoto3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgTemp = imgPhoto3;
                if (Build.VERSION.SDK_INT >= 22) verifCamara("foto3");
                else tomarFoto("foto3");
            }
        });

        ImageButton btnFile1 = (ImageButton) v.findViewById(R.id.btnFile1);
        btnFile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgTemp = imgPhoto1;
                if (Build.VERSION.SDK_INT >= 22) verifAccesoExterno("foto1");
                else imageGallery("foto1");
            }
        });

        ImageButton btnFile2 = (ImageButton) v.findViewById(R.id.btnFile2);
        btnFile2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgTemp = imgPhoto2;
                if (Build.VERSION.SDK_INT >= 22) verifAccesoExterno("foto2");
                else imageGallery("foto2");
            }
        });

        ImageButton btnFile3 = (ImageButton) v.findViewById(R.id.btnFile3);
        btnFile3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgTemp = imgPhoto3;
                if (Build.VERSION.SDK_INT >= 22) verifAccesoExterno("foto3");
                else imageGallery("foto3");
            }
        });

        arrayTouchs = new ArrayList<>();
        //setEnabledDialog(false);

        formCrear.show();
        dialogCur = formCrear;
    }

    private Dialog crearDialogoImg(final ImageView img){
        AlertDialog.Builder builder = new AlertDialog.Builder(InspActivity.this);
        builder.setCancelable(false);
        builder.setMessage("¿Desea eliminar el contenido?");
        builder.setPositiveButton("Aceptar",    new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                eliminarImg(img);

            }
        });

        builder.setNegativeButton("Cancelar",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.cancel();
            }
        });

        return builder.create();
    }

    private void eliminarImg(ImageView img) {

        if (Build.VERSION.SDK_INT < 23) img.setAdjustViewBounds(true);
        //img.setImageResource(android.R.color.transparent);
        img.setImageDrawable(null);

        Toast.makeText(getApplicationContext(), "Eliminado", Toast.LENGTH_SHORT).show();
    }

    private void modoFallo(View view, boolean bool) {
        // Activa desactiva elementos obligatorios
        setRequerido(view, R.id.txtNumMedidor, bool);
        setRequerido(view, R.id.txtLectura, bool);
        setRequerido(view, R.id.txtPoste, bool);
        setRequerido(view, R.id.txtRotulo, bool);
        setRequerido(view, R.id.txtSellos, bool);
        setRequerido(view, R.id.txtIcp, bool);
        setRequerido(view, R.id.txtVoltF1n, bool);

        // Activa o desactiva texto de olbigacion de spinner
        setTextViewReq(view, R.id.spTextMarca, bool);
        setTextViewReq(view, R.id.spTextTipo, bool);
        setTextViewReq(view, R.id.spTextFase, bool);
    }

    private void setRequerido(View view, int idRequerido, boolean bool) {
        if (bool) {
            EditText oText = (EditText) getLayoutContenedor(view).findViewById(idRequerido);
            TextInputLayout oTextInput = (TextInputLayout) oText.getParentForAccessibility();
            if (oTextInput.getHint() != null && oTextInput.getHint().toString().contains("*")) {
                oTextInput.setHint(oTextInput.getHint().toString().replace("*",""));
            }
        } else {
            EditText oText = (EditText) getLayoutContenedor(view).findViewById(idRequerido);
            TextInputLayout oTextInput = (TextInputLayout) oText.getParentForAccessibility();
            if (oTextInput.getHint() != null && !oTextInput.getHint().toString().contains("*")) {
                oTextInput.setHint(oTextInput.getHint().toString().concat("*"));
            }
        }

    }

    private void setTextViewReq(View view, int idRequerido, boolean bool) {
        if (bool) {
            TextView oText = (TextView) getLayoutContenedor(view).findViewById(idRequerido);
            if (oText.getText() != null && oText.getText().toString().contains("*")) {
                oText.setText(oText.getText().toString().replace("*",""));
            }
        } else {
            TextView oText = (TextView) getLayoutContenedor(view).findViewById(idRequerido);
            if (oText.getText() != null && !oText.getText().toString().contains("*")) {
                oText.setText(oText.getText().toString().concat("*"));
            }
        }
    }

    private void imageGallery(String name) {
        sImgUri = name;
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, ACTIVITY_SELECT_IMAGE);
    }

    private void tomarFoto(String name){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = null;
        try {
            // place where to store camera taken picture
            photo = PhotoUtils.createFile(name, "jpg", InspActivity.this);
            photo.delete();
        } catch (Exception e) {
            Log.v(getClass().getSimpleName(), "Can't create file to take picture!");
        }
        mImageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        startActivityForResult(intent, ACTIVITY_SELECT_FROM_CAMERA);
    }

    public void setActionsDialog(final int idRes, String sNombre) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(idRes, null);

        v.setMinimumWidth(400);

        dialogCrear.setTitle(sNombre);
        dialogCrear.setContentView(v);
        idResLayoutSelect = idRes;

        setSpinnerDialog(idRes, v);

        if (idRes != R.layout.dialog_poste) {
            setButtonAsociacion(v);
        }

        btnUbicacion = (ImageButton) v.findViewById(R.id.btnUbicacion);
        btnUbicacion.setColorFilter(Color.RED);
        btnUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bMapTap = true;
                dialogCrear.hide();
            }
        });

        ImageButton btnClose = (ImageButton) v.findViewById(R.id.btnCancelar);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarDialogCrear(false, null);
            }
        });

        ImageButton btnOk = (ImageButton) v.findViewById(R.id.btnConfirmar);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarDialogCrear(true, v);
            }
        });

        arrayTouchs = new ArrayList<>();
        setEnabledDialog(false);

        dialogCrear.show();
        dialogCur = dialogCrear;
    }

    private void setSpinnerDialog(int idLayout, View v) {

        ArrayAdapter<CharSequence> adapter;

        switch (idLayout) {
            case R.layout.dialog_poste:
                Spinner spTipoPoste = (Spinner) v.findViewById(R.id.spinnerTipoPoste);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayTipoPoste);
                spTipoPoste.setAdapter(adapter);

                Spinner spTipoTension = (Spinner) v.findViewById(R.id.spinnerTension);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayTension);
                spTipoTension.setAdapter(adapter);
                break;
            case R.layout.dialog_direccion:
                //Setear Spinner y recuperar valores
                Spinner spTipoEdif = (Spinner) v.findViewById(R.id.spinnerTipoEdif);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayTipoEdif);
                spTipoEdif.setAdapter(adapter);
                break;
            case R.layout.dialog_cliente:
                Spinner spTipoMedidor = (Spinner) v.findViewById(R.id.spinnerTipoMedidor);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayMedidor);
                spTipoMedidor.setAdapter(adapter);

                Spinner spTecMedidor = (Spinner) v.findViewById(R.id.spinnerTecMedidor);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayTecMedidor);
                spTecMedidor.setAdapter(adapter);

                Spinner spEmpalme = (Spinner) v.findViewById(R.id.spinnerTipoEmpalme);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayEmpalme);
                spEmpalme.setAdapter(adapter);

                Spinner spTipoFase = (Spinner) v.findViewById(R.id.spinnerFaseConex);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayTipoFase);
                spTipoFase.setAdapter(adapter);
                break;
            case R.layout.dialog_cliente_cnr:
                Spinner spTipoMedidorCnr = (Spinner) v.findViewById(R.id.spinnerTipoMedidor);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayMedidor);
                spTipoMedidorCnr.setAdapter(adapter);

                Spinner spTecMedidorCnr = (Spinner) v.findViewById(R.id.spinnerTecMedidor);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayTecMedidor);
                spTecMedidorCnr.setAdapter(adapter);

                Spinner spEmpalmeCnr = (Spinner) v.findViewById(R.id.spinnerTipoEmpalme);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayEmpalme);
                spEmpalmeCnr.setAdapter(adapter);

                Spinner spTipoCnr = (Spinner) v.findViewById(R.id.spinnerTipoCNR);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayTipoCnr);
                spTipoCnr.setAdapter(adapter);

                Spinner spTipoFaseCnr = (Spinner) v.findViewById(R.id.spinnerFaseConex);
                adapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, arrayTipoFase);
                spTipoFaseCnr.setAdapter(adapter);
                break;
        }
    }

    private void setButtonAsociacion(View v) {

        if (idResLayoutSelect == R.layout.dialog_direccion) {

            ImageButton btnAsocCalle = (ImageButton) v.findViewById(R.id.btnAsocCalle);
            btnAsocCalle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogCrear.hide();
                    bMapTap = true;
                    bCallOut = true;
                    nIndentify = 1; //1 = valor para calle
                    oLySelectAsoc = LyAsocCalle;
                    //myMapView.zoomToScale(oUbicacion, 700.0f);
                    setValueToAsoc(getLayoutContenedor(v));
                }
            });
            return;
        }

        ImageButton btnVerifNis = (ImageButton) v.findViewById(R.id.btnVerifNis);
        btnVerifNis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sUrl = (idResLayoutSelect == R.layout.dialog_cliente) ? srv_urlClientes : srv_urlClientesCnr;
                String sNis = getValueNis(getLayoutContenedor(v));
                if (!sNis.isEmpty()) {
                    verifNisQuery(sNis, "nis", sUrl, v);
                } else {
                    Toast.makeText(getApplicationContext(), "El campo NIS está vacío", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton btnAsocDireccion = (ImageButton) v.findViewById(R.id.btnAsocDireccion);
        btnAsocDireccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCrear.hide();
                bMapTap = true;
                bCallOut = true;
                oLySelectAsoc = LyAddDireccion;
                oLyExistAsoc = LyDIRECCIONES;
                setValueToAsoc(getLayoutContenedor(v));
            }
        });

        ImageButton btnAsocPoste = (ImageButton) v.findViewById(R.id.btnAsocPoste);
        btnAsocPoste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCrear.hide();
                bMapTap = true;
                bCallOut = true;
                oLySelectAsoc = LyAddPoste;
                oLyExistAsoc = LyPOSTES;
                setValueToAsoc(getLayoutContenedor(v));
            }
        });

        if (idResLayoutSelect == R.layout.dialog_cliente_cnr) {
            ImageButton btnAsocTramo = (ImageButton) v.findViewById(R.id.btnAsocTramo);
            btnAsocTramo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogCrear.hide();
                    bMapTap = true;
                    bCallOut = true;
                    nIndentify = 2; //2 = valor para tramo
                    oLySelectAsoc = LyAsocTramo;
                    setValueToAsoc(getLayoutContenedor(v));
                }
            });
        }
    }

    public void verifNisQuery(String txtBusqueda, String nomCampo, String dirUrl, View v) {
        String sWhere = String.format("%s = %s", nomCampo, txtBusqueda);

        NisVerifResult queryTask = new NisVerifResult(getLayoutChkNis(v));
        queryTask.execute(sWhere, dirUrl);
    }

    public void callQuery(String txtBusqueda, String nomCampo, String dirUrl) {
        String sWhere = String.format("%s = '%s'", nomCampo, txtBusqueda);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(InspActivity.this);

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
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(InspActivity.this);

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

    public void setLayerAddToggle(boolean visible) {
        LyAddPoste.setVisible(visible);
        LyAddDireccion.setVisible(visible);
        LyAddCliente.setVisible(visible);
        LyAddUnion.setVisible(visible);
        LyAddClienteCnr.setVisible(visible);
        LyAsocTramo.setVisible(false);
        LyAsocCalle.setVisible(false);

        if (fabShowDialog.getVisibility() == View.VISIBLE) {
            if (idResLayoutSelect == R.layout.dialog_cliente_cnr) LyAsocTramo.setVisible(visible);
            if (idResLayoutSelect == R.layout.dialog_direccion) LyAsocCalle.setVisible(visible);
        }
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
            case "ELECTRODEP":
                din_urlElectroDep = layerURL;
                break;
            case "SRV_POSTES":
                srv_urlPostes = layerURL;
                break;
            case "SRV_DIRECCIONES":
                srv_urlDireccion = layerURL;
                break;
            case "SRV_CLIENTES":
                srv_urlClientes = layerURL;
                break;
            case "SRV_CLIENTESCNR":
                srv_urlClientesCnr = layerURL;
                break;
            case "SRV_UNIONES":
                srv_urlUnion012 = layerURL;
                break;
            case "SRV_CALLES":
                srv_calles = layerURL;
                break;
            default:
                Toast.makeText(InspActivity.this, "Problemas inicializando layers url", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void addLayersToMap(UserCredentials credencial, String tipoLayer, String nombreCapa, String url, String mode, boolean visibilidad) {

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
                        Toast.makeText(InspActivity.this, "FeatureLayer debe tener un modo.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                case "ADDPOSTE":
                    LyAddPoste = new ArcGISFeatureLayer(url, ArcGISFeatureLayer.MODE.ONDEMAND, credencial);
                    LyAddPoste.setDefinitionExpression(String.format("EMPRESA = '%s'", empresa));
                    LyAddPoste.setMinScale(8000);
                    LyAddPoste.setVisible(visibilidad);
                    break;
                case "ADDADDRESS":
                    LyAddDireccion = new ArcGISFeatureLayer(url, ArcGISFeatureLayer.MODE.ONDEMAND, credencial);
                    LyAddDireccion.setDefinitionExpression(String.format("EMPRESA = '%s'", empresa));
                    LyAddDireccion.setMinScale(4500);
                    LyAddDireccion.setVisible(visibilidad);
                    break;
                case "ADDCLIENTE":
                    LyAddCliente = new ArcGISFeatureLayer(url, ArcGISFeatureLayer.MODE.ONDEMAND, credencial);
                    LyAddCliente.setDefinitionExpression(String.format("EMPRESA = '%s' AND ESTADO IS null", empresa));
                    LyAddCliente.setMinScale(6000);
                    LyAddCliente.setVisible(visibilidad);
                    break;
                case "ADDCLIENTECNR":
                    LyAddClienteCnr = new ArcGISFeatureLayer(url, ArcGISFeatureLayer.MODE.ONDEMAND, credencial);
                    LyAddClienteCnr.setDefinitionExpression(String.format("EMPRESA = '%s' AND ESTADO IS null", empresa));
                    LyAddClienteCnr.setMinScale(6000);
                    LyAddClienteCnr.setVisible(visibilidad);
                    break;
                case "ADDUNION":
                    LyAddUnion = new ArcGISFeatureLayer(url, ArcGISFeatureLayer.MODE.ONDEMAND, credencial);
                    LyAddUnion.setDefinitionExpression(String.format("EMPRESA = '%s'", empresa));
                    LyAddUnion.setMinScale(4500);
                    LyAddUnion.setVisible(visibilidad);
                    break;
                case "ASOCTRAMO":
                    LyAsocTramo = new ArcGISFeatureLayer(url, ArcGISFeatureLayer.MODE.ONDEMAND, credencial);
                    LyAsocTramo.setMinScale(6000);
                    LyAsocTramo.setVisible(visibilidad);
                    break;
                case "ASOCCALLE":
                    LyAsocCalle = new ArcGISFeatureLayer(url, ArcGISFeatureLayer.MODE.ONDEMAND, credencial);
                    //LyAsocCalle.setDefinitionExpression("where ESTADO IS null");
                    LyAsocCalle.setMinScale(6000);
                    LyAsocCalle.setVisible(visibilidad);
                    break;
                default:
                    Toast.makeText(InspActivity.this, "Problemas agregando layers url", Toast.LENGTH_SHORT).show();
                    break;
            }

        } else {
            if (mode != null) {
                Toast.makeText(InspActivity.this, "Layer dinámico no tiene mode, debe ser null", Toast.LENGTH_SHORT).show();
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
                        array11[0] = 1;
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
                        LyENCUESTA.setLayerDefinitions(layerDefs);
                        LyENCUESTA.setVisible(visibilidad);
                        break;
                    case "REEMPLAZO":
                        int array19[];
                        array19 = new int[1];
                        array19[0] = 1;
                        LyREEMPLAZO = new ArcGISDynamicMapServiceLayer(url, array19, credencial);
                        LyREEMPLAZO.setLayerDefinitions(layerDefs);
                        LyREEMPLAZO.setVisible(visibilidad);
                        break;
                    case "ELECTRODEP":
                        int array20[];
                        array20 = new int[1];
                        array20[0] = 0;
                        LyELECTRODEP = new ArcGISDynamicMapServiceLayer(url, array20, credencial);
                        LyELECTRODEP.setVisible(visibilidad);
                        break;
                    default:
                        Toast.makeText(InspActivity.this, "Problemas agregando layers dinámicos.", Toast.LENGTH_SHORT).show();
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
                            Log.w("MapsActivity", "Selected Graphics " + selectedFeatures.length);

                            if (selectedFeatures.length > 0) {
                                Graphic[] results = oLySelectAsoc.getSelectedFeatures();
                                Callout mapCallout = myMapView.getCallout();
                                mapCallout.hide();

                                for (Graphic graphic : results) {

                                    Map<String, Object> attr = graphic.getAttributes();
                                    Util oUtil = new Util();
                                    CalloutTvClass oCall = oUtil.getCalloutValues(attr);

                                    GisTextView tv = new GisTextView(InspActivity.this);
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
                        SimpleMarkerSymbol oMarketSymbol = new SimpleMarkerSymbol(Color.BLACK, 10, SimpleMarkerSymbol.STYLE.CIRCLE);
                        oMarketSymbol.setOutline(new SimpleLineSymbol(Color.RED, 1, SimpleLineSymbol.STYLE.SOLID));
                        Graphic oGraph = new Graphic(oPoint, oMarketSymbol);
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
                            LyELECTRODEP.reinitializeLayer(creds);
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

    private class NisVerifResult extends AsyncTask<String, Void, FeatureResult> {

        View vLayout;

        public NisVerifResult(View v) {
            vLayout = v;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(InspActivity.this);
            progress = ProgressDialog.show(InspActivity.this, "",
                    "Verificando NIS.");
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

                return results;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(FeatureResult results) {
            if (results != null) {
                int numResult = (int) results.featureCount();

                if (numResult > 0) {
                    for (Object element : results) {
                        progress.incrementProgressBy(numResult / 100);

                        if (element instanceof Feature) {

                            Feature feature = (Feature) element;
                            myMapView.setExtent(feature.getGeometry(), 0, true);
                            Util oUtil = new Util();

                            oUbicacion = myMapView.getCenter();

                            oUtil.setAttrInView(idResLayoutSelect, vLayout, feature.getAttributes());

                            /*GisTextView tv = new GisTextView(MapsActivity.this);
                            tv.setText(outStr.toString());
                            tv.setPoint((Point) feature.getGeometry());*/

                            break;
                        }
                    }

                    myMapView.zoomin(true);
                } else {
                    Toast.makeText(InspActivity.this, "NIS no registrado", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(InspActivity.this, "Falló la verificación, intente nuevamente", Toast.LENGTH_SHORT).show();
            }
            progress.dismiss();
        }
    }

    private class AsyncQueryTask extends AsyncTask<String, Void, FeatureResult> {

        FeatureResult oResultTramos;
        FeatureResult oResultNis;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(InspActivity.this);
            progress = ProgressDialog.show(InspActivity.this, "",
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
                } else if (SpiBusqueda == 3) {

                    if (results != null && results.featureCount() == 1) {
                        for (Object element : results) {
                            Feature feature = (Feature) element;
                            String sNis = feature.getAttributeValue("nis").toString();

                            String sWhere = getValueByEmp("CLIENTES_XY_006.nis = '") + sNis + "'";

                            QueryParameters oParam = new QueryParameters();
                            oParam.setWhere(sWhere);

                            oParam.setReturnGeometry(true);
                            oParam.setOutFields(new String[]{"*"});

                            String urlClientes = LyCLIENTES.getUrl().concat("/0");

                            QueryTask oQueryNis = new QueryTask(urlClientes, credenciales);
                            oResultNis = oQueryNis.execute(oParam);

                            if (oResultNis.featureCount() > 0) break;
                        }
                    }
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

                if (SpiBusqueda == 3) {

                    try {

                        if (results.featureCount() > 1){
                            lstBusqMedidores = new ListView(getApplicationContext());
                            datosBusq = new BusqClass[numResult];
                            int cont = 0;

                            for (Object element : results) {
                                Feature feature = (Feature) element;

                                String sNis = feature.getAttributeValue("nis").toString();
                                String sSerie = feature.getAttributeValue("serie_medidor").toString();
                                String sMarca = feature.getAttributeValue("marca_medidor").toString();
                                String sModelo = feature.getAttributeValue("modelo").toString();

                                BusqClass oMedidor = new BusqClass(sSerie+"-"+sNis, sMarca, sModelo);
                                datosBusq[cont] = oMedidor;
                                cont++;
                            }

                            AdaptBusqMedidor adaptador;
                            adaptador = new AdaptBusqMedidor(InspActivity.this, datosBusq);

                            lstBusqMedidores.setAdapter(adaptador);

                            abrirBusqMedidores();

                        } else if (results.featureCount() == 1) {
                            for (Object oNis : oResultNis) {
                                Feature fNis = (Feature) oNis;

                                myMapView.setExtent(fNis.getGeometry(), 0, true);

                                SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.RED, 16, SimpleMarkerSymbol.STYLE.CROSS);
                                Graphic resultLocGraphic = new Graphic(fNis.getGeometry(), resultSymbol);
                                mBusquedaLayer.addGraphic(resultLocGraphic);

                                Callout mapCallout = myMapView.getCallout();
                                fabNavRoute.setVisibility(View.GONE);
                                mapCallout.hide();

                                StringBuilder outStr;
                                Util oUtil = new Util();
                                outStr = oUtil.getStringByAttrClass(0, fNis.getAttributes());
                                GisTextView tv = new GisTextView(InspActivity.this);
                                tv.setText(outStr.toString());
                                tv.setPoint((Point) fNis.getGeometry());
                                tv.setTextColor(Color.WHITE);

                                tv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        myMapView.getCallout().hide();
                                        fabNavRoute.setVisibility(View.GONE);
                                    }
                                });

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

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {

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
                            outStr = oUtil.getStringByAttrClass(SpiBusqueda, feature.getAttributes());
                            GisTextView tv = new GisTextView(InspActivity.this);
                            tv.setText(outStr.toString());
                            tv.setPoint((Point) feature.getGeometry());
                            tv.setTextColor(Color.WHITE);

                            tv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    myMapView.getCallout().hide();
                                    fabNavRoute.setVisibility(View.GONE);
                                }
                            });

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
                }

                myMapView.zoomin(true);
            } else {
                Toast.makeText(InspActivity.this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
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
            progress = ProgressDialog.show(InspActivity.this, "Identificando sector", "Espere un momento ...");
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

                            GisTextView tv = new GisTextView(InspActivity.this);
                            tv.setText(outStr.toString());
                            tv.setTextColor(Color.WHITE);

                            if (identifyResult.getGeometry().getClass() != Point.class) {
                                if (identifyResult.getGeometry().getClass() == Polyline.class) {
                                    Polyline oPolyline = (Polyline) identifyResult.getGeometry();
                                    tv.setPoint(oUtil.calculateCenterPolyline(oPolyline));
                                } else tv.setPoint(oPoint);
                            } else tv.setPoint((Point) identifyResult.getGeometry());

                            tv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    myMapView.getCallout().hide();
                                    fabNavRoute.setVisibility(View.GONE);
                                }
                            });

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

                            GisTextView tv = new GisTextView(InspActivity.this);
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
                Toast.makeText(InspActivity.this, "No hay datos", Toast.LENGTH_SHORT).show();
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            PhotoUtils photoUtils = new PhotoUtils(getApplicationContext());

            //Cambio de orientacion imgTemp = null
            if (imgTemp == null) {
                Toast.makeText(getApplicationContext(), "Ocupe la misma orientación en que está usando GISRED", Toast.LENGTH_LONG).show();
                return;
            }

            //Parche temporal cambios de ajuste
            if (Build.VERSION.SDK_INT < 23) imgTemp.setAdjustViewBounds(true);
            imgTemp.setCropToPadding(true);

            if (requestCode == ACTIVITY_SELECT_IMAGE) {
                mImageUri = data.getData();
                Log.w("onActivityResult", "URI: " + mImageUri.getPath());
                imgTemp.setImageBitmap(photoUtils.getImage(mImageUri, 250));
                try {
                    photoUtils.copyFromGallery(mImageUri, sImgUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == ACTIVITY_SELECT_FROM_CAMERA) {
                Log.w("onActivityResult", "URI: " + mImageUri.getPath());
                photoUtils.copyToGallery(mImageUri);
                Bitmap oBitmap = photoUtils.getImage(mImageUri, 250);
                Log.w("onActivityResult", String.format("W: %s H: %s", oBitmap.getWidth(), oBitmap.getHeight()));
                imgTemp.setImageBitmap(oBitmap);
            }
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

                    Toast.makeText(InspActivity.this, "No puede utilizar GPS, contacte soporte", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case Util.REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(InspActivity.this, "Ya puede utilizar archivos, presione nuevamente", Toast.LENGTH_LONG).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Toast.makeText(InspActivity.this, "No puede utilizar archivos, contacte soporte", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case Util.REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(InspActivity.this, "Ya puede utilizar camara, presione nuevamente", Toast.LENGTH_LONG).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Toast.makeText(InspActivity.this, "No puede utilizar camara, contacte soporte", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case Util.REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(InspActivity.this, "Ya puede copiar archivos, presione nuevamente", Toast.LENGTH_LONG).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Toast.makeText(InspActivity.this, "No puede copiar archivos, contacte soporte", Toast.LENGTH_LONG).show();
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
