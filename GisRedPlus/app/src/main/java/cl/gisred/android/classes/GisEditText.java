package cl.gisred.android.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.esri.core.geometry.Point;

/**
 * Created by cramiret on 06-06-2016.
 */
public class GisEditText extends EditText {

    private int idObjeto;
    private String tipo;
    private Point point;

    public GisEditText(Context context) {
        super(context);
    }

    public GisEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
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
