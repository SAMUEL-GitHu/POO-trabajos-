package pe.edu.upeu.sysventas.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Convierte un monto en soles a letras, como va en el comprobante
 * ("SON: SETENTA CON 00/100 SOLES").
 *
 * <p>Cubre de 0 a 999 999 999.99, que sobra para una venta de la tienda.
 * Si el monto sale de ese rango, devuelve el numero tal cual.</p>
 */
public final class NumeroLetras {

    private static final String[] UNIDADES = {
        "", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE",
        "OCHO", "NUEVE", "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE"};
    private static final String[] DECENAS = {
        "", "", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA",
        "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
    private static final String[] CENTENAS = {
        "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS",
        "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS",
        "NOVECIENTOS"};

    private NumeroLetras() {
    }

    /** Ej: 70.00 -> "SETENTA CON 00/100 SOLES". */
    public static String convertir(BigDecimal total) {
        if (total == null) return "";
        BigDecimal monto = total.setScale(2, RoundingMode.HALF_UP);
        long enteros = monto.longValue();
        int centavos = monto.remainder(BigDecimal.ONE)
                .movePointRight(2).intValue();
        if (enteros < 0 || enteros > 999_999_999L) {
            return monto.toPlainString() + " SOLES";
        }
        String letras = enteros == 0 ? "CERO" : convertirEntero(enteros);
        return letras + " CON " + String.format("%02d", centavos) + "/100 SOLES";
    }

    private static String convertirEntero(long n) {
        if (n == 0) return "";
        if (n < 16) return UNIDADES[(int) n];
        if (n < 20) return "DIECI" + UNIDADES[(int) n - 10];
        if (n == 20) return "VEINTE";
        if (n < 30) return "VEINTI" + UNIDADES[(int) n - 20];
        if (n < 100) {
            long d = n / 10, r = n % 10;
            return DECENAS[(int) d] + (r == 0 ? "" : " Y " + UNIDADES[(int) r]);
        }
        if (n == 100) return "CIEN";
        if (n < 1000) {
            long c = n / 100, r = n % 100;
            return CENTENAS[(int) c] + (r == 0 ? "" : " " + convertirEntero(r));
        }
        if (n < 1_000_000) {
            long miles = n / 1000, r = n % 1000;
            String m = miles == 1 ? "MIL" : convertirEntero(miles) + " MIL";
            return m + (r == 0 ? "" : " " + convertirEntero(r));
        }
        long millones = n / 1_000_000, r = n % 1_000_000;
        String m = millones == 1 ? "UN MILLON"
                : convertirEntero(millones) + " MILLONES";
        return m + (r == 0 ? "" : " " + convertirEntero(r));
    }
}
