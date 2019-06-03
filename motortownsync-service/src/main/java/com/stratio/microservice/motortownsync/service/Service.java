package com.stratio.microservice.motortownsync.service;

import com.stratio.microservice.motortownsync.entity.Producto;
import com.stratio.microservice.motortownsync.service.model.ServiceInput;
import com.stratio.microservice.motortownsync.service.model.ServiceOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Service {


  int numberOfProductos();

  List<Producto> getProductos();

  List<String> getProductosCsv();

  ServiceOutput writeProductsToSftp(ServiceInput input);

  String writeProductsToSftp();

  String writeStockToSftp();

  ServiceOutput reprocess(ServiceInput input);

  String writeProductsDiffToSftp();



}
