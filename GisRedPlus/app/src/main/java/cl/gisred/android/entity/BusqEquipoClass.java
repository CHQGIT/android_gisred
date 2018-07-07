package cl.gisred.android.entity;

/**
 * Created by cramiret on 25-03-2018.
 */
public class BusqEquipoClass {

    private String idEquipo;
    private String nombre;
    private String alimentador;
    private String tipo;

    public BusqEquipoClass(String id, String nom, String alim){
        idEquipo = id;
        setNombre(nom);
        alimentador = alim;
    }

    public BusqEquipoClass(String id, String nom, String alim, String typ){
        idEquipo = id;
        setNombre(nom);
        alimentador = alim;
        tipo = typ;
    }

    public String getIdEquipo(){
        return idEquipo;
    }

    public String getNombre(){
        return nombre;
    }

    public String getAlimentador(){
        return alimentador;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }
}
