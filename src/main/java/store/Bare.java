package store;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class Bare {

    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions()
            .setHAEnabled(true)
            .setClusterHost("127.0.0.1"), ar -> {
            System.out.println("Bare ready");
        });
    }
}
