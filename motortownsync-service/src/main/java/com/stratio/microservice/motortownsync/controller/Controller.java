package com.stratio.microservice.motortownsync.controller;

import static java.util.stream.Collectors.toList;

import brave.Tracer;

import com.stratio.microservice.motortownsync.generated.rest.api.POSTEndpointOfTheMicroserviceApi;
import com.stratio.microservice.motortownsync.generated.rest.model.MicroserviceRequest;
import com.stratio.microservice.motortownsync.generated.rest.model.MicroserviceResponse;
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

    log.info("AURGI Connect to Postgres");
    log.info("AURGI Productos table elements: " + service.numberOfProductos());

    log.info("AURGI Connect to Omnisearch");
    log.info("AURGI Omnisearch domains: " + service.omniSearch());
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

}
