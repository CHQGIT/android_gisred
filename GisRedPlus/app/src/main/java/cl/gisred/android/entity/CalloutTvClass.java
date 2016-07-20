package cl.gisred.android.entity;

/**
 * Created by cramiret on 23-05-2016.
 */
public class CalloutTvClass {

    private String vista;
    private String valor;
    private int idObjeto;
    private String tipo;

    public CalloutTvClass(String view, String value) {
        vista = view;
        valor = value;
    }

    public CalloutTvClass(String view, String value, int idObj) {
        vista = view;
        valor = value;
        idObjeto = idObj;
    }

    public CalloutTvClass(String view, String value, int idObject, String type) {
        vista = view;
        valor = value;
        idObjeto = idObject;
        tipo = type;
    }

    public String getVista() {
        return vista;
    }

    public String getValor() {
        return valor;
    }

    public int getIdObjeto() {
        return idObjeto;
    }

    public String getTipo() {
        return tipo;
    }
}
