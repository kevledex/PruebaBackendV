package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.model.Representante;
import com.itsqmet.aplicativoweb.repository.RepresentanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RepresentanteService {
    @Autowired
    private RepresentanteRepository representanteRepository;

    public List<Representante> obtenerTodos() {
        return representanteRepository.findAll();
    }

    public Optional<Representante> obtenerPorId(Long id) {
        return representanteRepository.findById(id);
    }

    public Representante guardarRepresentante(Representante representante) {
        return representanteRepository.save(representante);
    }

    public void eliminarRepresentante(Long id) {
        representanteRepository.deleteById(id);
    }
}