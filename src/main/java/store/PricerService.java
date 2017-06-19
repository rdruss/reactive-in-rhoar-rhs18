package store;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PricerService extends AbstractVerticle {


    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions()
            .setHAEnabled(true)
            .setClusterHost("127.0.0.1"), ar -> {
            Vertx vertx = ar.result();
            vertx.deployVerticle(PricerService.class.getName(),
                new DeploymentOptions().setHa(true));
        });
    }

    private Map<String, Double> prices = new HashMap<>();
    private Random random = new Random();
    private ServiceDiscovery discovery;
    private Record record;

    @Override
    public void start() throws Exception {
        ServiceDiscovery.create(vertx, discovery -> {
            this.discovery = discovery;
            discovery.publish(HttpEndpoint.createRecord("pricer", "localhost", 8081, "/"),
                ar -> record = ar.result());
        });
        
        Router router = Router.router(vertx);
        router.get("/prices/:name").handler(rc -> {
            String name = rc.pathParam("name");
            JsonObject productWithPrice = getProductWithPrice(name);
            rc.response().end(productWithPrice.encode());
        });

        vertx.eventBus().<JsonObject>consumer("pricer", msg -> {
            JsonObject body = msg.body();
            String name = body.getString("name");
            JsonObject productWithPrice = getProductWithPrice(name);
            msg.reply(productWithPrice);
        });

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(8081);
    }

    private JsonObject getProductWithPrice(String name) {
        Double price = prices
            .computeIfAbsent(name,
                k -> (double) random.nextInt(50));
        return new JsonObject().put("name", name)
            .put("price", price);
    }

    @Override
    public void stop(Future<Void> done) throws Exception {
        if (discovery != null && record != null) {
            discovery.unpublish(record.getRegistration(), v -> done.complete());
        }
    }
}
