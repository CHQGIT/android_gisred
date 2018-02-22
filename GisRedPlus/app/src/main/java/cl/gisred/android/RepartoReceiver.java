package cl.gisred.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class RepartoReceiver extends BroadcastReceiver {
    public RepartoReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String sPackage = context.getPackageName();
        String sIntent = intent.getDataString();

        Toast.makeText(context, "Data: " + sPackage + " - " + sIntent, Toast.LENGTH_LONG).show();
    }
}
