package cl.gisred.android.entity;

/**
 * Created by cramiret on 19-10-2017.
 */
public class RepartoClass {

    private static int length_code = 34;
    private static int length_req = 6;

    private int id;
    private int nis;
    private String codigo;
    private double x;
    private double y;

    public RepartoClass(int iId, String sCodigo, double dX, double dY){
        id = iId;
        nis = getNisByCode(sCodigo);
        codigo = sCodigo;
        x = dX;
        y = dY;
    }

    private static int getNisByCode(String cod) {
        if (cod.length() < length_code)
            return 0;
        else {
            int fx = cod.length() - length_code + length_req;
            String sNis = cod.substring(0, fx);

            if (Integer.valueOf(sNis) != null) {
                return Integer.valueOf(sNis);
            } else return 0;
        }
    }

    public static boolean valCode(String cod) {
        if (cod.length() < length_code)
            return false;
        else {
            int fx = cod.length() - length_code + length_req;
            String sNis = cod.substring(0, fx);

            return Integer.valueOf(sNis) != null && Integer.valueOf(sNis) > 0;
        }
    }

    public int getId() {
        return id;
    }

    public int getNis() {
        return nis;
    }

    public String getCodigo() {
        return codigo;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
