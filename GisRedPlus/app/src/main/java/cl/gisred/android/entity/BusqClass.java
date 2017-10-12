package cl.gisred.android.entity;

/**
 * Created by cramiret on 23-08-2017.
 */
public class BusqClass {

    private String serieNis;
    private String marca;
    private String modelo;

    public BusqClass(String nis, String mar, String mod){
        serieNis = nis;
        setMarca(mar);
        modelo = mod;
    }

    public String getSerieNis(){
        return serieNis;
    }

    public String getMarca(){
        return marca;
    }

    public String getModelo(){
        return modelo;
    }

    public String getNis() {
        return serieNis.split("-")[1];
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }
}
