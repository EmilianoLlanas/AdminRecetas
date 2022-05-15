package com.springboot.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.springboot.webflux.app.models.document.Categoria;
import com.springboot.webflux.app.models.document.Producto;

import reactor.core.publisher.Flux;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String>{
	
	public Flux<Producto> findByNombre(String nombre);
	
	public Flux<Producto> findByNombreLike(String nombre);
	
	public Flux<Producto> findByPrecioBetween(Double precioInicial, Double precioFinal);
	
	public Flux<Producto> findByCategoria(Categoria categoria);
	
	public Flux<Producto> findByCategoria_Nombre(String nombre);
	
	public Flux<Producto> findByombreAndPrecioBetween(String nombre, Double precioInicial, Double precioFinal);
	
}