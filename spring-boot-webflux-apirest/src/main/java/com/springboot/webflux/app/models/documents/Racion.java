package com.springboot.webflux.app.models.documents;

import javax.validation.Valid;
import javax.validation.constraints.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="raciones")
public class Racion {
	@Id
	private String id;
	
	@NotNull
	@Min(value = 1, message = "La cantidad debe ser mayor que 0")
	private Integer cantidad;
	
	@NotNull
	@Valid
	private Ingrediente ingrediente;
	
	public Racion() {
		
	}
	
	public Racion(Integer cantidad, Ingrediente ingrediente) {
		this.cantidad = cantidad;
		this.ingrediente = ingrediente;
	}
	public String getId() {
		return id;
	}
	public Racion setId(String id) {
		this.id = id;
		return this;
	}
	public Integer getCantidad() {
		return cantidad;
	}
	public Racion setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
		return this;
	}
	public Ingrediente getIngrediente() {
		return ingrediente;
	}
	public Racion setIngrediente(Ingrediente ingrediente) {
		this.ingrediente = ingrediente;
		return this;
	}
	
	public boolean equals(Racion otraRacion) {
		return (this.cantidad == otraRacion.cantidad 
			   && this.ingrediente.getNombre().equals(otraRacion.ingrediente.getNombre()))
			   || this.id.equals(otraRacion.id);
	}

	@Override
	public String toString() {
		return "Racion [id=" + id + ", cantidad=" + cantidad + ", ingrediente=" + ingrediente + "]";
	}
	
}
