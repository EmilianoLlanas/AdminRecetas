package com.springboot.webflux.app.controllers;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.springboot.webflux.app.models.dao.CategoriaDao;
import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.ValidationError;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/categoria-controller/categorias")
public class CategoriaController {
	
	@Autowired
	CategoriaDao categoriaDao;

	@GetMapping
	public Flux<Categoria> listar() {
		return categoriaDao.findAll();
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Categoria>> consultar(@PathVariable String id) {
		return categoriaDao.findById(id)
				          .map(categoria -> {
				        	  return ResponseEntity.ok()
				        			  .contentType(MediaType.APPLICATION_JSON)
				        	          .body(categoria);
				          })
						  .defaultIfEmpty(
							  ResponseEntity.notFound().build()
						  );
	}
	
	@PostMapping("/crear")
	public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Categoria> monoCategoria) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return monoCategoria
				  .flatMap(categoria-> {
					  return categoriaDao
							     .save(categoria)
			                     .map(cat -> {
			                    	    respuesta.put("categoria", cat);
										respuesta.put("mensaje", "categoria creado con éxito");
										respuesta.put("timestamp", new Date());
				                 
										return ResponseEntity
						                 	     .created(URI.create("/categoria-controller/categorias/"+cat.getId()))
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
									respuesta.put("mensaje", "error al crear el categoria");
									respuesta.put("timestamp", new Date());
									
									return Mono.just(ResponseEntity
											.badRequest()
											.body(respuesta));
						     });
				  });
	}
}
