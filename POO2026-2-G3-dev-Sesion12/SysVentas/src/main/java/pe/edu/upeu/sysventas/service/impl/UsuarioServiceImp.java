package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.UsuarioRepository;
import pe.edu.upeu.sysventas.service.IUsuarioService;
import pe.edu.upeu.sysventas.utils.PasswordUtil;

import java.util.Optional;

/**
 * Servicio de usuarios con claves encriptadas con BCrypt (Sesion 12,
 * rubrica 6: investigacion adicional no realizada en clases).
 *
 * <p>Antes el login comparaba la clave en claro dentro del SQL
 * ({@code WHERE usuario = ? AND clave = ?}) y las claves se guardaban
 * tal cual se escribian. Ahora:</p>
 * <ul>
 *   <li>al guardar o actualizar un usuario, la clave se guarda con hash
 *       BCrypt (ver {@link PasswordUtil});</li>
 *   <li>al iniciar sesion se busca solo por usuario y se verifica el hash
 *       con {@code BCrypt.checkpw}, nunca se compara la clave en el SQL;</li>
 *   <li>si un usuario viejo todavia tiene la clave en claro (base de datos
 *       de sesiones anteriores), el primer inicio de sesion correcto la
 *       migra a BCrypt sin que el usuario tenga que hacer nada.</li>
 * </ul>
 */
public class UsuarioServiceImp extends CrudGenericoServiceImp<Usuario, Long>
        implements IUsuarioService {

    private final UsuarioRepository repo;

    public UsuarioServiceImp(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    protected ICrudGenericoRepository<Usuario, Long> getRepo() {
        return repo;
    }

    @Override
    public Usuario save(Usuario usuario) {
        usuario.setClave(hashSiHaceFalta(usuario.getClave()));
        return super.save(usuario);
    }

    @Override
    public Usuario update(Long id, Usuario usuario) {
        usuario.setClave(hashSiHaceFalta(usuario.getClave()));
        return super.update(id, usuario);
    }

    @Override
    public Optional<Usuario> loginUsuario(String usuario, String clave) {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            return Optional.empty();
        }
        Optional<Usuario> encontrado = repo.buscarUsuario(usuario.trim());
        if (encontrado.isEmpty()) {
            return Optional.empty();
        }
        Usuario user = encontrado.get();
        String guardada = user.getClave();
        if (PasswordUtil.esHashBCrypt(guardada)) {
            return PasswordUtil.verificar(clave, guardada)
                    ? Optional.of(user) : Optional.empty();
        }
        // Migracion: clave vieja en claro. Si coincide, se guarda el hash.
        if (clave.equals(guardada)) {
            user.setClave(PasswordUtil.hash(clave));
            repo.update(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /** Si la clave todavia no es un hash BCrypt, la convierte en hash. */
    private String hashSiHaceFalta(String clave) {
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("La clave no puede estar vacia");
        }
        return PasswordUtil.esHashBCrypt(clave) ? clave : PasswordUtil.hash(clave);
    }
}
