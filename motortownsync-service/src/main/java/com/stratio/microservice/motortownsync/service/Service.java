package com.stratio.microservice.motortownsync.service;

import com.stratio.microservice.motortownsync.entity.Producto;
import com.stratio.microservice.motortownsync.service.model.ServiceInput;
import com.stratio.microservice.motortownsync.service.model.ServiceOutput;

import java.util.List;

public interface Service {

  ServiceOutput doSomething(ServiceInput input);

  int numberOfProductos();

  List<Producto> getProductos();

  String omniSearch();
}
