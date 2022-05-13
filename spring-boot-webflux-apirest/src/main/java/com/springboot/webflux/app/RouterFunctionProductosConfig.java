package com.springboot.webflux.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.springboot.webflux.app.handlers.ProductoHandler;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterFunctionProductosConfig {
	private static final String productosPath = "/producto-controller/productos";
	
	@Bean
	public RouterFunction<ServerResponse> routes(ProductoHandler handler) {
		return route(GET(productosPath), handler::listar)
			   .andRoute(GET(productosPath+"/{id}"), handler::consultar)
			   .andRoute(POST(productosPath), handler::crear)
			   .andRoute(DELETE(productosPath+"/{id}"), handler::borrar)
			   .andRoute(PUT(productosPath+"/{id}"), handler::editar);
	}

}
