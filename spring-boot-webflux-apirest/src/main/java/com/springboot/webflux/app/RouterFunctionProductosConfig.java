package com.springboot.webflux.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.springboot.webflux.app.handlers.ProductoHandler;
import com.springboot.webflux.app.handlers.RecetaHandler;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@EnableWebFlux
@Configuration
public class RouterFunctionProductosConfig implements WebFluxConfigurer{
	private static final String productosPath = "/producto-controller/productos";
	private static final String recetasPath = "/receta-controller/recetas";

	
	@Bean
	public RouterFunction<ServerResponse> routes(ProductoHandler handler) {
		return route(GET(productosPath), handler::listar)
			   .andRoute(GET(productosPath+"/{id}"), handler::consultar)
			   .andRoute(POST(productosPath), handler::crear)
			   .andRoute(DELETE(productosPath+"/{id}"), handler::borrar)
			   .andRoute(PUT(productosPath+"/{id}"), handler::editar);
	}
	
	@Bean
	public RouterFunction<ServerResponse> routesRecetas(RecetaHandler handler) {
		return route(GET(recetasPath), handler::listar)
			   .andRoute(GET(recetasPath+"/{id}"), handler::consultar)
			   .andRoute(POST(recetasPath), handler::crear)
			   .andRoute(DELETE(recetasPath+"/{id}"), handler::eliminar)
			   .andRoute(PUT(recetasPath+"/{id}"), handler::editar)
				.andRoute(GET(recetasPath+"/{id}/raciones"), handler::consultarRaciones)
				.andRoute(DELETE(recetasPath+"/{id}/raciones"), handler::eliminarRaciones);
	}
	
	 @Override
	    public void addCorsMappings(CorsRegistry registry) {
	        registry.addMapping(productosPath+"/**");
	        CorsRegistration registration = registry.addMapping(recetasPath+"/**");
	        	
	        registration
	            .allowedOrigins("http://localhost:8081")
	            .allowedMethods("GET", "POST", "PUT", "DELETE")
	            .maxAge(3600);
	    }

}
