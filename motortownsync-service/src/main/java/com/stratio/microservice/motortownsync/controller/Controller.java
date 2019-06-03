package com.stratio.microservice.motortownsync.controller;

import brave.Tracer;

import com.stratio.microservice.motortownsync.generated.rest.api.POSTEndpointOfTheMicroserviceApi;
import com.stratio.microservice.motortownsync.generated.rest.model.MicroserviceRequest;
import com.stratio.microservice.motortownsync.generated.rest.model.MicroserviceResponse;
import com.stratio.microservice.motortownsync.service.Service;
import com.stratio.microservice.motortownsync.service.mapper.ServiceRequestMapper;
import com.stratio.microservice.motortownsync.service.mapper.ServiceResponseMapper;
import com.stratio.microservice.motortownsync.service.model.ServiceOutput;
import javax.validation.Valid;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    log.info("MOTORTOWN MOTORTOWNSYNC version 0.8");

  }

  /*
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
  */


  @RequestMapping(value = "/sftpwriter",
          produces = { "application/json" },
          consumes = { "application/json" },
          method = RequestMethod.POST)
  public ResponseEntity<MicroserviceResponse> writeProductsToSftp(@ApiParam(value = "Example of body input for the microservice sftpwriter" ,required=false )  @Valid @RequestBody MicroserviceRequest body)
          throws Exception {

    ServiceOutput output = service.writeProductsToSftp(requestMapper.mapInput(body));

    log.info("MOTORTOWN POST received, result is" + output.getExampleOutputField());

    MicroserviceResponse result = responseMapper.mapOutput(output);

    return new ResponseEntity<>(result, HttpStatus.OK);

  }

  @RequestMapping(value = "/reprocess",
          produces = { "application/json" },
          consumes = { "application/json" },
          method = RequestMethod.POST)
  public ResponseEntity<MicroserviceResponse> reprocess(@ApiParam(value = "" ,required=false )  @Valid @RequestBody MicroserviceRequest body)
          throws Exception {

    ServiceOutput output = service.reprocess(requestMapper.mapInput(body));

    MicroserviceResponse result = responseMapper.mapOutput(output);

    return new ResponseEntity<>(result, HttpStatus.OK);

  }

  @RequestMapping(value = "/productscsvfile",
          produces = { "application/json" },
          method = RequestMethod.GET)
  @Timed
  @Transactional(timeout = 180)
  public String getProductsDiff()
          throws Exception {

    log.info("MOTORTOWN GET received");

    String productResult = service.writeProductsToSftp();
    String stockResult = service.writeStockToSftp();

    return productResult + "," + stockResult;

  }

}
