package com.springboot.webflux.app.models.document;

import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Document(collection="producto")

public class Producto 
{
	@Id
	private String id;
	
	@NotNull
	@NotEmpty
	private String nombre;
	
	@NotNull 
	@Min(value = 1, message = "El precio no debe ser menor que 1")
	@Max(value = 25000, message = "El precio no debe ser mayor que 25mil")
	private Double precio;
	
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createdAt;
	
	@NotNull
	private Categoria categoria;
	
	public Producto() {
		
	}
	
	
	public Producto(String nombre, Double precio) {
		
		this.nombre = nombre;
		this.precio = precio;
		
	}
	public Producto (String nombre, Double precio, Categoria categoria) {
		this(nombre, precio);
		this.categoria=categoria;
		
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Double getPrecio() {
		return precio;
	}
	public void setPrecio(Double precio) {
		this.precio = precio;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}


	public Categoria getCategoria() {
		return categoria;
	}


	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}	
}