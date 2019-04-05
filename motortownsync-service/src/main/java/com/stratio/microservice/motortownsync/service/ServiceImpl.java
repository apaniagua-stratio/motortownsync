package com.stratio.microservice.motortownsync.service;

import com.stratio.microservice.motortownsync.entity.Producto;
import com.stratio.microservice.motortownsync.repository.PostgresRepository;
import com.stratio.microservice.motortownsync.service.model.ServiceInput;
import com.stratio.microservice.motortownsync.service.model.ServiceOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

@Slf4j
@Service
public class ServiceImpl implements com.stratio.microservice.motortownsync.service.Service{

  @Autowired
  private PostgresRepository repo;

  @Override
  public ServiceOutput doSomething(ServiceInput input) {

    return null;

  }


  public int numberOfProductos() {
    return repo.getProductos().size();
  }

  public List<Producto> getProductos() {
    return repo.getProductos();
  }

  public String omniSearch() {

    String sUrl="https://se-searcher.anjana.local/searcher/domains";
    String command = "curl -X GET https://se-searcher.anjana.local/searcher/domains";
    String resul ="";

    try {

      URL url = new URL(sUrl);

      BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        for (String line; (line = reader.readLine()) != null;) {
          resul += line;
        }
        reader.close();

      }
      catch (IOException e) {
        e.printStackTrace();
      }


    return resul;

      /* with a local process
      Process process = Runtime.getRuntime().exec(command);
      InputStream inputStream= process.getInputStream();
      inputStream.
      process.destroy();
      */




  }

}
