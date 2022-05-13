package com.springboot.webflux.app.controllers;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.springboot.webflux.app.models.dao.RecetaDao;
import com.springboot.webflux.app.models.documents.Racion;
import com.springboot.webflux.app.models.documents.Receta;
import com.springboot.webflux.app.models.documents.ValidationError;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/receta-controller/recetas")
public class RecetaController {
	
	@Autowired
	RecetaDao recetaDao;

	@GetMapping
	public Flux<Receta> listar(@RequestParam(required=false) Map<String,String> qparams) {
		String nombre = qparams.get("nombre");
		
		Flux<Receta> recetas = recetaDao
								.findAll()
								.filter(receta -> null != nombre 
												 ? receta.getNombre().contains(nombre) 
									  			 : true);
		
		return recetas;
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Receta>> consultar(@PathVariable String id) {
		return recetaDao.findById(id)
				          .map(receta -> {
				        	  return ResponseEntity.ok()
				        			  .contentType(MediaType.APPLICATION_JSON)
				        	          .body(receta);
				          })
						  .defaultIfEmpty(
							  ResponseEntity.notFound().build()
						  );
	}
					     
	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Receta> monoReceta) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return monoReceta
				  .flatMap(receta-> {
					  return recetaDao
							     .save(receta)
			                     .map(prod -> {
			                    	    respuesta.put("receta", prod);
										respuesta.put("mensaje", "receta creada con éxito");
										respuesta.put("timestamp", new Date());
				                 
										return ResponseEntity
						                 	     .created(URI.create("/receta-controller/recetas/"+prod.getId()))
						                	     .contentType(MediaType.APPLICATION_JSON)
						                	     .body(respuesta);
				                   });
				  })
				  .onErrorResume(ex -> {
					  return createError(ex);
				  });
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Map<String, Object>>> editar(@Valid @RequestBody Mono<Receta> monoReceta, @PathVariable String id) {
        Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return monoReceta
				  .flatMap(receta-> {
					  return recetaDao
							  .findById(id)
							  .flatMap(r-> {
									  r.setNombre(receta.getNombre());
									  r.setDificultad(receta.getDificultad());
									  r.setTiempoPreparacion(receta.getTiempoPreparacion());
									  r.setDescripcion(receta.getDescripcion());
									  
									  if (null != receta.getRaciones()) {
										  r.setRaciones(receta.getRaciones());
									  }
									  
									  return recetaDao.save(r);  
							  })
							  .defaultIfEmpty(new Receta())
							  .map(r -> {
								  respuesta.put("timestamp", new Date());
								  
								  if (null == r.getId()) {
									  respuesta.put("mensaje", "receta con id:"+ id +" no localizada"); 
									  return ResponseEntity
											  .status(HttpStatus.NOT_FOUND)
											  .contentType(MediaType.APPLICATION_JSON)
											  .body(respuesta);
								  }
							  
								  respuesta.put("receta", r);
								  respuesta.put("mensaje", "receta actualizada con éxito");
			                 
								  return ResponseEntity
					                 	     .ok()
					                	     .contentType(MediaType.APPLICATION_JSON)
					                	     .body(respuesta);
					          });
				  })
				  .onErrorResume(ex -> {
					  return createError(ex);
				  });
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Map<String, Object>>> eliminar(@PathVariable String id) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return recetaDao
				 .findById(id)
				 .flatMap(r-> {
					  return recetaDao.delete(r).then(Mono.just(new Receta(id)));
				  })
				 .defaultIfEmpty(new Receta())
				 .map(p -> {
					  respuesta.put("timestamp", new Date());
					  
					  if (null == p.getId()) {
						  respuesta.put("mensaje", "receta con id:"+ id +" no localizada"); 
						  return ResponseEntity
								  .status(HttpStatus.NOT_FOUND)
								  .contentType(MediaType.APPLICATION_JSON)
								  .body(respuesta);
					  }
				  
					  respuesta.put("receta", p);
					  respuesta.put("mensaje", "receta eliminada con éxito");
                 
					  return ResponseEntity
		                 	     .ok()
		                	     .contentType(MediaType.APPLICATION_JSON)
		                	     .body(respuesta);
		          });
	}
	
	private Mono<ResponseEntity<Map<String, Object>>> createError(Throwable ex) {
		 Map<String, Object> respuesta = new HashMap<String, Object>();
		 
		 return Mono.just(ex)
				  .cast(WebExchangeBindException.class)
				  .flatMapMany(e-> Flux.fromIterable(e.getFieldErrors()))
				  .map(fieldError-> new ValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
				  .collectList()
			      .flatMap(list -> {
			    	    respuesta.put("errors", list);
						respuesta.put("mensaje", "error al crear la receta");
						respuesta.put("timestamp", new Date());
						
						return Mono.just(ResponseEntity
								.badRequest()
								.body(respuesta));
			     });
	}
	
	@GetMapping("/{id}/raciones")
	public Mono<ResponseEntity<List<Racion>>> consultarRaciones(@PathVariable String id) {
		return recetaDao.findById(id)
						.map(receta -> {
							List<Racion> raciones = receta.getRaciones();
							
				        	return ResponseEntity
				        			.ok()
				        			.contentType(MediaType.APPLICATION_JSON)
				        			.body(raciones);
				        }).defaultIfEmpty(
								ResponseEntity.notFound().build()
						);
	}
	
	@DeleteMapping("/{id}/raciones")
	public Mono<ResponseEntity<Map<String, Object>>> eliminarRaciones(@PathVariable String id) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return recetaDao
				 .findById(id)
				 .flatMap(r-> {
					  r.setRaciones(null);
					  return recetaDao.save(r);
				  })
				 .defaultIfEmpty(new Receta())
				 .map(r -> {
					  respuesta.put("timestamp", new Date());
					  
					  if (null == r.getId()) {
						  respuesta.put("mensaje", "receta con id:"+ id +" no localizada"); 
						  return ResponseEntity
								  .status(HttpStatus.NOT_FOUND)
								  .contentType(MediaType.APPLICATION_JSON)
								  .body(respuesta);
					  }
				  
					  respuesta.put("receta", r);
					  respuesta.put("mensaje", "Lista de raciones eliminada con éxito");
                 
					  return ResponseEntity
		                 	     .ok()
		                	     .contentType(MediaType.APPLICATION_JSON)
		                	     .body(respuesta);
		          });
	}
}
