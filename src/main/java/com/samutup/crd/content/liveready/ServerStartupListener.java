package com.samutup.crd.content.liveready;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;

public interface ServerStartupListener extends AppCheckHandler, Handler<AsyncResult<HttpServer>> {

}
