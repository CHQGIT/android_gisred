package cl.gisred.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


public class FormActivity extends AppCompatActivity {


    private MenuClass[] datos;
    private ListView lstOpciones;
    ArrayList<String> aForms;
    private String sEmpresa;
    private Bundle bundle;

    String usuario, password;
    UserCredentials credenciales;

    // Variables de acceso
    ArrayList arrayForms = new ArrayList(Arrays.asList("INSPECCION_MASIVA", "INSPECCION_AP", "INSPECCION_CLIENTES_ESPECIALES"));

    public void setCredenciales(String usuario , String password) {
        credenciales = new UserCredentials();
        credenciales.setUserAccount(usuario, password);
    }

    public void getForms()
    {
        if (arrayForms != null && arrayForms.size() > 0) {

            datos = new MenuClass[arrayForms.size()];
            int cont = 0;

            for (Object form : arrayForms) {

                try {
                    String sForm = (String) form;
                    String sComplexModulo = sEmpresa + "@" + sForm;
                    MenuClass oMenu = new MenuClass(sForm.replace("_", " "), aForms.contains(sComplexModulo));
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
            adaptador = new AdaptadorMenus(FormActivity.this, datos);

            lstOpciones.setAdapter(adaptador);
        }
        else {
            Toast.makeText(FormActivity.this, "No hay datos, verifique credenciales", Toast.LENGTH_LONG).show();
            Intent oIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(oIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        lstOpciones = (ListView)findViewById(R.id.LstOpciones);

        bundle = getIntent().getExtras();
        aForms = bundle.getStringArrayList("widgets");
        usuario = bundle.getString("usuario");
        password = bundle.getString("password");
        sEmpresa = bundle.getString("empresa");

        setCredenciales(usuario, password);

        getForms();

        lstOpciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (view.isEnabled()){
                    Intent oIntent = new Intent(FormActivity.this, InspActivity.class);

                    bundle.putString("form", datos[position].getTitulo());
                    oIntent.putExtras(bundle);
                    startActivity(oIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "No tiene permisos para éste formulario", Toast.LENGTH_SHORT).show();
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
            if (dato.getTitulo().contains("MASIVA")) {
                dato.setDescripcion("Formulario de inspección masiva");
                //dato.setRes((dato.getEstado()) ? R.mipmap.ic_menu_protocolo_inspeccion : R.mipmap.ic_menu_protocolo_inspeccion_g);
            } else if (dato.getTitulo().contains("AP")) {
                dato.setDescripcion("Formulario de inspección AP");
                //dato.setRes((dato.getEstado()) ? R.mipmap.ic_menu_ing_clientes : R.mipmap.ic_menu_ing_clientes_g);
            } else if (dato.getTitulo().contains("ESPECIALES")) {
                dato.setDescripcion("Formulario de inspección de clientes especiales");
                //dato.setRes((dato.getEstado()) ? R.mipmap.ic_menu_ing_clientes : R.mipmap.ic_menu_ing_clientes_g);
            }
            dato.setRes((dato.getEstado()) ? R.mipmap.ic_menu_protocolo_inspeccion : R.mipmap.ic_menu_protocolo_inspeccion_g);
            return dato;
        }
    }
}




