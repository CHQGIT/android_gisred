package cl.gisred.android.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.esri.android.action.IdentifyResultSpinnerAdapter;
import com.esri.core.tasks.identify.IdentifyResult;

import java.util.List;

/**
 * Created by cramiret on 18-05-2016.
 */
public class MapIdentifyAdapter implements SpinnerAdapter {
    String m_show = null;
    List<String> resultList;
    int currentDataViewed = -1;
    Context m_context;

    public MapIdentifyAdapter(Context context, List<String> results) {
        this.resultList = results;
        this.m_context = context;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StringBuilder outputVal = new StringBuilder();

        // Get Name attribute from identify results
        String curResult = this.resultList.get(position);
        outputVal.append("Address: " + curResult);


        // Create a TextView to write identify results
        TextView txtView;
        txtView = new TextView(this.m_context);
        txtView.setText(outputVal);
        txtView.setTextColor(Color.BLACK);
        txtView.setLayoutParams(new ListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        txtView.setGravity(Gravity.CENTER_VERTICAL);

        return txtView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
