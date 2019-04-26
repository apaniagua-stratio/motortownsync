package com.stratio.microservice.motortownsync.service;

import com.stratio.microservice.motortownsync.entity.Producto;
import com.stratio.microservice.motortownsync.service.model.ServiceInput;
import com.stratio.microservice.motortownsync.service.model.ServiceOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Service {

  ServiceOutput doSomething(ServiceInput input);

  int numberOfProductos();

  List<Producto> getProductos();

  List<String> getProductosCsv();

  ServiceOutput writeProductsToSftp(ServiceInput input);

  String writeProductsToSftp();

  String writeProductsDiffToSftp();

 /*
  String getFromMagentoAPI(String id);

  CompletableFuture<String> async_getFromMagentoAPI(String id);

  CompletableFuture<String>  async_postToMagentoAPI(Producto prod);

  String postToMagentoAPI(Producto prod);


  String getJSONProducto();

  String getJSONProducto(Producto prod);
  */

}
