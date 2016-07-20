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
    private String sEmpresa;

    String usuario, password;
    UserCredentials credenciales;

    // Variables de acceso
    ArrayList arrayModulos = new ArrayList(Arrays.asList("STANDARD", "INGRESO_CLIENTES"));
    ArrayList<String> arrayWidgets = new ArrayList(Arrays.asList("STANDARD", "INGRESO_CLIENTES_TECNO", "INGRESO_CLIENTES_CNR"));

    Bundle bundleSpinner = null;

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
            Toast.makeText(MainActivity.this, "No hay datos, verifique contraseña", Toast.LENGTH_LONG).show();
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

        Bundle bundle = getIntent().getExtras();
        aModulos = bundle.getStringArrayList("modulos");
        usuario = bundle.getString("usuarioLogin");
        password = bundle.getString("passwordLogin");
        sEmpresa = bundle.getString("empresa");
        bundleSpinner = bundle.getBundle("options");

        setCredenciales(usuario, password);

        getWidgets();

        lstOpciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (view.isEnabled()){
                    Intent oIntent = new Intent(MainActivity.this, MapsActivity.class);
                    Bundle oBundle = new Bundle();
                    oBundle.putString("empresa", sEmpresa);
                    oBundle.putString("usuario", usuario);
                    oBundle.putString("password", password);
                    oBundle.putString("modulo", datos[position].getTitulo());
                    oBundle.putStringArrayList("widgets", arrayWidgets);
                    oBundle.putBundle("options", bundleSpinner);
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
            }
            return dato;
        }
    }
}




