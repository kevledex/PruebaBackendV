package com.itsqmet.aplicativoweb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor


/**
 * AJUSTADO: se agregan los campos exigidos por el Art. 8 del Acuerdo
 * Ministerial ("Las asignaturas optativas, adicionales... se evaluarán de
 * forma cualitativa y no serán un requisito de promoción de los
 * estudiantes"): esOptativaOAdicional y cuentaParaPromocion (MateriaService
 * fuerza cuentaParaPromocion=false cuando esOptativaOAdicional=true).
 *
 * La relación "notas" (OneToMany hacia Nota) se elimina: Nota ya no
 * referencia Materia directamente, sino a través de Actividad -> MateriaCurso
 * (ver ajustes a Actividad/Nota). En su lugar se expone "ofertas", la lista
 * de MateriaCurso (oferta real de esta materia en cada curso/año lectivo,
 * con su docente y periodos pedagógicos semanales -- Tabla 35 del
 * Instructivo).
 */

public class Materia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @NotBlank(message = "El nombre de la materia no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre de la materia debe tener entre 2 y 50 carácteres")
    private String nombre;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(min = 5, max = 200, message = "La descripcion debe tener entre 5 y 200 carácteres")
    private String descripcion;

    private boolean esOptativaOAdicional;

    private boolean cuentaParaPromocion = true;

    /**
     * CORREGIDO: sin "@JsonIgnore" esto causaba recursión infinita al
     * serializar -- Materia -> ofertas -> MateriaCurso -> materia (la misma
     * Materia) -> ofertas -> ... -- que terminaba en StackOverflowError en
     * cuanto existía al menos una oferta (MateriaCurso) para la materia,
     * rompiendo cualquier endpoint que devolviera Materia o MateriaCurso
     * (incluido "/api/materias" y "/api/cursos/{id}/materias").
     */
    @OneToMany(mappedBy = "materia")
    @JsonIgnore
    private List<MateriaCurso> ofertas;

}
