package cl.gisred.android.entity;

import com.esri.core.geometry.Geometry;

import java.util.Comparator;

/**
 * Created by cramiret on 29-08-2017.
 */
public class InspLectClass {

    private String objectId;
    private String estado;
    private String tipo;
    private int res;
    private boolean leida;
    private String ot;
    private int secuencia;
    private Geometry geo;

    public InspLectClass(String objId, String est, String rev, String sOt){
        objectId = objId;
        setEstado(est);
        setLeida(rev.equals("leida"));
        ot = sOt;
    }

    public InspLectClass(String objId, String est, String rev, String sOt, String typ, int sec, Geometry geometry){
        objectId = objId;
        setEstado(est);
        setTipo(typ);
        setSecuencia(sec);
        setLeida(rev.equals("leida"));
        setGeo(geometry);
        ot = sOt;
    }

    public InspLectClass(String objId, String est, String rev, String sOt, String typ, int sec){
        objectId = objId;
        setEstado(est);
        setTipo(typ);
        setSecuencia(sec);
        setLeida(rev.equals("leida"));
        ot = sOt;
    }

    public static Comparator<InspLectClass> InspSec = new Comparator<InspLectClass>() {

        public int compare(InspLectClass s1, InspLectClass s2) {

            int sec1 = s1.getSecuencia();
            int sec2 = s2.getSecuencia();

	   /*For ascending order*/
            return sec1-sec2;

	   /*For descending order*/
            //rollno2-rollno1;
        }};

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

    public String getOt() {
        return ot;
    }

    public void setOt(String ot) {
        this.ot = ot;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(int secuencia) {
        this.secuencia = secuencia;
    }

    public Geometry getGeo() {
        return geo;
    }

    public void setGeo(Geometry geo) {
        this.geo = geo;
    }
}
