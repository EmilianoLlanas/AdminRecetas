package com.springboot.webflux.app.handlers;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.springboot.webflux.app.models.dao.RecetaDao;
import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.documents.Racion;
import com.springboot.webflux.app.models.documents.Receta;
import com.springboot.webflux.app.models.documents.ValidationError;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.*;

@Component
@RequestMapping("/receta-controller/recetas")
public class RecetaHandler {
	
	@Autowired
	RecetaDao recetaDao;
	
	@Autowired
	private Validator validator;

	public Mono<ServerResponse> listar(ServerRequest request) {
		String nombre = request.queryParam("nombre").orElse(null);
		
		Flux<Receta> recetas = recetaDao
								.findAll()
								.filter(receta -> null != nombre 
												 ? receta.getNombre().contains(nombre) 
									  			 : true);
		
		return ServerResponse
				 .ok()
				 .contentType(MediaType.APPLICATION_JSON)
				 .body(recetas, Receta.class);
	}
	
	public Mono<ServerResponse> consultar(ServerRequest request) {
		String id = request.pathVariable("id");
		
		return recetaDao.findById(id)
				          .flatMap(receta -> {
				        	  return ServerResponse.ok()
				        			  .contentType(MediaType.APPLICATION_JSON)
				        	          .body(fromValue(receta));
				          })
						  .switchIfEmpty(
							  ServerResponse.notFound().build()
						  );
	}
					     
	public Mono<ServerResponse> crear(ServerRequest request) {
	Mono<Receta> monoReceta = request.bodyToMono(Receta.class);
	Map<String, Object> respuesta = new HashMap<String, Object>();
	
		return monoReceta
				  .flatMap(receta-> {
					  return recetaDao
							     .save(receta)
			                     .flatMap(prod -> {
			                    	 
			                    	Errors errors = new BeanPropertyBindingResult(prod, Receta.class.getName());
									validator.validate(prod, errors);
										
									if (errors.hasErrors()) {
										return createErrors(errors.getFieldErrors());
									}    	 	
									respuesta.put("receta", prod);
									respuesta.put("mensaje", "receta creada con éxito");
									respuesta.put("timestamp", new Date());
				                
										return ServerResponse
						                 	     .created(URI.create("/receta-controller/recetas/"+prod.getId()))
						                	     .contentType(MediaType.APPLICATION_JSON)
						                	     .body(fromValue(respuesta));
				                   });
				  });
	}
	

	public Mono<ServerResponse> editar(ServerRequest request) {
		String id = request.pathVariable("id");
		Mono<Receta> monoReceta = request.bodyToMono(Receta.class);

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
							  .flatMap(r -> {
								  respuesta.put("timestamp", new Date());
								  
								  Errors errors = new BeanPropertyBindingResult(r, Receta.class.getName());
									validator.validate(r, errors);
									
									if (errors.hasErrors()) {
										return createErrors(errors.getFieldErrors());
									}
								  
								  if (null == r.getId()) {
									  respuesta.put("mensaje", "receta con id:"+ id +" no localizada"); 
									  return ServerResponse
											  .status(HttpStatus.NOT_FOUND)
											  .contentType(MediaType.APPLICATION_JSON)
											  .body(fromValue(respuesta));
								  }
							  
								  respuesta.put("receta", r);
								  respuesta.put("mensaje", "receta actualizada con éxito");
			                 
								  return ServerResponse
					                 	     .ok()
					                	     .contentType(MediaType.APPLICATION_JSON)
					                	     .body(fromValue(respuesta));
					          });
				  });
	}
	
	public Mono<ServerResponse> eliminar(ServerRequest request) {
		String id = request.pathVariable("id");
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return recetaDao
				 .findById(id)
				 .flatMap(r-> {
					  return recetaDao.delete(r).then(Mono.just(new Receta(id)));
				  })
				 .defaultIfEmpty(new Receta())
				 .flatMap(p -> {
					  respuesta.put("timestamp", new Date());
					  
					  if (null == p.getId()) {
						  respuesta.put("mensaje", "receta con id:"+ id +" no localizada"); 
						  return ServerResponse
								  .status(HttpStatus.NOT_FOUND)
								  .contentType(MediaType.APPLICATION_JSON)
								  .body(fromValue(respuesta));
					  }
				  
					  respuesta.put("receta", p);
					  respuesta.put("mensaje", "receta eliminada con éxito");
                 
					  return ServerResponse
		                 	     .ok()
		                	     .contentType(MediaType.APPLICATION_JSON)
		                	     .body(fromValue(respuesta));
		          });
	}
	
	public Mono<ServerResponse> consultarRaciones(ServerRequest request) {
		String id = request.pathVariable("id");
		return recetaDao.findById(id)
						.flatMap(receta -> {
							List<Racion> raciones = receta.getRaciones();
							
				        	return ServerResponse
				        			.ok()
				        			.contentType(MediaType.APPLICATION_JSON)
				        			.body(fromValue(raciones));
				        }).switchIfEmpty(
								ServerResponse.notFound().build()
						);
	}
	
	public Mono<ServerResponse> eliminarRaciones(ServerRequest request) {
		String id = request.pathVariable("id");
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return recetaDao
				 .findById(id)
				 .flatMap(r-> {
					  r.setRaciones(null);
					  return recetaDao.save(r);
				  })
				 .defaultIfEmpty(new Receta())
				 .flatMap(r -> {
					  respuesta.put("timestamp", new Date());
					  
					  if (null == r.getId()) {
						  respuesta.put("mensaje", "receta con id:"+ id +" no localizada"); 
						  return ServerResponse
								  .status(HttpStatus.NOT_FOUND)
								  .contentType(MediaType.APPLICATION_JSON)
								  .body(fromValue(respuesta));
					  }
				  
					  respuesta.put("receta", r);
					  respuesta.put("mensaje", "Lista de raciones eliminada con éxito");
                 
					  return ServerResponse.ok()
		                	     .contentType(MediaType.APPLICATION_JSON)
		                	     .body(fromValue(respuesta));
		          });
	}
	
	private Mono<ServerResponse> createErrors(List<FieldError> errors) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return Flux
				.fromIterable(errors)
 			    .map(e-> {
 				   
 				   return new ValidationError(e.getField(), e.getDefaultMessage());//"campo: "+e.getField()+" error:"+e.getDefaultMessage();
 			    }).collectList()
 			    .flatMap(list -> {
 				   
 			    	 respuesta.put("errors", list);
					 respuesta.put("message", "Ocurrió un error");
					 respuesta.put("timestamp", new Date());
					
 			    	  return 
 			    			 ServerResponse
 			    			   .badRequest()
 			    			   .body(fromValue(respuesta));
 			      });
	}
}
