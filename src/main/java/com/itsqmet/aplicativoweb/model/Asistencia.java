package com.itsqmet.aplicativoweb.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * CORREGIDO:
 *  1) "@lombok" no es una anotación válida -> se reemplaza por "@Data".
 *  2) El uniqueConstraint referenciaba la columna "material_id" (typo), que
 *     no coincide con la columna real "materia_id" definida en el
 *     @JoinColumn de más abajo. Con ddl-auto=update esto podía fallar al
 *     generar el esquema. Se corrige a "materia_id".
 */
@Entity
@Data
@NoArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(
        columnNames = {"alumno_id","fecha", "materia_id"}
        ))

public class Asistencia {
    @Id
    @GeneratedValue(strategy  = GenerationType.IDENTITY)
    private Long id;
    @NotNull (message ="El campo fecha es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate fecha;

    @NotBlank (message = "El campo estado es obligatorio")
    @Pattern(
            regexp = "Presente|Atraso|Falta Justificada|Falta Injustificada",
            message = "El estado debe ser: Presente, Atraso, Falta Justificada o Falta Injustificada"
    )
    private String estado;

    @Size(max=200, message= "La observacopm no puede superar las 500 caracteres")
    @Column(length = 500)
    private String  observacion;

    @NotNull(message = "Debe seleccionar un alumno")
    @ManyToOne(optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    @JsonIgnoreProperties({"representanteRegistro"})
    private Alumno alumno;

    @NotNull(message = "Debe selecionar una materia")
    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    @JsonIgnoreProperties({"docente"})
    private Materia materia;

}
