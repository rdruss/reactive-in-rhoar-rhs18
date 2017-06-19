package store;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.ArrayList;
import java.util.List;

public class AppV1 extends AbstractVerticle {

    private List<Product> products = new ArrayList<>();


//    public static void main(String[] args) {
//        Vertx vertx = Vertx.vertx();
//        vertx.deployVerticle(AppV1.class.getName());
//    }


//    @Override
//    public void start() throws Exception {
//        products.add(new Product("coffee", 1));
//        products.add(new Product("drinks", 2));
//
//        Router router = Router.router(vertx);
//
//        // REST API
//        router.get("/products").handler(this::getProducts);
//        router.route().handler(BodyHandler.create());
//        router.post("/products").handler(this::addProduct);
//
//        router.get("/assets/*").handler(StaticHandler.create());
//
//        vertx.createHttpServer()
//            .requestHandler(router::accept)
//            .listen(8080);
//    }

    private void getProducts(RoutingContext rc) {
        rc.response().end(Json.encode(products));
    }

    private void addProduct(RoutingContext rc) {
        JsonObject json = rc.getBodyAsJson();
        Product product = new Product(json.getString("name"), json.getInteger("id"));
        products.add(product);
        getProducts(rc);
    }
}
