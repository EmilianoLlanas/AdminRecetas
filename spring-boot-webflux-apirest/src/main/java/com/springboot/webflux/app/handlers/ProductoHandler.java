package com.springboot.webflux.app.handlers;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.springboot.webflux.app.models.dao.ProductoDao;
import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.documents.ValidationError;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.*;

@Component
public class ProductoHandler {
	
	Logger log = LoggerFactory.getLogger(ProductoHandler.class);
	
	@Autowired
	private ProductoDao productoDao;
	
	@Autowired
	private Validator validator;

	
	public Mono<ServerResponse> listar(ServerRequest request) {
		
		String nombre = request.queryParam("nombre").orElse(null);
		String precio = request.queryParam("precio").orElse(null);
		String categoria = request.queryParam("categoria").orElse(null);
		
		Flux<Producto> productos = 
				productoDao.findAll()
				           .filter(producto-> null!=nombre ? producto.getNombre().contains(nombre) : true)
				           .filter(producto-> null!=precio ? producto.getPrecio()<=Double.parseDouble(precio) : true )
				           .filter(producto-> null!=categoria ? producto.getCategoria().getNombre().equals(categoria) : true );	
		
		return ServerResponse
				 .ok()
				 .contentType(MediaType.APPLICATION_JSON)
				 .body(productos, Producto.class);
	}
	

	public Mono<ServerResponse> consultar(ServerRequest request) {
		String id = request.pathVariable("id");

		return productoDao
				.findById(id)
				.flatMap(producto -> {
					return ServerResponse
					  .ok()
					  .contentType(MediaType.APPLICATION_JSON)
					  .body(fromValue(producto));
					
				})
				.switchIfEmpty(
						ServerResponse.notFound().build()
                );
	}
	
	public Mono<ServerResponse> crear(ServerRequest request) {		
		Mono<Producto> monoProducto = request.bodyToMono(Producto.class);
		
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return monoProducto
				.flatMap(producto -> {
					log.info("producto:"+producto.toString());
					return productoDao
						.save(producto)
						.flatMap(prod ->{
							
							Errors errors = new BeanPropertyBindingResult(prod, Producto.class.getName());
							validator.validate(prod, errors);
							
							if (errors.hasErrors()) {
								return createErrors(errors.getFieldErrors());
							}
							
							respuesta.put("producto", producto);
							respuesta.put("message", "alta exitosa");
							respuesta.put("timestamp", new Date());
							
							return ServerResponse
									 .created(URI.create("/producto-controller/productos/"+prod.getId()))
									 .contentType(MediaType.APPLICATION_JSON)
									 .body(fromValue(respuesta));
						});
	           });
	}
	
	public Mono<ServerResponse> editar(ServerRequest request) {
		String id = request.pathVariable("id");
		Mono<Producto> monoProducto = request.bodyToMono(Producto.class);
		
		Map<String, Object> respuesta = new HashMap<String, Object>();
		return monoProducto
				.flatMap(producto -> {
						return productoDao.findById(id)
						           .flatMap(prod-> {
										
						        	   prod.setNombre(producto.getNombre());
						        	   prod.setPrecio(producto.getPrecio());
						        	   prod.setCategoria(producto.getCategoria());
						        	   
						        	   return productoDao.save(prod);
						           })
						           .defaultIfEmpty(new Producto())
						           .flatMap(prod -> {
						        	   
						        	   Errors errors = new BeanPropertyBindingResult(prod, Producto.class.getName());
										validator.validate(prod, errors);
										
										if (errors.hasErrors()) {
											return createErrors(errors.getFieldErrors());
										}
						        	   
						        	   if (null == prod.getId()) {
											respuesta.put("message", "no existe el producto con id:"+id);
											respuesta.put("timestamp", new Date());
											
											return ServerResponse.status(HttpStatus.NOT_FOUND)
													             .contentType(MediaType.APPLICATION_JSON)
													             .body(fromValue(respuesta));
						        	   }
						        	   
										respuesta.put("producto", prod);
										respuesta.put("message", "edición exitosa");
										respuesta.put("timestamp", new Date());
										
										return ServerResponse
												 .ok()
												 .contentType(MediaType.APPLICATION_JSON)
												 .body(fromValue(respuesta));
									});
				});
//				.onErrorResume(ex -> {
//		        	   return generarError(ex);
//		           });
	}
	
	public Mono<ServerResponse> borrar(ServerRequest request) {

		String id = request.pathVariable("id");
		Map<String, Object> respuesta = new HashMap<String, Object>();
		return productoDao.findById(id)
						           .flatMap(prod-> {
						        	   return productoDao.delete(prod).then(Mono.just(id));
						           })
						           .defaultIfEmpty("")
						           .flatMap(check -> {
						        	   
						        	   if (check.equals("")) {
											respuesta.put("message", "no existe el producto con id:"+id);
											respuesta.put("timestamp", new Date());
											
											return ServerResponse.status(HttpStatus.NOT_FOUND)
													             .contentType(MediaType.APPLICATION_JSON)
													             .body(fromValue(respuesta));
						        	   }
						        	   
						        	   respuesta.put("message", "baja exitosa");
										respuesta.put("timestamp", new Date());
										
						        	   return ServerResponse.ok()
						        	   .contentType(MediaType.APPLICATION_JSON)
							             .body(fromValue(respuesta));
						           });
	}
	
	private Mono<ResponseEntity<Map<String,Object>>> generarError(Throwable ex) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return Mono.just(ex)
 			   .cast(WebExchangeBindException.class)
 			   .flatMapMany(e-> Flux.fromIterable(e.getFieldErrors()))
 			   .map(e-> {
 				   
 				   return "campo: "+e.getField()+" error:"+e.getDefaultMessage();
 			   }).collectList()
 			   .flatMap(list -> {
 				   
 				   respuesta.put("errors", list);
					   respuesta.put("message", "Ocurrió un error");
					   respuesta.put("timestamp", new Date());
						
 			    	  return Mono.just( 
 			    			  ResponseEntity
 			    			   .badRequest()
 			    			   .body(respuesta));
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
