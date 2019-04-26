package com.stratio.microservice.motortownsync.controller;

import static java.util.stream.Collectors.toList;

import brave.Tracer;

import com.stratio.microservice.motortownsync.entity.Producto;
import com.stratio.microservice.motortownsync.generated.rest.api.POSTEndpointOfTheMicroserviceApi;
import com.stratio.microservice.motortownsync.generated.rest.model.MicroserviceRequest;
import com.stratio.microservice.motortownsync.generated.rest.model.MicroserviceResponse;
import com.stratio.microservice.motortownsync.repository.PostgresRepository;
import com.stratio.microservice.motortownsync.repository.ProductosRepository;
import com.stratio.microservice.motortownsync.service.Service;
import com.stratio.microservice.motortownsync.service.mapper.ServiceRequestMapper;
import com.stratio.microservice.motortownsync.service.mapper.ServiceResponseMapper;
import com.stratio.microservice.motortownsync.service.model.ServiceOutput;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiParam;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
public class Controller implements POSTEndpointOfTheMicroserviceApi {


  private final Tracer tracer;

  private final Service service;

  private final ServiceRequestMapper requestMapper;

  private final ServiceResponseMapper responseMapper;

  @Value("${service.name}")
  private String serviceId;

  @Autowired
  public Controller(Tracer tracer,
      Service service,
      ServiceRequestMapper requestMapper,
      ServiceResponseMapper responseMapper) {
    this.tracer = tracer;
    this.service = service;
    this.requestMapper = requestMapper;
    this.responseMapper = responseMapper;


  }

  @Override
  @RequestMapping(value = "/microservices",
      produces = { "application/json" },
      consumes = { "application/json" },
      method = RequestMethod.POST)
  public ResponseEntity<MicroserviceResponse> doSomething(@ApiParam(value = "Example of body input for the microservice" ,required=true )  @Valid @RequestBody MicroserviceRequest body)
  throws Exception {

      ServiceOutput output = service.doSomething(requestMapper.mapInput(body));

      MicroserviceResponse result = responseMapper.mapOutput(output);

      return new ResponseEntity<>(result, HttpStatus.OK);

  }

  @RequestMapping(value = "/sftpwriter",
          produces = { "application/json" },
          consumes = { "application/json" },
          method = RequestMethod.POST)
  public ResponseEntity<MicroserviceResponse> writeProductsToSftp(@ApiParam(value = "Example of body input for the microservice sftpwriter" ,required=false )  @Valid @RequestBody MicroserviceRequest body)
          throws Exception {

    ServiceOutput output = service.writeProductsToSftp(requestMapper.mapInput(body));

    log.info("AURGI POST received, result is" + output.getExampleOutputField());

    MicroserviceResponse result = responseMapper.mapOutput(output);

    return new ResponseEntity<>(result, HttpStatus.OK);

  }


  @RequestMapping(value = "/sftpdiff",
          produces = { "application/json" },
          method = RequestMethod.GET)
  public String getProductsDiff()
          throws Exception {


    return service.writeProductsDiffToSftp();


  }

}
