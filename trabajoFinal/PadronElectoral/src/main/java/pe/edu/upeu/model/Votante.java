package pe.edu.upeu.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registro del padron electoral (Ejercicio 2).
 * Campos pedidos en el enunciado: nombre completo, RFC, seccion
 * electoral y distrito. El folio (idVotante) es el identificador
 * unico de cada votante y se genera automaticamente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Votante {

    private Long idVotante;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El RFC es obligatorio")
    private String rfc;

    @NotBlank(message = "La seccion electoral es obligatoria")
    private String seccionElectoral;

    @NotBlank(message = "El distrito es obligatorio")
    private String distrito;
}
