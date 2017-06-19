package store;

import com.codahale.metrics.SharedMetricRegistries;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.vertx.MetricsHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MetricVerticle extends AbstractVerticle {


    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.route("/metrics").handler(CorsHandler.create("*"));
        CollectorRegistry defaultRegistry = CollectorRegistry.defaultRegistry;
        defaultRegistry.register(new DropwizardExports(SharedMetricRegistries.getOrCreate("vertx-dw")));
        DefaultExports.initialize();
        new MetricCollector().register();

        MetricsService metricsService = MetricsService.create(vertx);

        router.route("/metrics").handler(new MetricsHandler());
        router.route("/admin/*").handler(StaticHandler.create("admin"));
        router.route("/dist/*").handler(StaticHandler.create("admin/dist"));

        HttpServer server = vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(8082);

        vertx.setPeriodic(2000, l -> {
            JsonObject snapshot = metricsService.getMetricsSnapshot(server);
            System.out.println(snapshot);
        });
    }
}
