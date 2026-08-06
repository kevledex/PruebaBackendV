package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.HabilidadSocioemocional;
import com.itsqmet.aplicativoweb.repository.HabilidadSocioemocionalRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * NUEVO. CRUD estándar del catálogo de habilidades socioemocionales
 * priorizadas (Anexo 2 del Instructivo), igual patrón que
 * DocenteController/RolController (extiende BaseCrudController).
 */
@RestController
@RequestMapping("/api/habilidades-socioemocionales")
public class HabilidadSocioemocionalController extends BaseCrudController<HabilidadSocioemocional> {

    public HabilidadSocioemocionalController(HabilidadSocioemocionalRepository repository) {
        super(repository);
    }
}
