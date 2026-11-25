package pe.edu.upeu.sysventas.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para guardar claves con BCrypt (Sesion 12, rubrica 6:
 * investigacion adicional no realizada en clases).
 *
 * <p>BCrypt guarda en un solo texto el algoritmo, el costo, la sal y el
 * hash, por ejemplo:</p>
 * <pre>$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy</pre>
 * <p>Por eso en la base de datos la columna {@code usuario.clave} es
 * VARCHAR(255): el hash siempre ocupa 60 caracteres, pero queda margen.</p>
 *
 * <p>Reglas que se aplican en {@code UsuarioServiceImp}:</p>
 * <ul>
 *   <li>al crear o cambiar un usuario, la clave se guarda con
 *       {@link #hash(String)}; nunca se guarda en claro;</li>
 *   <li>al iniciar sesion se verifica con {@link #verificar(String, String)},
 *       nunca comparando textos en el SQL;</li>
 *   <li>si un usuario viejo todavia tiene la clave en claro (base de datos
 *       de sesiones anteriores), el primer inicio de sesion correcto la
 *       migra a BCrypt automaticamente.</li>
 * </ul>
 */
public final class PasswordUtil {

    /** Costo de BCrypt (2^10 rondas). Es el valor por defecto de la libreria. */
    private static final int COSTO = 10;

    private PasswordUtil() {
    }

    /** Genera el hash BCrypt de una clave en claro. Cada llamada usa una sal distinta. */
    public static String hash(String claveEnClaro) {
        if (claveEnClaro == null || claveEnClaro.isBlank()) {
            throw new IllegalArgumentException("La clave no puede estar vacia");
        }
        return BCrypt.hashpw(claveEnClaro, BCrypt.gensalt(COSTO));
    }

    /** Comprueba si una clave en claro corresponde a un hash BCrypt guardado. */
    public static boolean verificar(String claveEnClaro, String hashGuardado) {
        if (claveEnClaro == null || hashGuardado == null || hashGuardado.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(claveEnClaro, hashGuardado);
        } catch (IllegalArgumentException e) {
            // Hash con formato invalido: no es un hash BCrypt valido.
            return false;
        }
    }

    /** True si el valor guardado ya es un hash BCrypt ($2a$...). */
    public static boolean esHashBCrypt(String valor) {
        return valor != null && valor.startsWith("$2a$");
    }
}
