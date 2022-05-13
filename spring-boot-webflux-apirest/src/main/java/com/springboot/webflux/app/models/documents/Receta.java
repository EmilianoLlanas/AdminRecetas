package com.springboot.webflux.app.models.documents;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="recetas")
public class Receta {
	@Id
	private String id;
	@NotEmpty(message = "Nombre es requerido")
	private String nombre;
	
	@NotNull
	@Min(value = 1, message = "La dificultad no debe ser menor que 1")
    @Max(value = 10, message = "La dificultad no debe ser mayor que 10")
	private Integer dificultad;
	
	@NotEmpty
	private String tiempoPreparacion;
	private List<Racion> raciones;
	private String descripcion;
	
	private String url;
	
	public Receta() {
	}
	
	public Receta(String id) {
		this.id = id;
	}
	
	public Receta(String nombre, Integer dificultad, String tiempoPreparacion) {
		this.nombre = nombre;
		this.dificultad = dificultad;
		this.tiempoPreparacion = tiempoPreparacion;
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
	public Integer getDificultad() {
		return dificultad;
	}
	public void setDificultad(Integer dificultad) {
		this.dificultad = dificultad;
	}
	public String getTiempoPreparacion() {
		return tiempoPreparacion;
	}
	public void setTiempoPreparacion(String tiempoPreparacion) {
		this.tiempoPreparacion = tiempoPreparacion;
	}
	public List<Racion> getRaciones() {
		return raciones;
	}
	public void setRaciones(List<Racion> raciones) {
		this.raciones = raciones;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
