package pe.edu.upeu.sysventas.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Talla de una prenda (tabla {@code talla}).
 * Ejemplos: XS, S, M, L, XL, XXL y numericas como 28, 30, 32.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Talla {
    private Long idTalla;

    @NotBlank(message = "El nombre de la talla es obligatorio")
    @Size(max = 20, message = "La talla no puede tener mas de 20 caracteres")
    private String nombre;
}
