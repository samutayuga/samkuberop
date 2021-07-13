package com.samutup.crd.content;

import com.samutup.crd.content.model.atwinSession;
import com.samutup.crd.content.model.ErrorMessage;
import com.samutup.crd.content.model.ResourcePath;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;

public class CRDFacade {

  private static final Logger LOGGER = LoggerFactory.getLogger(CRDFacade.class);

  KubernetesClient kubernetesClient;

  public CRDFacade(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public void create(Map atwinSession, ResourcePath resourcePath,
      RoutingContext routingContext) {
    try {

      CustomResourceDefinition crd = kubernetesClient.apiextensions().v1()
          .customResourceDefinitions()
          .load(this.getClass()
              .getResourceAsStream("/".concat(resourcePath.getId()).concat(".yaml")))
          .get();
      Map<String, Object> spec = crd.getSpec().getAdditionalProperties();
      LOGGER.info("existing CR additional properties " + spec);
      spec.put("replicaCount", atwinSession.get("replicaCount"));
      Object objCount = spec.get("replicaCount");
      if (Integer.parseInt((String) objCount) == Integer
          .parseInt((String) atwinSession.get("replicaCount"))) {
        crd = kubernetesClient.apiextensions().v1().customResourceDefinitions()
            .create(crd);
        LOGGER.info("Upgrade:=" + crd);
        routingContext.response().setStatusCode(HttpResponseStatus.CREATED.code()).end();
      } else {
        LOGGER.error("cannot update the object");
        routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
            .end(ContentReponseUtil
                .toJsonString(new ErrorMessage().setErrMessage("cannot update the object")));
      }

    } catch (Exception exception) {
      LOGGER.error("failed to upgrade ", exception);
      LOGGER.error("cannot update the object");
      routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
          .end(ContentReponseUtil
              .toJsonString(new ErrorMessage()
                  .setErrMessage("cannot update the object" + exception.getMessage())));
    }


  }

  public void get(ResourcePath rp, RoutingContext routingContext) {
    try {

      CustomResourceDefinition crd = kubernetesClient.apiextensions().v1()
          .customResourceDefinitions()
          .load(this.getClass()
              .getResourceAsStream("/".concat(rp.getId()).concat(".yaml")))
          .get();
      routingContext.response().setStatusCode(HttpResponseStatus.OK.code())
          .end(ContentReponseUtil.toJsonString(crd.toString()));

    } catch (Exception exception) {
      LOGGER.error("failed to upgrade ", exception);
      routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
          .end(ContentReponseUtil
              .toJsonString(new ErrorMessage().setErrMessage(exception.getMessage())));
    }


  }

}
