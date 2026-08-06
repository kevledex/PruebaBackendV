package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.model.Mensaje;
import com.itsqmet.aplicativoweb.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MensajeService {

    @Autowired
    private MensajeRepository mensajeRepository;

    public List<Mensaje>obtenerTodos(){
        return mensajeRepository.findAll();
    }

    public Optional<Mensaje> obtenerPorId(Long id){
        return  mensajeRepository.findById(id);
    }
    public Mensaje guardarMensaje (Mensaje mensaje){
        if (mensaje.getFecha()==null){
            mensaje.setFecha(LocalDate.now());
        }
        return mensajeRepository.save(mensaje);
    }
    public void eliminarMensaje(Long id){
        mensajeRepository.deleteById(id);
    }

}
