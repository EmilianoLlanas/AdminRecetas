package com.springboot.webflux.app.models.documents;

public class ValidationError {
	private String campo;
	private String error;
	
	public ValidationError() {
		
	}
	
	public ValidationError(String campo, String error) {
		this.campo = campo;
		this.error = error;
	}
	
	public String getCampo() {
		return campo;
	}
	public void setCampo(String campo) {
		this.campo = campo;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
	
}
