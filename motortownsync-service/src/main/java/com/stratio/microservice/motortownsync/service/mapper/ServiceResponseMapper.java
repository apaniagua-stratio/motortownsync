package com.stratio.microservice.motortownsync.service.mapper;

import com.stratio.microservice.motortownsync.generated.rest.model.MicroserviceResponse;
import com.stratio.microservice.motortownsync.service.model.ServiceOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceResponseMapper {

  MicroserviceResponse mapOutput(ServiceOutput output);
}
