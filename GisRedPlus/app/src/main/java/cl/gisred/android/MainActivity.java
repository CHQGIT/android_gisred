package cl.gisred.android;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.core.io.UserCredentials;

import java.util.ArrayList;
import java.util.Arrays;

import cl.gisred.android.entity.MenuClass;


public class MainActivity extends AppCompatActivity {


    private MenuClass[] datos;
    private ListView lstOpciones;
    ArrayList<String> aModulos;
    ArrayList<String> aWidgets;
    private String sEmpresa;
    private Bundle bundle;

    String usuario, password;
    UserCredentials credenciales;

    // Variables de acceso
    ArrayList arrayModulos = new ArrayList(Arrays.asList("STANDARD", "INGRESO_CLIENTES", "PROTOCOLO_INSPECCION", "LECTORES", "TELEMEDIDA", "CATASTRO_AP", "POWER_ON"));

    public void setCredenciales(String usuario , String password) {
        credenciales = new UserCredentials();
        credenciales.setUserAccount(usuario, password);
    }

    public void getWidgets()
    {
        if (arrayModulos != null && arrayModulos.size() > 0) {

            datos = new MenuClass[arrayModulos.size()];
            int cont = 0;

            for (Object modulo : arrayModulos) {

                try {
                    String sModulo = (String) modulo;
                    String sComplexModulo = sEmpresa + "@" + sModulo;
                    MenuClass oMenu = new MenuClass(sModulo.replace("_", " "), aModulos.contains(sComplexModulo));
                    datos[cont] = oMenu;
                    cont++;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        if (datos != null && datos.length > 0) {
            AdaptadorMenus adaptador;
            adaptador = new AdaptadorMenus(MainActivity.this, datos);

            lstOpciones.setAdapter(adaptador);
        }
        else {
            Toast.makeText(MainActivity.this, "No hay datos, verifique credenciales", Toast.LENGTH_LONG).show();
            Intent oIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(oIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        lstOpciones = (ListView)findViewById(R.id.LstOpciones);

        bundle = getIntent().getExtras();
        aModulos = bundle.getStringArrayList("modulos");
        aWidgets = bundle.getStringArrayList("widgets");
        usuario = bundle.getString("usuarioLogin");
        password = bundle.getString("passwordLogin");
        sEmpresa = bundle.getString("empresa");

        setCredenciales(usuario, password);

        getWidgets();

        lstOpciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (view.isEnabled()){
                    Intent oIntent;
                    Bundle oBundle = new Bundle();

                    if (datos[position].getTitulo().contains("INSPECCION")){
                        oIntent = new Intent(MainActivity.this, FormActivity.class);
                        oBundle.putStringArrayList("modulos", aModulos);
                    } else if (datos[position].getTitulo().contains("LECTORES")){
                        oIntent = new Intent(MainActivity.this, LectorActivity.class);
                        oBundle.putStringArrayList("modulos", aModulos);
                    } else if (datos[position].getTitulo().contains("TELEMEDIDA")){
                        oIntent = new Intent(MainActivity.this, TelemedidaActivity.class);
                        oBundle.putStringArrayList("modulos", aModulos);
                    } else if (datos[position].getTitulo().contains("CATASTRO")){
                        oIntent = new Intent(MainActivity.this, CatastroActivity.class);
                        oBundle.putStringArrayList("modulos", aModulos);
                    } else
                        oIntent = new Intent(MainActivity.this, MapsActivity.class);

                    oBundle.putString("empresa", sEmpresa);
                    oBundle.putString("usuario", usuario);
                    oBundle.putString("password", password);
                    oBundle.putString("modulo", datos[position].getTitulo());
                    oBundle.putStringArrayList("widgets", aWidgets);
                    oIntent.putExtras(oBundle);
                    startActivity(oIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "No tiene permisos para éste módulo", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class AdaptadorMenus extends ArrayAdapter<MenuClass> {

        public AdaptadorMenus(Context context, MenuClass[] datos) {
            super(context, R.layout.list_item_menu, datos);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View item = inflater.inflate(R.layout.list_item_menu, null);

            TextView lblTitulo = (TextView)item.findViewById(R.id.LblTitulo);
            lblTitulo.setText(datos[position].getTitulo());

            item.setEnabled(datos[position].getEstado());
            datos[position] = getDataByModule(datos[position]);

            TextView lblDescripcion = (TextView)item.findViewById(R.id.LblDescripcion);
            lblDescripcion.setText(datos[position].getDescripcion());

            ImageView oImage = (ImageView) item.findViewById(R.id.imageMenu);
            oImage.setImageResource(datos[position].getRes());

            return(item);
        }

        private MenuClass getDataByModule(MenuClass dato) {
            if (dato.getTitulo().contains("STANDARD")) {
                dato.setDescripcion("Módulo de visualización standard");
                dato.setRes((dato.getEstado()) ? R.mipmap.ic_menu_standard : R.mipmap.ic_menu_standard_g);
            } else if (dato.getTitulo().contains("CLIENTES")) {
                dato.setDescripcion("Visualización e ingreso clientes en terreno");
                dato.setRes((dato.getEstado()) ? R.mipmap.ic_menu_ing_clientes : R.mipmap.ic_menu_ing_clientes_g);
            } else if (dato.getTitulo().contains("INSPECCION")) {
                dato.setDescripcion("Visualización e ingreso de inspecciones");
                dato.setRes((dato.getEstado()) ? R.mipmap.ic_menu_protocolo_inspeccion : R.mipmap.ic_menu_protocolo_inspeccion_g);
            } else if (dato.getTitulo().contains("LECTORES")) {
                dato.setDescripcion("Visualización e ingreso de lecturas");
                dato.setRes((dato.getEstado()) ? R.mipmap.ic_menu_ing_lectores : R.mipmap.ic_menu_ing_lectores_g);
            } else if (dato.getTitulo().contains("TELEMEDIDA")) {
                dato.setDescripcion("Visualización e ingreso de telemedidas");
                dato.setRes((dato.getEstado()) ? R.mipmap.ic_menu_telemedida : R.mipmap.ic_menu_telemedida_g);
            } else if (dato.getTitulo().contains("CATASTRO")) {
                dato.setDescripcion("Visualización e ingreso de catastros AP");
                dato.setRes((dato.getEstado()) ? R.mipmap.ic_menu_protocolo_inspeccion : R.mipmap.ic_menu_protocolo_inspeccion_g);
            } else if (dato.getTitulo().contains("POWER")) {
                dato.setDescripcion("Visualización de interrupciones");
                dato.setRes((dato.getEstado()) ? R.mipmap.ic_menu_power_on : R.mipmap.ic_menu_power_on_g);
            }
            return dato;
        }
    }
}




