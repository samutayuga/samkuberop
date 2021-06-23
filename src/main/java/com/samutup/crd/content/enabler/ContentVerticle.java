package com.samutup.crd.content.enabler;

import static com.samutup.crd.content.ContentReponseUtil.handleRequest;

import com.samutup.crd.content.CRDFacade;
import com.samutup.crd.content.config.ContentConstant;
import com.samutup.crd.content.config.ContentSettings;
import com.samutup.crd.content.liveready.AppCheckHandler;
import com.samutup.crd.content.liveready.LifenessReadinessCheck;
import com.samutup.crd.content.liveready.ServerStartupListener;
import com.samutup.crd.content.liveready.ServerStartupListenerImpl;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.stream.Collectors;

/**
 * this is the class that acts as entry point from the client request
 */
public class ContentVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentVerticle.class);

  /**
   * get kubernetes client
   * @return
   */
  private KubernetesClient getKubernetesClient() {
    Config config = Config.autoConfigure(null);
    LOGGER.info("All environment variables " + System.getenv());
    KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
    return kubernetesClient;
  }


  /**
   * Start the verticle.<p> This is called by Vert.x when the verticle instance is deployed. Don't
   * call it yourself.<p> If your verticle does things in its startup which take some time then you
   * can override this method and call the startFuture some time later when start up is complete.
   *
   * @param startPromise a promise which should be called when verticle start-up is complete.
   */
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);

    try {
      ContentSettings contentSettings = config().mapTo(ContentSettings.class);
      int portNumber = contentSettings.getEngineSettings().getHttpport();
      //list all path
      vertx.getOrCreateContext()
          .put(ContentConstant.SERVER_CONFIG.configVal, contentSettings.getResourcePaths());

      ServerStartupListener serverStartupListenHandler = new ServerStartupListenerImpl(startPromise,
          portNumber, contentSettings);
      // register readiness and liveness check
      //new AppCheckHandler[]{serverStartupListenHandler}
      LifenessReadinessCheck
          .registerReadinessCheck(router, new AppCheckHandler[]{serverStartupListenHandler});
      LifenessReadinessCheck.registerLivenessCheck(router, null);
      CRDFacade crdFacade = new CRDFacade(getKubernetesClient());
      contentSettings.getResourcePaths()
          .forEach(js -> router.route().handler(BodyHandler.create()).blockingHandler(
              handleRequest(contentSettings, crdFacade)));
      // create server
      HttpServer server = vertx.createHttpServer();
      server.requestHandler(router).listen(portNumber, serverStartupListenHandler);
    } catch (Exception exception) {
      LOGGER.error("Unexpected error, config " + config(), exception);
    }
  }


}
