package com.itsqmet.aplicativoweb.service;
import com.itsqmet.aplicativoweb.dto.AsistenciaDtos.ItemAsistencia;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.Alumno;
import com.itsqmet.aplicativoweb.model.Asistencia;
import com.itsqmet.aplicativoweb.model.Materia;
import com.itsqmet.aplicativoweb.repository.AlumnoRepository;
import com.itsqmet.aplicativoweb.repository.AsistenciaRepository;
import com.itsqmet.aplicativoweb.repository.MateriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    public List<Asistencia> obtenerTodas(){
        return asistenciaRepository.findAll();
    }

    public List<Asistencia> listarPorFechaYMateria(LocalDate fecha, Long materiaId){
        return asistenciaRepository.findByFechaAndMateriaId(fecha, materiaId);
    }

    //Buscar por id
    public Optional <Asistencia> obtenerPorId(Long id){
        return asistenciaRepository.findById(id);
    }

    //Registrar asistencia
    public Asistencia guardarAsistencia(Asistencia asistencia ){
        return asistenciaRepository.save(asistencia);
    }

    //Eliminar asistencia
    public void eliminarAsistencia (Long id){
        asistenciaRepository.deleteById(id);
    }

    /**
     * Registra o actualiza (upsert, según el único índice
     * alumno_id+fecha+materia_id) la asistencia de todo un curso en una sola
     * operación, tal como la toma un docente al inicio de la clase.
     */
    public List<Asistencia> guardarLote(LocalDate fecha, Long materiaId, List<ItemAsistencia> estudiantes){
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia", materiaId));

        return estudiantes.stream().map(item -> {
            Alumno alumno = alumnoRepository.findById(item.alumnoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Alumno", item.alumnoId()));

            Asistencia asistencia = asistenciaRepository
                    .findByAlumnoIdAndFechaAndMateriaId(alumno.getId(), fecha, materiaId)
                    .orElseGet(Asistencia::new);

            asistencia.setAlumno(alumno);
            asistencia.setMateria(materia);
            asistencia.setFecha(fecha);
            asistencia.setEstado(item.estado());
            asistencia.setObservacion(item.observacion());

            return asistenciaRepository.save(asistencia);
        }).toList();
    }

}