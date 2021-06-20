package com.samutup.crd.content;

import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import java.util.Map;
import java.util.stream.Collectors;

public class KubePodLocator {

  private static final Logger LOGGER = LoggerFactory.getLogger(KubePodLocator.class);

  public void scale(int replicaCount) {
    KubernetesClient kubernetesClient = new DefaultKubernetesClient();
    NamespaceList namespaceList = kubernetesClient.namespaces().list();
    String allNs = namespaceList.getItems().stream().map(p -> p.getMetadata().getName())
        .collect(Collectors.joining());
    LOGGER.info(allNs);
    CustomResourceDefinitionContext customResourceDefinitionContext = new CustomResourceDefinitionContext
        .Builder()
        .withGroup("charts.samutup.com")
        .withVersion("v1alpha1")
        .withScope("Namespaced")
        .withPlural("minimarts")
        .build();

    // Map<String, Object> objectMap = kubernetesClient.customResource(customResourceDefinitionContext)
    //    .get();
    //LOGGER.info(objectMap);

    //do business logic to replace the value.
    //eg. increase the replica
    //LOGGER.info(stringObjectMap);
    try {
      Map<String, Object> stringObjectMap = kubernetesClient
          .customResource(customResourceDefinitionContext)
          .load(this.getClass().getResourceAsStream("/sample_minimart.yaml"));
      Object specMaps = stringObjectMap.get("spec");
      if (specMaps instanceof Map) {
        ((Map<String, Object>) specMaps).put("replicaCount", replicaCount);
      }
      Object objCount = ((Map<String, Object>) stringObjectMap.get("spec")).get("replicaCount");
      if ((Integer) objCount == replicaCount) {
        Map<String, Object> results = kubernetesClient
            .customResource(customResourceDefinitionContext)
            .createOrReplace(stringObjectMap);
        LOGGER.info("Upgrade:=" + results);
      } else {
        LOGGER.error("cannot update the object");
      }

    } catch (Exception exception) {
      LOGGER.error("failed to upgrade ", exception);
    }


  }


}
