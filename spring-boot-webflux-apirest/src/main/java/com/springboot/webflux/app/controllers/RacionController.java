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

import com.springboot.webflux.app.models.dao.RacionDao;
import com.springboot.webflux.app.models.dao.RecetaDao;
import com.springboot.webflux.app.models.documents.Racion;
import com.springboot.webflux.app.models.documents.ValidationError;

import reactor.core.publisher.*;

@RestController
@RequestMapping("/racion-controller/raciones")
public class RacionController {
	Logger log = LoggerFactory.getLogger(RacionController.class);
	
	@Autowired
	RacionDao racionDao;
	
	@Autowired
	RecetaDao recetaDao;

	@GetMapping
	public Flux<Racion> listar() {
		return racionDao.findAll();
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Racion>> consultar(@PathVariable String id) {
		return racionDao.findById(id)
				          .map(racion -> {
				        	  return ResponseEntity.ok()
				        			  .contentType(MediaType.APPLICATION_JSON)
				        	          .body(racion);
				          })
						  .defaultIfEmpty(
							  ResponseEntity.notFound().build()
						  );
	}
	
	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Racion> monoRacion) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return monoRacion
				  .flatMap(racion-> {
					return racionDao.findAll()
                                  .filter(rac -> rac.equals(racion))
                                  .single(racion)
                                  .flatMap(ing -> {
                                	if (null == ing.getId())  {				                                  
									  return racionDao.save(racion);
                                	}
                                	return Mono.just(ing);
                                  })
			                     .map(ing -> {
			                    	    respuesta.put("racion", ing);
										respuesta.put("mensaje", "racion creado con éxito");
										respuesta.put("timestamp", new Date());
				                 
										return ResponseEntity
						                 	     .created(URI.create("/racion-controller/racions/"+ing.getId()))
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
									respuesta.put("mensaje", "error al crear el racion");
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
		
		return buscaRacion(id)
				.flatMap(enUso-> {
					if (enUso) {
						respuesta.put("mensaje", "Ración en uso, no se puede eliminar"); 
						  return Mono.just(
								  ResponseEntity
								  .status(HttpStatus.BAD_REQUEST)
								  .contentType(MediaType.APPLICATION_JSON)
								  .body(respuesta));
					}
				
				return racionDao
						 .findById(id)
						 .flatMap(rac -> {
							  return racionDao.delete(rac).then(Mono.just(new Racion().setId(id)));
						  })
						 .defaultIfEmpty(new Racion())
						 .map(ing -> {
							  respuesta.put("timestamp", new Date());
							  
							  if (null == ing.getId()) {
								  respuesta.put("mensaje", "Racion con id:"+ id +" no localizado"); 
								  return ResponseEntity
										  .status(HttpStatus.NOT_FOUND)
										  .contentType(MediaType.APPLICATION_JSON)
										  .body(respuesta);
							  }
						  
							  respuesta.put("racion", ing);
							  respuesta.put("mensaje", "racion eliminada con éxito");
		                 
							  return ResponseEntity
				                 	     .ok()
				                	     .contentType(MediaType.APPLICATION_JSON)
				                	     .body(respuesta);
				          });
				});
	}
	
	@GetMapping("/buscar/{id}")
	public Mono<Boolean> buscaRacion(@PathVariable String id) {
		return recetaDao
				.findAll()
				.filter(receta-> {
					log.info("Filter receta:"+receta.getRaciones());
					
					Mono<Boolean> exists =  Flux.fromIterable(receta.getRaciones())
									            .filter(racion-> racion.getId().equals(id))
									            .hasElements();
					
					log.info("Check receta:"+exists);
					
				 return exists.block();
				})
				.hasElements();	
	}
}
