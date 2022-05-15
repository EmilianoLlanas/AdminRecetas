package com.springboot.webflux.app.controllers;

import java.net.URI;
import java.util.*;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.springboot.webflux.app.models.dao.IngredienteDao;
import com.springboot.webflux.app.models.dao.RacionDao;
import com.springboot.webflux.app.models.documents.Ingrediente;
import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.documents.Racion;
import com.springboot.webflux.app.models.documents.ValidationError;

import reactor.core.publisher.*;

@RestController
@RequestMapping("/ingrediente-controller/ingredientes")
public class IngredienteController {
	
	Logger log = LoggerFactory.getLogger(IngredienteController.class);
	
	@Autowired
	IngredienteDao ingredienteDao;
	
	@Autowired
	RacionDao racionDao;

	@GetMapping
	public Flux<Ingrediente> listar() {
		return ingredienteDao.findAll();
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Ingrediente>> consultar(@PathVariable String id) {
		return ingredienteDao.findById(id)
				          .map(ingrediente -> {
				        	  return ResponseEntity.ok()
				        			  .contentType(MediaType.APPLICATION_JSON)
				        	          .body(ingrediente);
				          })
						  .defaultIfEmpty(
							  ResponseEntity.notFound().build()
						  );
	}
	
	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Ingrediente> monoIngrediente) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return monoIngrediente
				  .flatMap(ingrediente-> {
					return ingredienteDao.findAll()
                                  .filter(ingredient -> ingredient.getNombre().equals(ingrediente.getNombre()))
                                  .single(ingrediente)
                                  .flatMap(ing -> {
                                	if (null == ing.getId())  {				                                  
									  return ingredienteDao.save(ingrediente);
                                	}
                                	return Mono.just(ing);
                                  })
			                     .map(ing -> {
			                    	    respuesta.put("ingrediente", ing);
										respuesta.put("mensaje", "ingrediente creado con éxito");
										respuesta.put("timestamp", new Date());
				                 
										return ResponseEntity
						                 	     .created(URI.create("/ingrediente-controller/ingredientes/"+ing.getId()))
						                	     .contentType(MediaType.APPLICATION_JSON)
						                	     .body(respuesta);
				                   });
				  })
				  .onErrorResume(ex -> {
					  return Mono.just(ex)
							  .cast(WebExchangeBindException.class)
							  .flatMapMany(e-> Flux.fromIterable(e.getFieldErrors()))
							  .map(fieldError-> new ValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
							  .collectList()
						      .flatMap(list -> {
						    	    respuesta.put("errors", list);
									respuesta.put("mensaje", "error al crear el ingrediente");
									respuesta.put("timestamp", new Date());
									
									return Mono.just(ResponseEntity
											.badRequest()
											.body(respuesta));
						     });
				  });
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Map<String, Object>>> eliminar(@PathVariable String id) {
		Map<String, Object> respuesta = new HashMap<String, Object>();

		
		return buscaIngrediente(id)
				.flatMap(enUso->{
					if (enUso) {
						log.info("Error exception: utilizado");
						respuesta.put("mensaje", "Ingrediente en uso, no se puede eliminar"); 
						  return Mono.just(
								  ResponseEntity
								  .status(HttpStatus.BAD_REQUEST)
								  .contentType(MediaType.APPLICATION_JSON)
								  .body(respuesta));
					}
					
					return ingredienteDao
							 .findById(id)
							 .flatMap(ing -> {
								 log.info("Eliminar ingrediente:"+ing);
								  return ingredienteDao.delete(ing).then(Mono.just("ingrediente eliminado con éxito"));
							  })
							 .defaultIfEmpty("")
							 .map(msj -> {
								  
								  if (msj.equals("")) {
									  respuesta.put("mensaje", "Ingrediente con id:"+ id +" no localizado"); 
									  return ResponseEntity
											  .status(HttpStatus.NOT_FOUND)
											  .contentType(MediaType.APPLICATION_JSON)
											  .body(respuesta);
								  }
							  
								  respuesta.put("mensaje", msj);
					        
								  return ResponseEntity
						                	 .ok()
						               	     .contentType(MediaType.APPLICATION_JSON)
						               	     .body(respuesta);
					         });
				});
	}
	
	public Mono<Boolean> buscaIngrediente(String id) {
		return racionDao
				.findAll()
				.filter(racion-> {
					log.info("Filter racion:"+racion.getId());
					log.info("Check racion:"+racion.getIngrediente().getId().equals(id));
				 return racion.getIngrediente().getId().equals(id);
				})
				.hasElements();	
	}
}
