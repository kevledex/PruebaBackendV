package com.itsqmet.aplicativoweb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import  lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor

public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @NotNull(message = "La fecha es un campo obligatorio")
    private LocalDate fecha;

    @NotBlank(message = "El campo curso es obligatorio")
    @Size(max =20, message = "El curso debe tener maximo 20 caracteres")
    private String curso;


    @NotBlank(message = "El campo contenido es obligatorio")
    @Column(length = 500)
    @Size(max = 500, message = "el contenido  debe tener maximo 500 caracteres")
    private String contenido;

    @NotBlank(message = "El campo estado es obligatorio")
    @Pattern(regexp="^(Enviado|Leido|Archivado)$",message = "El estado debe ser Enviado,Leido o Archivado")
    private String estado="Enviado";

    @NotNull(message = "El campo remitente es obligatorio")
    @ManyToOne
    @JsonIgnoreProperties({"rol"})
    private Docente remitente;

}
