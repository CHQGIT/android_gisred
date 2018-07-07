package cl.gisred.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.esri.core.map.FeatureResult;
import com.esri.core.tasks.query.OutStatistics;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Map;

import cl.gisred.android.classes.GifView;
import cl.gisred.android.util.Util;

public class EmpActivity extends AppCompatActivity {

    ArrayList<String> aEmpresas;
    Dialog formNews;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appEmp);
        setSupportActionBar(toolbar);

        try {
            final Bundle bundle = getIntent().getExtras();
            aEmpresas = bundle.getStringArrayList("empresas");

            //TODO activar statistics
            //StatisticsTask queryTask = new StatisticsTask();
            //queryTask.execute(getResources().getString(R.string.url_ECSE_varios));

            ImageButton btnChilquinta = (ImageButton) findViewById(R.id.btnChilquinta);
            if (aEmpresas.contains("chilquinta")) {
                btnChilquinta.setImageResource(R.drawable.chilquinta);
                btnChilquinta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToMainMenu(bundle, "chilquinta");
                    }
                });
            } else {
                btnChilquinta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "No tiene acceso a los módulos de ésta empresa", Toast.LENGTH_SHORT).show();
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
            } else {
                btnLitoral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "No tiene acceso a los módulos de ésta empresa", Toast.LENGTH_SHORT).show();
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
            } else {
                btnCasablanca.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "No tiene acceso a los módulos de ésta empresa", Toast.LENGTH_SHORT).show();
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
            } else {
                btnLinares.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "No tiene acceso a los módulos de ésta empresa", Toast.LENGTH_SHORT).show();
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
            } else {
                btnParral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "No tiene acceso a los módulos de ésta empresa", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            formNews = new Dialog(EmpActivity.this);
            boolean bAbrir;

            SharedPreferences oPref = getSharedPreferences("GisRedPrefs", Context.MODE_PRIVATE);
            if (!oPref.contains("news")) { bAbrir = true; }
            else {
                bAbrir = oPref.getBoolean("news", true);
            }

            if (bAbrir) {
                abrirFormNews();
                SharedPreferences.Editor editor = oPref.edit();
                editor.putBoolean("news", false);
                editor.apply();
            }

        } catch (Exception e) {
            e.printStackTrace();
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
            case R.id.action_news:
                abrirFormNews();
                return true;
            case R.id.action_logout:
                DialogoConfirmacion dialogo = new DialogoConfirmacion();
                dialogo.show(getFragmentManager(), "tagAlerta");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void abrirFormNews() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthSize = displayMetrics.widthPixels;
        int widthScale = (widthSize * 3) / 4;

        if (Build.VERSION.SDK_INT < 21) {
            v = inflater.inflate(R.layout.form_news, null);
            ImageView imgAbout1 = (ImageButton) v.findViewById(R.id.imgAbout1);
            imgAbout1.setImageResource(R.drawable.img_about_old);
        } else {
            v = inflater.inflate(R.layout.form_news_card, null);
            ImageView imgAbout1 = (ImageButton) v.findViewById(R.id.imgAbout1);
            ImageView imgAbout2 = (ImageButton) v.findViewById(R.id.imgAbout2);

            if (widthSize <= 540){
                imgAbout1.setImageResource(R.drawable.img_nov_sma);
                imgAbout2.setImageResource(R.drawable.img_waz_sma);
            } else if (widthSize <= 720){
                imgAbout1.setImageResource(R.drawable.img_nov_med);
                imgAbout2.setImageResource(R.drawable.img_waz_med);
            } else if (widthSize > 720){
                imgAbout1.setImageResource(R.drawable.img_nov_lar);
                imgAbout2.setImageResource(R.drawable.img_waz_lar);
            }
        }

        v.setMinimumWidth(widthScale);

        if (Build.VERSION.SDK_INT < 21) formNews.setTitle("NOVEDADES GISRED");
        formNews.setContentView(v);

        FloatingActionButton fabClose = (FloatingActionButton) v.findViewById(R.id.actionClose);
        fabClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formNews.hide();
            }
        });

        formNews.show();
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

    private class StatisticsTask extends AsyncTask<String, Void, FeatureResult> {

        FeatureResult featureEncuesta;
        FeatureResult featureReemplazo;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(EmpActivity.this);

            progress = ProgressDialog.show(EmpActivity.this, "",
                    "Cargando preferencias...");
        }

        @Override
        protected FeatureResult doInBackground(String... strings) {
            if (strings == null || strings.length < 1) return null;

            String url = strings[0].concat("/0");
            QueryParameters qParameters = new QueryParameters();
            OutStatistics[] outStatistics = new OutStatistics[]{new OutStatistics(OutStatistics.Type.MAX, "ARCGIS.DBO.ECSE.ano", "anoMax")};
            qParameters.setReturnGeometry(false);
            qParameters.setOutStatistics(outStatistics);

            QueryTask qTask = new QueryTask(url);

            try {
                featureEncuesta = qTask.execute(qParameters);

                url = url.replace("/0", "/1");
                qTask = new QueryTask(url);
                featureReemplazo = qTask.execute(qParameters);

                return featureEncuesta;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(FeatureResult results) {

            for (Object result : results) {
                Map<String, Object> recordAsMap = (Map<String, Object>) result;
                double maxAno = (Double) (recordAsMap.get("maxAno"));
            }

            progress.dismiss();
        }
    }

}
