package com.springboot.webflux.app;

import java.util.Date;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;

import com.springboot.webflux.app.models.dao.CategoriaDao;
import com.springboot.webflux.app.models.dao.ProductoDao;
import com.springboot.webflux.app.models.document.Categoria;
import com.springboot.webflux.app.models.document.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner{
	
private Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);
	
	@Autowired
	private ProductoDao productoDao;
	
	@Autowired
	private CategoriaDao categoriaDao;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		
		limpiarProductos()
		.thenMany(limpiarCategorias())
		.thenMany(cargarDatos())
		.thenMany(actualizarDatos())
		.subscribe(item-> log.info(item.getId()));
	}
	
	public Flux<Producto> cargarDatos() {
		
		Categoria electronica = new Categoria("Electrónica");
		Categoria muebles = new Categoria("Muebles");
		Categoria computo = new Categoria("Cómputo");
		Categoria deportes = new Categoria("Deportes");
		
		return Flux.just(electronica, muebles, computo, deportes)
		.flatMap(categoria -> {
		    return categoriaDao.save(categoria);
		}).thenMany(
		
		 Flux.just(new Producto("TV Panasonic Pantalla LCD", 1599.99, electronica),
				new Producto("Sony Camara HD Digital", 2550.00, computo),
				new Producto("Apple iPad Mini", 4590.00, computo),
				new Producto("Lenovo Notebook ideaPad", 11999.00, computo),
				new Producto("HP Multifuncional", 2999.99, computo),
				new Producto("Bicicleta BMX infantil R12", 3750.00, deportes),
				new Producto("HP Notebook Omen 17", 6599.90, computo),
				new Producto("Cómoda de 5 Cajones", 4390.00, muebles),
				new Producto("TV Sony Bravia OLED 4K", 22400.00, muebles))
		.flatMap(producto -> {
		producto.setCreatedAt(new Date());
		return productoDao.save(producto);
		})
		);
	}
	
	public Mono<Void> limpiarProductos() {
		return mongoTemplate.dropCollection("productos");
	}
	public Mono<Void> limpiarCategorias() {
		return mongoTemplate.dropCollection("categorias");
	}
	
	public Flux<Producto> actualizarDatos() {
		return  productoDao
				.findAll()
		.flatMap(producto -> {
			producto.setCreatedAt(new Date());
			return productoDao.save(producto);		
		}).doOnNext(item -> log.info("Updated:" +item.getNombre()));
	}
	
	
	

}
