package com.springboot.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.springboot.webflux.app.models.documents.Racion;

public interface RacionDao extends ReactiveMongoRepository<Racion, String> {

}
