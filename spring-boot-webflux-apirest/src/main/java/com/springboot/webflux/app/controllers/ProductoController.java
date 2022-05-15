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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.springboot.webflux.app.models.dao.CategoriaDao;
import com.springboot.webflux.app.models.dao.ProductoDao;
import com.springboot.webflux.app.models.document.Categoria;
import com.springboot.webflux.app.models.document.Producto;

import ch.qos.logback.core.status.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/producto-controller/productos")
public class ProductoController {
	
	@Autowired
	private ProductoDao productoDao;
	
	@Autowired
	private CategoriaDao categoriaDao;
	
	@GetMapping ("/")
	public Flux<Producto> listar(@RequestParam(required=false) Map<String,String> qparams) {
		qparams.forEach((a,b) -> {
			System.out.println(String.format("%s -> %s", a,b));
			
		});
		String nombre = qparams.get("nombre");
		String precio = qparams.get("precio");
		String categoria = qparams.get("categoria");
		return productoDao.findAll()
							.filter(producto -> null!=nombre ? producto.getNombre().contains(nombre) : true)
							.filter(producto -> null!=precio ? producto.getPrecio()<=Double.parseDouble(precio) : true)
							.filter(producto -> null!=categoria ? producto.getNombre().equals(categoria) : true);
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Producto>> consultar(@PathVariable String id) {
	  return productoDao.findById(id)
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
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Map<String, Object>>> borrar(@PathVariable String id) {
		Map<String, Object> respuesta = new HashMap<String, Object>();			
		return productoDao.findById(id)
		
		          .flatMap(prod -> {
		        	  			
		        	  			return productoDao.delete(prod).then(Mono.just(id));
		        	  			
		          })
		          .defaultIfEmpty("")
		          .map(check -> {
		        	  if(check.equals("")) {
		        		  	
							respuesta.put("mensaje", "no existe el producto id"+id);
							respuesta.put("timestamp", new Date());
							
							return ResponseEntity.status(HttpStatus.NOT_FOUND)
									.contentType(MediaType.APPLICATION_JSON)
									.body(respuesta);
		        	  }
		        	  respuesta.put("mensaje", "vaja exitosa articulo con el id"+id);
						respuesta.put("timestamp", new Date());
		        	  
		        	  return ResponseEntity.ok()
		        	  .contentType(MediaType.APPLICATION_JSON)
		        	  	.body(respuesta);
		          });
	}
		
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Map<String, Object>>> editar(@PathVariable String id, @Valid @RequestBody Mono<Producto> monoProducto) {
		Map<String, Object> respuesta = new HashMap<String, Object>();	
		
		return monoProducto
				.flatMap(producto ->{
				
					return productoDao.findById(id)
		
		          .flatMap(prod -> {
		        	  			prod.setNombre(producto.getNombre());
		        	  			prod.setPrecio(producto.getPrecio());
		        	  			prod.setCategoria(producto.getCategoria());
		        	  			return productoDao.save(prod);
		        	  			
		          })
		          .defaultIfEmpty(new Producto())
		          .map(prod -> {
		        	  if(null == prod.getId()) {
		        		  	
							respuesta.put("mensaje", "no existe el producto id"+id);
							respuesta.put("timestamp", new Date());
							
							return ResponseEntity.status(HttpStatus.NOT_FOUND)
									.contentType(MediaType.APPLICATION_JSON)
									.body(respuesta);
		        	  }
		        	  
		        	  
		        		respuesta.put("producto", prod);
						respuesta.put("mensaje", "edicion éxito");
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
	
	private Mono<ResponseEntity<Map<String, Object>>> generarError(Throwable ex)
	{
		
		Map<String, Object> respuesta = new HashMap<String, Object>();
					return Mono.just(ex)
					
					 .cast(WebExchangeBindException.class)
					 .flatMapMany(e-> Flux.fromIterable(e.getFieldErrors()))
					.map(e -> {
						
						return "Campo:" + e.getField() + " Error:" + e.getDefaultMessage();
						
						
					}).collectList()
					.flatMap(list->{
						
						respuesta.put("errors", list);
						respuesta.put("mensaje", "Ocurrio un error");
						respuesta.put("timestamp", new Date());
						
							return Mono.just(
									ResponseEntity
								.badRequest()
								.body(respuesta));
								
					});
	}

			  
			 
	@PostMapping("")
	public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Producto> monoProducto) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		return monoProducto.flatMap(producto -> {
			return productoDao
					.save(producto)
					.map(prod -> {
						
						respuesta.put("producto", prod);
						respuesta.put("mensaje", "producto creado con éxito");
						respuesta.put("timestamp", new Date());
						
							return ResponseEntity.created(URI.create("/producto-controller/productos/" + prod.getId()))
							.contentType(MediaType.APPLICATION_JSON)
							.body(respuesta);
			});
		}).onErrorResume(ex -> {
			return generarError(ex);
					
		});
	}
	
	@GetMapping ("/categorias")
	public Flux<Categoria> listarCategorias() {
		return categoriaDao.findAll();
		
	}

	@GetMapping("/categorias/{id}")
	public Mono<ResponseEntity<Categoria>> consultarCategoria(@PathVariable String id) {
	  return categoriaDao.findById(id)
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
	@PostMapping("/categorias")
	public Mono<ResponseEntity<Map<String, Object>>> crearCategoria(@Valid @RequestBody Mono<Categoria> monoCategoria) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		return monoCategoria.flatMap(categoria -> {
			return categoriaDao
					.save(categoria)
					.map(cat -> {
						
						respuesta.put("categoria", cat);
						respuesta.put("mensaje", "producto creado con éxito");
						respuesta.put("timestamp", new Date());
						
							return ResponseEntity.created(URI.create("/producto-controller/productos/" + cat.getId()))
							.contentType(MediaType.APPLICATION_JSON)
							.body(respuesta);
			});
		}).onErrorResume(ex -> {
			return generarError(ex);
					
		});
	}
	
	

}
