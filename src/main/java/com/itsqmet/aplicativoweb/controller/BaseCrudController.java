package com.itsqmet.aplicativoweb.controller;
import jakarta.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


public abstract class BaseCrudController <T> {
    private final JpaRepository <T, Long> repository;

    protected BaseCrudController(JpaRepository<T, Long> repository){
        this.repository= repository;
    }
    @GetMapping
    public List<T>lista(){
        return repository.findAll();
    }
    @GetMapping("/{id}")
    public T obtener(@PathVariable Long id){
        return repository.findById(id)
                .orElseThrow(()-> new NoSuchElementException("Registro no encontrado"));
    }
    @PostMapping
    public ResponseEntity<T> crear(@Valid @RequestBody T body){
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(body));

    }
    @PutMapping("/{id}")
    public T actualizar (
            @PathVariable Long id,
            @Valid @RequestBody T body){
        T actual= obtener(id);
        BeanUtils.copyProperties(body, actual,"id");
        return repository.save(actual);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity <Void> eliminar(@PathVariable Long id){
        if(!repository.existsById(id)){
            throw new NoSuchElementException("Registro no encontradp");

        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
