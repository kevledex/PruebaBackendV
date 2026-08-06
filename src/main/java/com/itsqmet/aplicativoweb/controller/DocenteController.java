package com.itsqmet.aplicativoweb.controller;
import com.itsqmet.aplicativoweb.dto.DocenteDtos.AsignacionMaterias;
import com.itsqmet.aplicativoweb.model.Docente;
import com.itsqmet.aplicativoweb.model.Materia;
import com.itsqmet.aplicativoweb.repository.DocenteRepository;
import com.itsqmet.aplicativoweb.service.DocenteService;
import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/api/docentes")
public class DocenteController   extends BaseCrudController<Docente>{
    private final DocenteService docenteService;

    public DocenteController(
            DocenteRepository docentes,
            DocenteService docenteService) {
        super(docentes);
        this.docenteService = docenteService;
    }

        @PutMapping("/{id}/materias")
        public List<Materia> asignarMaterias(
                @PathVariable Long id,
                @RequestBody AsignacionMaterias solicitud){
            return docenteService.asignarMaterias(id, solicitud);


        }

    }