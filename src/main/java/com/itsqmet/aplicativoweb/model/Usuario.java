package com.itsqmet.aplicativoweb.model;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 20, message = "El usuario debe tener entre 4 y 20 caracteres")
    @Column(unique = true, length = 20)
    private String usuario;

    @Pattern(regexp = "\\d{10}", message = "La cedula solo debe tener numeros")
    @Size(min = 10, max = 10, message = "La cedula debe tener 10 digitos")
    @Column(unique = true, length = 10)
    private String cedula;

    @NotBlank(message = "El campo contraseña es obligatorio")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonAlias("password")
    private String contrasenia;

    @NotBlank (message = "El campo estado es obligatorio")
    @Pattern(
            regexp = "^(Activo|Inactivo)$",
            message = "El estado solo puede ser Activo o Inactivo")
    @Column(nullable = false)
    private String estado = "Activo";

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Rol rol;
}
