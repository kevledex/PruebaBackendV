package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeRepository extends JpaRepository<Mensaje,Long> {
}
