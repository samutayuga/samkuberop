package com.samutup.crd.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samutup.crd.content.config.ContentSettings;
import com.samutup.crd.content.model.ResourcePath;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;
import java.util.Optional;
import java.util.function.Consumer;

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


  public static Handler<RoutingContext> handleRequest(ContentSettings contentSettings) {
    return rc -> {
      //TODO: input validation
      //Map<String, String> paths = rc.get(AtmConfig.PATHS.configVal);
      String uri = rc.request().uri();

      if (HttpMethod.POST.equals(rc.request().method())) {
        //handlePost(contentSettings, uri, rc, kafkaProducer);
        //call kafka producer
        rc.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
            .end(ContentReponseUtil.toJsonString(contentSettings.getResourcePaths()));
      } else if (HttpMethod.GET.equals(rc.request().method())) {
        Optional<ResourcePath> atmProviderOptional = contentSettings.getResourcePaths().stream()
            .filter(p -> uri.startsWith(p.getPath())).findFirst();
        if (atmProviderOptional.isPresent()) {
          ResourcePath resourcePath = atmProviderOptional.get();
          //Future.succeededFuture(new JsonObject().put("hello", "world"));
          LOGGER.info("serving the request with path " + uri);

          //search over topic config
          KubePodLocator kubePodLocator = new KubePodLocator();
          String[] paths = uri.split("/");
          if (paths.length == 3) {
            //if () {
            LOGGER.info("set the size of replica to be " + paths[1]);
            kubePodLocator.scale(Integer.parseInt(paths[2]));
            //}
          }
          rc.response().setStatusCode(HttpResponseStatus.OK.code())
              .putHeader("Content-Type", "application/json")
              .end(ContentReponseUtil.toJsonString(resourcePath));

        } else {
          rc.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
              .end(ContentReponseUtil.toJsonString(contentSettings.getResourcePaths()));
        }
      } else {
        rc.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
      }
    };

  }


  public static Consumer<KafkaConsumerRecord<String, String>> recordConsumer = consumerRecord -> LOGGER
      .info(
          "processing key=" + consumerRecord.key() + " value=" + consumerRecord.value()
              + " partition=" + consumerRecord.partition()
              + " offset=" + consumerRecord.offset());
  public static Handler<RecordMetadata> metadataHandler = event -> LOGGER
      .info(
          "topic=" + event.getTopic() + " offset=" + event.getOffset()
              + " partition=" + event.getPartition());
}
