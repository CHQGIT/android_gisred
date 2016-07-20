package cl.gisred.android.classes;

import android.content.Context;
import android.widget.TextView;

import com.esri.core.geometry.Point;

/**
 * Created by cramiret on 03-06-2016.
 */
public class GisTextView extends TextView {

    private int idObjeto;
    private String tipo;
    private Point point;

    public GisTextView(Context context) {
        super(context);
    }

    public int getIdObjeto() {
        return idObjeto;
    }

    public void setIdObjeto(int idObjeto) {
        this.idObjeto = idObjeto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}
