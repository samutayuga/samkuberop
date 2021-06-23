package com.samutup.crd.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samutup.crd.content.config.ContentSettings;
import com.samutup.crd.content.model.ErrorMessage;
import com.samutup.crd.content.model.ResourcePath;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContentReponseUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentReponseUtil.class);

  static ObjectMapper objectMapper = new ObjectMapper();

  public static String toJsonString(Object waypointStat) {
    try {
      return objectMapper.writeValueAsString(waypointStat);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return "{}";
  }

  public static <T> T fromJSON(final TypeReference<T> type,
      final String jsonPacket) {
    T data = null;

    try {
      data = new ObjectMapper().readValue(jsonPacket, type);
    } catch (Exception e) {
      // Handle the problem
    }
    return data;
  }

  public static String getJsonString(String jsonString, String field) {
    try {
      return objectMapper.readTree(jsonString).get(field).toString();
    } catch (JsonProcessingException jsonProcessingException) {
      jsonProcessingException.printStackTrace();
    }
    return null;
  }


  public static Handler<RoutingContext> handleRequest(ContentSettings contentSettings,
      CRDFacade crdFacade) {
    return rc -> {
      String availableName = contentSettings.getResourcePaths().stream().map(
          ResourcePath::getPath)
          .collect(
              Collectors.joining(","));
      String uri = rc.request().uri();
      Optional<ResourcePath> resourcePath = contentSettings.getResourcePaths().stream()
          .filter(p -> uri.startsWith(p.getPath())).findFirst();

      if (resourcePath.isEmpty()) {
        rc.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
            .putHeader("Content-Type", "application/json")
            .end(ContentReponseUtil
                .toJsonString(
                    new ErrorMessage().setErrMessage("available resource " + availableName)));
      } else {
        //get the path config
        ResourcePath rp = resourcePath.get();
        LOGGER.info("resource path " + resourcePath);
        if (HttpMethod.POST.equals(rc.request().method())) {
          try {
            String strBody = rc.getBodyAsString();
            Map atTwinSession = Json.decodeValue(strBody, Map.class);
            LOGGER.info("serving POST for " + uri + " body=" + strBody);
            //do business logic
            //perform crd request
            crdFacade.create(atTwinSession, rp, rc);


          } catch (Exception anyException) {
            LOGGER.error("error ", anyException);
            rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .putHeader("Content-Type", "application/json")
                .end(ContentReponseUtil
                    .toJsonString(new ErrorMessage().setErrMessage(anyException.getMessage())));
          }


        } else if (HttpMethod.GET.equals(rc.request().method())) {
          Optional<ResourcePath> atmProviderOptional = contentSettings.getResourcePaths().stream()
              .filter(p -> uri.startsWith(p.getPath())).findFirst();

        } else {
          rc.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
      }


    };

  }
}
