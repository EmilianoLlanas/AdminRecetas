package com.springboot.webflux.app.models.documents;

import javax.validation.constraints.NotEmpty;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="ingredientes")
public class Ingrediente {
	@Id
	private String id;
	
	@NotEmpty(message = "Nombre es requerido")
	private String nombre;
	
	public Ingrediente() {
	}
	
	public Ingrediente(String nombre) {
		this.nombre = nombre;
	}

	public String getId() {
		return id;
	}
	public Ingrediente setId(String id) {
		this.id = id;
		return this;
	}
	public String getNombre() {
		return nombre;
	}
	public Ingrediente setNombre(String nombre) {
		this.nombre = nombre;
		return this;
	}
}
