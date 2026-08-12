package com.itsqmet.aplicativoweb.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * CORREGIDO: la anotación "@lombok" no existe (el paquete lombok no expone
 * ningún marcador con ese nombre); se reemplaza por "@Data" para que la clase
 * compile y genere getters/setters/equals/hashCode. También se elimina el
 * import "org.aspectj.bridge.IMessage" (no usado y no resuelto salvo que el
 * proyecto agregue la dependencia de AspectJ, lo cual rompía la compilación).
 */
@Entity
@Data
@NoArgsConstructor
public class Docente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank (message = "El campo cedula es obligatorio")
    @Size(min = 10, max = 10, message = "La cedula debe tener 10 digitos")
    @Pattern(regexp="\\d{10}", message = "La cedula solo debe tener numeros")
    @Column(unique = true, length = 10, nullable = false)
    private String cedula;

    @NotBlank  (message = "El campo nombres es obligatorio")
    @Size(min = 2, max = 20, message = "El nombre debe tener entre 2 a 20 caracteres")
    private String nombres;
    @NotBlank(message = "El campo apellidos es obligatorio")
    @Size(min = 2, max = 20, message = "El apellido debe tener 2 a 20 caracteres")
    private String apellidos;

    @NotNull(message = "El campo fecha de nacimienyo es obligatorio")
    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El campo especialidad es ogligatorio")
    @Size(min = 2, max = 30, message = "La especialidad debe tener 2 a 20 caracteres")
    private String especialidad;

    @NotBlank(message = "El campo titulo es ogligatorio")
    @Size(min = 2, max = 30, message = "El titulo debe tener 2 a 20 caracteres")
    private String titulo;

    @NotBlank(message = "El campo telefono es obligatorio")
    @Pattern(regexp = "^09\\d{8}$",
            message = "El teléfono debe tener 10 dígitos y comenzar con 09")
    @Column(length = 10)

    private String telefono;

    @Email(message = "Ingrese un correo electronico valido")
    @NotBlank(message = "El campo correo es obligatorio")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    @Column(unique = true, nullable = false)
    private String correo;

    @NotNull(message = "Debe seleccionar un rol")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"permisos"})
    private Rol rol;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    @JsonIgnoreProperties({"rol"})
    private Usuario usuario;

}
