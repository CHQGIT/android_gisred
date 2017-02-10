package cl.gisred.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class EmpActivity extends AppCompatActivity {

    ArrayList<String> aEmpresas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appEmp);
        setSupportActionBar(toolbar);

        try {
            final Bundle bundle = getIntent().getExtras();
            aEmpresas = bundle.getStringArrayList("empresas");

            ImageButton btnChilquinta = (ImageButton) findViewById(R.id.btnChilquinta);
            if (aEmpresas.contains("chilquinta")) {
                btnChilquinta.setImageResource(R.drawable.chilquinta);
                btnChilquinta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToMainMenu(bundle, "chilquinta");
                    }
                });
            }

            ImageButton btnLitoral = (ImageButton) findViewById(R.id.btnLitoral);
            if (aEmpresas.contains("litoral")) {
                btnLitoral.setImageResource(R.drawable.litoral);
                btnLitoral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToMainMenu(bundle, "litoral");
                    }
                });
            }

            ImageButton btnCasablanca = (ImageButton) findViewById(R.id.btnCasablanca);
            if (aEmpresas.contains("casablanca")) {
                btnCasablanca.setImageResource(R.drawable.casablanca);
                btnCasablanca.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToMainMenu(bundle, "casablanca");
                    }
                });
            }

            ImageButton btnLinares = (ImageButton) findViewById(R.id.btnLinares);
            if (aEmpresas.contains("linares")) {
                btnLinares.setImageResource(R.drawable.linares);
                btnLinares.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToMainMenu(bundle, "linares");
                    }
                });
            }

            ImageButton btnParral = (ImageButton) findViewById(R.id.btnParral);
            if (aEmpresas.contains("parral")) {
                btnParral.setImageResource(R.drawable.parral);
                btnParral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToMainMenu(bundle, "parral");
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "Landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "Portrait", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToMainMenu(Bundle oBundle, String mEmpresa) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        oBundle.putString("empresa", mEmpresa);
        intent.putExtras(oBundle);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                DialogoConfirmacion dialogo = new DialogoConfirmacion();
                dialogo.show(getFragmentManager(), "tagAlerta");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        /*DialogoConfirmacion dialogo = new DialogoConfirmacion();
        dialogo.show(getFragmentManager(), "tagAlerta");*/

    }

    public static class DialogoConfirmacion extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());

            builder.setMessage("¿Desea cerrar la sesión?")
                    .setTitle("Confirmacion")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            logout();
                            dialog.cancel();
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            return builder.create();
        }

        private void logout() {
            SharedPreferences prefs = getActivity().getSharedPreferences("GisRedPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("username");
            editor.remove("password");
            editor.apply();
        }
    }
}
