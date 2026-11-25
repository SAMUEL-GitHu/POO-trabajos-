package pe.edu.upeu.sysventas.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Color de una prenda (tabla {@code color}).
 * {@code codigoHex} es opcional y sirve para mostrar el color en la
 * interfaz, por ejemplo "#000000" para negro.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Color {
    private Long idColor;

    @NotBlank(message = "El nombre del color es obligatorio")
    @Size(max = 40, message = "El color no puede tener mas de 40 caracteres")
    private String nombre;

    @Size(max = 7, message = "El codigo hexadecimal tiene como maximo 7 caracteres")
    private String codigoHex;
}
