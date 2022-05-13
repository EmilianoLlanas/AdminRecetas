package com.springboot.webflux.app.controllers;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.springboot.webflux.app.models.dao.ProductoDao;
import com.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/producto-controller-old/productos")
public class ProductoController {
	
	Logger log = LoggerFactory.getLogger(ProductoController.class);
	
	@Autowired
	private ProductoDao productoDao;

	@GetMapping
	public Flux<Producto> listar(@RequestParam(required=false) Map<String,String> qparams) {
		qparams.forEach((a,b) -> {
	        System.out.println(String.format("%s -> %s",a,b));
	    });
		
		String nombre = qparams.get("nombre");
		String precio = qparams.get("precio");
		String categoria = qparams.get("categoria");
		
		return productoDao.findAll()
				          .filter(producto-> null!=nombre ? producto.getNombre().contains(nombre) : true)
				          .filter(producto-> null!=precio ? producto.getPrecio()<=Double.parseDouble(precio) : true )
				          .filter(producto-> null!=categoria ? producto.getCategoria().getNombre().equals(categoria) : true );
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Producto>> consultar(@PathVariable String id) {
		return productoDao
				.findById(id)
				.map(producto -> {
					return ResponseEntity
					  .ok()
					  .contentType(MediaType.APPLICATION_JSON)
					  .body(producto);
					
				})
				.defaultIfEmpty(
						ResponseEntity.notFound().build()
                );
	}
	
	@PostMapping
	public Mono<ResponseEntity<Map<String,Object>>> crear(@Valid @RequestBody Mono<Producto> monoProducto) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		return monoProducto
				.flatMap(producto -> {
					log.info("producto:"+producto.toString());
					return productoDao
						.save(producto)
						.map(prod ->{
							
							respuesta.put("producto", producto);
							respuesta.put("message", "alta exitosa");
							respuesta.put("timestamp", new Date());
							
							return ResponseEntity
									 .created(URI.create("/producto-controller/productos/"+prod.getId()))
									 .contentType(MediaType.APPLICATION_JSON)
									 .body(respuesta);
						});
	           }).onErrorResume(ex -> {
	        	   return generarError(ex);
	           });
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Map<String,Object>>> editar(@Valid @RequestBody Mono<Producto> monoProducto, @PathVariable String id) {
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
						           .map(prod -> {
						        	   
						        	   if (null == prod.getId()) {
											respuesta.put("message", "no existe el producto con id:"+id);
											respuesta.put("timestamp", new Date());
											
											return ResponseEntity.status(HttpStatus.NOT_FOUND)
													             .contentType(MediaType.APPLICATION_JSON)
													             .body(respuesta);
						        	   }
						        	   
										respuesta.put("producto", prod);
										respuesta.put("message", "edición exitosa");
										respuesta.put("timestamp", new Date());
										
										return ResponseEntity
												 .ok()
												 .contentType(MediaType.APPLICATION_JSON)
												 .body(respuesta);
									});
				}).onErrorResume(ex -> {
		        	   return generarError(ex);
		           });
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Map<String,Object>>> borrar(@PathVariable String id) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		return productoDao.findById(id)
						           .flatMap(prod-> {
						        	   return productoDao.delete(prod).then(Mono.just(id));
						           })
						           .defaultIfEmpty("")
						           .map(check -> {
						        	   
						        	   if (check.equals("")) {
											respuesta.put("message", "no existe el producto con id:"+id);
											respuesta.put("timestamp", new Date());
											
											return ResponseEntity.status(HttpStatus.NOT_FOUND)
													             .contentType(MediaType.APPLICATION_JSON)
													             .body(respuesta);
						        	   }
						        	   
						        	   respuesta.put("message", "baja exitosa");
										respuesta.put("timestamp", new Date());
										
						        	   return ResponseEntity.ok()
						        	   .contentType(MediaType.APPLICATION_JSON)
							             .body(respuesta);
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
}
