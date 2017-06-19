package store;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;

public class AppV2 extends AbstractVerticle {

    private Database database;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(AppV2.class.getName());
    }
    
    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        // REST API
        router.get("/products").handler(this::getProducts);
        router.route().handler(BodyHandler.create());
        router.post("/products").handler(this::addProduct);

        router.get("/assets/*").handler(StaticHandler.create());
        
        Database.initialize(vertx)
            .flatMap(db -> {
                database = db;
                return vertx.createHttpServer()
                    .requestHandler(router::accept)
                    .rxListen(8080);
            }).subscribe();
    }

    private void getProducts(RoutingContext rc) {
        HttpServerResponse response = rc.response().setChunked(true);
        database.retrieve()
            .subscribe(
                p -> response.write(Json.encode(p) + " \n\n"),
                rc::fail,
                response::end);
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
