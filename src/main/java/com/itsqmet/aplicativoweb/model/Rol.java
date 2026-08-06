package com.itsqmet.aplicativoweb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "roles")

public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del rol no puede estar vacío")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Column(unique = true)
    private String nombre;

    @NotBlank(message = "El estado no puede estar vacío")
    @Pattern(regexp = "Activo|Inactivo", message = "El estado debe ser 'Activo' o 'Inactivo'")
    private String estado = "Activo";

    private boolean protegido;

    @Transient
    private long usuarios;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "rol_permisos",
            joinColumns = @JoinColumn(name = "rol_id")
    )
    @Column(name = "permiso")
    private  Set<@NotBlank(message = "El permiso no puede estar vacío") String> permisos = new LinkedHashSet<>();
}
