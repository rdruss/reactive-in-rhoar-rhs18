package store;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import rx.Single;


/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class AppV7 extends AbstractVerticle {
    
    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions()
                .setClusterHost("127.0.0.1")
                .setMetricsOptions(
                    new DropwizardMetricsOptions()
                        .setJmxEnabled(true)
                        .setRegistryName("vertx-dw")
                ),
            ar -> {
                Vertx vertx = ar.result();
                vertx.deployVerticle(AppV6.class.getName());
                vertx.deployVerticle(MetricVerticle.class.getName());
            });
    }
}
