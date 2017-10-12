package cl.gisred.android.entity;

/**
 * Created by cramiret on 29-08-2017.
 */
public class InspLectClass {

    private String objectId;
    private String estado;
    private int res;
    private boolean leida;

    public InspLectClass(String objId, String est, String rev){
        objectId = objId;
        setEstado(est);
        setLeida(rev.equals("leida"));
    }

    public String getObjectId(){
        return objectId;
    }

    public String getEstado(){
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }
}
