package pe.edu.upeu.config;

import pe.edu.upeu.controller.VotanteController;
import pe.edu.upeu.repository.VotanteRepository;
import pe.edu.upeu.servise.IVotanteService;
import pe.edu.upeu.servise.impl.VotanteServiceImp;

import java.util.HashMap;
import java.util.Map;

public class AppContext {

    // Singleton: una sola instancia en toda la app
    private static AppContext instance;

    public static synchronized AppContext getInstance() {
        if (instance == null) instance = new AppContext();
        return instance;
    }

    // El "directorio": Clase -> Objeto
    private final Map<Class<?>, Object> contenedor = new HashMap<>();

    // Constructor privado: aqui se arma toda la aplicacion
    private AppContext() {
        registrarRepositorios();
        registrarServicios();
        registrarControladores();
    }

    // CAPA 1 - REPOSITORIOS
    private void registrarRepositorios() {
        registrar(VotanteRepository.class, new VotanteRepository());
    }

    // CAPA 2 - SERVICIOS
    private void registrarServicios() {
        registrar(IVotanteService.class, new VotanteServiceImp(getBean(VotanteRepository.class)));
    }

    // CAPA 3 - CONTROLADORES JavaFX
    private void registrarControladores() {
        registrar(VotanteController.class, new VotanteController(getBean(IVotanteService.class)));
    }

    // API del contenedor
    private void registrar(Class<?> tipo, Object bean) {
        contenedor.put(tipo, bean);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> tipo) {
        Object bean = contenedor.get(tipo);
        if (bean == null) {
            bean = contenedor.values().stream()
                    .filter(b -> tipo.isAssignableFrom(b.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Bean no encontrado: " + tipo.getName() +
                                    "\n-> ¿Lo registraste en AppContext?"));
        }
        return (T) bean;
    }
}
