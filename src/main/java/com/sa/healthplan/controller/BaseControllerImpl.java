package com.sa.healthplan.controller;

import com.sa.healthplan.model.Base;

import com.sa.healthplan.service.BaseServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public abstract class BaseControllerImpl<E extends Base, S extends BaseServiceImpl<E, Long>> implements BaseController<E, Long> {

    @Autowired
    protected S service;

    @Operation(summary = "Devuelve todas las entidades")
    @GetMapping("")
    @Override
    public ResponseEntity<?> getAll() {
        try {

            List<E> listHp = service.findAll();

            if (listHp.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            // Implemnetando HATEOAS
            for (E hp : listHp) {
                hp.add(linkTo(methodOn(HealthPlanController.class).getOne(hp.getId())).withSelfRel());
                hp.add(linkTo(methodOn(HealthPlanController.class).getAll()).withRel(IanaLinkRelations.COLLECTION));
            }

            CollectionModel<E> model = CollectionModel.of(listHp);
            model.add(linkTo(methodOn(HealthPlanController.class).getAll()).withSelfRel());

            return new ResponseEntity<>(model, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ERROR_404);
        }

    }

    @Operation(summary = "Devuelve entidades por ID")
    @GetMapping("{id}")
    @Override
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {

            E hp = service.findById(id);

            // Implementando HATEOAS
            hp.add(linkTo(methodOn(HealthPlanController.class).getOne(hp.getId())).withSelfRel());
            hp.add(linkTo(methodOn(HealthPlanController.class).getAll()).withRel(IanaLinkRelations.COLLECTION));

            return new ResponseEntity<>(hp, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ERROR_404);
        }
    }

    @Operation(summary = "Crea nuevas entidades")
    @PostMapping("")
    @Override
    public ResponseEntity<?> save(@RequestBody E entity) {
        try {

            E saved = service.save(entity);

            saved.add(linkTo(methodOn(HealthPlanController.class).getOne(saved.getId())).withSelfRel());
            saved.add(linkTo(methodOn(HealthPlanController.class).getAll()).withRel(IanaLinkRelations.COLLECTION));

            return ResponseEntity.created(
                    linkTo(methodOn(HealthPlanController.class).getOne(saved.getId()))
                            .toUri())
                    .body(saved);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_404);
        }

    }

    @Operation(summary = "Modifica una entidad por ID")
    @PutMapping("{id}")

    @Override
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody E entity) {
        try {

            /* Le seteo el ID porque de lo contrario se lo tenia que pasar 
               si o si por el body para que no me cree otra entidad en lugar
               de modificar la que le paso por la URI
             */
            entity.setId(id);
            service.update(id, entity);

            entity.add(linkTo(methodOn(HealthPlanController.class).getOne(entity.getId())).withSelfRel());
            entity.add(linkTo(methodOn(HealthPlanController.class).getAll()).withRel(IanaLinkRelations.COLLECTION));

            return new ResponseEntity<>(entity, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_404);
        }
    }

    @Operation(summary = "Elimina una entidad por ID")
    @DeleteMapping("{id}")

    @Override
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.delete(id));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_404);
        }
    }

    @Operation(summary = "Devuelve las entidades paginadas")
    @GetMapping("/paged")
    @Override
    public ResponseEntity<?> getAll(Pageable pageable) {
        try {

            return ResponseEntity.status(HttpStatus.OK).body(service.findAll(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ERROR_404);
        }
    }

}
