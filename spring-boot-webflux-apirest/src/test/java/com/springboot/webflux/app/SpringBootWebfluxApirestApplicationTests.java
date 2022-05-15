package com.springboot.webflux.app;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.springboot.webflux.app.models.dao.CategoriaDao;
import com.springboot.webflux.app.models.dao.ProductoDao;
import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {

	@Autowired
	private WebTestClient testClient;
	
	@Autowired
	private ProductoDao productoDao;
	
	@Autowired
	private CategoriaDao categoriaDao;
	
	@Test
	void testListar() {
		Long totalProductos = productoDao.count().block();
		
		testClient
		  .get()
		  .uri("/producto-controller/productos")
		  .accept(MediaType.APPLICATION_JSON)
		  .exchange()
		  .expectStatus().isOk()
		  .expectHeader().contentType(MediaType.APPLICATION_JSON)
		  .expectBodyList(Producto.class)
		  .hasSize(totalProductos.intValue())
		  ;
	}
	
	@Test
	void testListarPorNombre() {
		String nombreProducto = "TV";
		Long totalProductos = productoDao.findAll()
								         .filter(producto -> producto.getNombre().contains(nombreProducto))
								         .count()
								         .block();
		
		testClient
		  .get()
		  .uri("/producto-controller/productos?nombre="+nombreProducto)
		  .accept(MediaType.APPLICATION_JSON)
		  .exchange()
		  .expectStatus().isOk()
		  .expectHeader().contentType(MediaType.APPLICATION_JSON)
		  .expectBodyList(Producto.class)
		  .hasSize(totalProductos.intValue())
		  ;
	}
	
	@Test
	void testListarPorCategoria() {
		String nombreCategoria = "Cómputo";

		Map<String, Producto> productosCat = productoDao
				                        .findByCategoria_Nombre(nombreCategoria)
				                        .collectMap(producto -> producto.getId())
				                        .block();
		
		testClient
		  .get()
		  .uri("/producto-controller/productos?categoria="+nombreCategoria)
		  .accept(MediaType.APPLICATION_JSON)
		  .exchange()
		  .expectStatus().isOk()
		  .expectHeader().contentType(MediaType.APPLICATION_JSON)
		  .expectBodyList(Producto.class)
		  .hasSize(productosCat.size())
		  .consumeWith(response -> {
			  List<Producto> productos = response.getResponseBody();
			  for(Producto producto: productos) {
				  System.out.println("Producto:"+producto);
				  if(!productosCat.containsKey(producto.getId())) {
					  throw new AssertionError("No se encontró el producto con id="+producto.getId());
				  }
			  }
		  })
		  ;
	}
	
	@Test
	void testCrearProducto() {
		
		Categoria categoria = categoriaDao.findByNombre("Electrónica").block();
		
		Producto productoNuevo = new Producto();
		productoNuevo.setNombre("Demo Producto");
		productoNuevo.setPrecio(3500D);
		productoNuevo.setCreatedAt(new Date());
		productoNuevo.setCategoria(categoria);
		
		testClient
		  .post()
		  .uri("/producto-controller/productos")
		  .contentType(MediaType.APPLICATION_JSON)
		  .body(Mono.just(productoNuevo), Producto.class)
		  .accept(MediaType.APPLICATION_JSON)
		  .exchange()
		  .expectStatus().isCreated()
		  .expectHeader().contentType(MediaType.APPLICATION_JSON)
		  .expectBody()
		  .jsonPath("$.producto.id").isNotEmpty()
		  .jsonPath("$.producto.precio").isNumber()
		  .jsonPath("$.producto.nombre").isEqualTo(productoNuevo.getNombre());
		  
	}
	
	@Test
	void testEditarProducto() {
		String testCategoria = "Electrónica";
		Categoria categoria = categoriaDao.findByNombre(testCategoria).block();
		
		Producto producto = productoDao.findByNombre("HP Notebook Omen 17").blockFirst();
		
		Producto productoEditado = new Producto();
		productoEditado.setNombre(producto.getNombre());
		productoEditado.setPrecio(producto.getPrecio());
		productoEditado.setCreatedAt(producto.getCreatedAt());
		productoEditado.setCategoria(categoria);
		
		testClient
		  .put()
		  .uri("/producto-controller/productos/{id}", Collections.singletonMap("id", producto.getId()))
		  .contentType(MediaType.APPLICATION_JSON)
		  .body(Mono.just(productoEditado), Producto.class)
		  .accept(MediaType.APPLICATION_JSON)
		  .exchange()
		  .expectStatus().isOk()
		  .expectHeader().contentType(MediaType.APPLICATION_JSON)
		  .expectBody()
		  .jsonPath("$.producto.categoria.nombre").isEqualTo(testCategoria);
		
	}

}
