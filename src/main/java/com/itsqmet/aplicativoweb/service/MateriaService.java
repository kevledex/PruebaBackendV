package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.exception.MateriaNoEncontradaException;
import com.itsqmet.aplicativoweb.exception.DatosInvalidosException;
import com.itsqmet.aplicativoweb.model.Materia;
import com.itsqmet.aplicativoweb.repository.MateriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * AJUSTADO: se aplica la regla del Art. 8 del Acuerdo Ministerial: una
 * materia marcada como optativa/adicional nunca puede contar para la
 * promoción, sin importar lo que llegue en el payload de la petición.
 */
@Service
public class MateriaService {

    private final MateriaRepository materiaRepository;

    public MateriaService(MateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }


    //READ-listar todas

    public List<Materia> obtenerTodo() {

        return materiaRepository.findAll();
    }

    //READ- buscar por ID personalizada
    public Materia buscarPorId(Long id) {
        return materiaRepository.findById(id)
        .orElseThrow(() -> new MateriaNoEncontradaException(id));
    }
    //CREATE- crear materia
    public Materia crearMateria(Materia materia) {
        if(materia.getNombre() == null || materia.getNombre().trim().isEmpty()){
            throw  new DatosInvalidosException("El nombre de la materia es obligatorio");
        }
        aplicarReglaDeOptativa(materia);
        return materiaRepository.save(materia);
    }

    //UPDATE- actualizar materia
    public Materia actualizar(Long id, Materia materiaActualizada) {
        return materiaRepository.findById(id).map(materia -> {
            materia.setNombre(materiaActualizada.getNombre());
            materia.setDescripcion(materiaActualizada.getDescripcion());
            materia.setEsOptativaOAdicional(materiaActualizada.isEsOptativaOAdicional());
            materia.setCuentaParaPromocion(materiaActualizada.isCuentaParaPromocion());
            aplicarReglaDeOptativa(materia);
            return materiaRepository.save(materia);
        }).orElseThrow(() -> new MateriaNoEncontradaException(id));
    }

    //DELETE- eliminar materia
    public boolean eliminar(Long id) {
        if (!materiaRepository.existsById(id)) {
            throw  new MateriaNoEncontradaException(id);
        }
        materiaRepository.deleteById(id);
        return true;
    }

    private void aplicarReglaDeOptativa(Materia materia) {
        if (materia.isEsOptativaOAdicional()) {
            materia.setCuentaParaPromocion(false);
        }
    }
}
