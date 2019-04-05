package com.stratio.microservice.motortownsync.service.mapper;

import com.stratio.microservice.motortownsync.generated.rest.model.MicroserviceRequest;
import com.stratio.microservice.motortownsync.service.model.ServiceInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceRequestMapper {

  ServiceInput mapInput(MicroserviceRequest request);
}
