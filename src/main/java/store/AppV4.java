package store;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.servicediscovery.ServiceDiscovery;
import io.vertx.rxjava.servicediscovery.types.HttpEndpoint;
import rx.Single;


public class AppV4 extends AbstractVerticle {

    private Database database;

    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions().setClusterHost("127.0.0.1"), ar -> {
            Vertx vertx = ar.result();
            vertx.deployVerticle(AppV4.class.getName());
        });
    }

    private WebClient pricer;

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        // REST API
        router.get("/products").handler(this::getProducts);
        router.route().handler(BodyHandler.create());
        router.post("/products").handler(this::addProduct);

        router.get("/assets/*").handler(StaticHandler.create());

        ServiceDiscovery.create(vertx, discovery -> {
            Single<WebClient> pricer = HttpEndpoint
                .rxGetWebClient(discovery, svc -> svc.getName().equals("pricer"));
            pricer.flatMap(cl -> {
                this.pricer = cl;
                return Database.initialize(vertx);
            }).flatMap(db -> {
                database = db;
                return vertx.createHttpServer()
                    .requestHandler(router::accept)
                    .rxListen(8080);
            }).subscribe();
        });
    }

    private void getProducts(RoutingContext rc) {
        HttpServerResponse response = rc.response().setChunked(true);
        database.retrieve()
            .flatMapSingle(this::getPriceForProduct)
            .subscribe(
                p -> response.write(Json.encode(p) + " \n\n"),
                rc::fail,
                response::end);
    }

    private Single<Product> getPriceForProduct(Product p) {
        return pricer.get("/prices/" + p.getName())
            .rxSend()
            .map(HttpResponse::bodyAsJsonObject)
            .map(json -> p.setPrice(json.getDouble("price")));
    }

    private void addProduct(RoutingContext rc) {
        JsonObject body = rc.getBodyAsJson();
        String name = body.getString("name");
        database.insert(name)
            .subscribe(
                p -> {
                    String json = Json.encode(p);
                    rc.response().setStatusCode(201).end(json);
                },
                rc::fail);
    }
}
