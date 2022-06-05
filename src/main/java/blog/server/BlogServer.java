package blog.server;

import java.io.IOException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class BlogServer {
  public static void main(String[] args) throws IOException, InterruptedException {
    int port = 50051;
    MongoClient mongoClient = MongoClients.create("mongodb://root:root@localhost:27017/");
    Server server = ServerBuilder.forPort(port)
        .addService(new BlogServiceImpl(mongoClient))
        .build();
    server.start();
    System.out.println("Server Started");
    System.out.println("Listening on port: " + port);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("server shutdown received");
      server.shutdown();
      System.out.println("server shutdown");
    }));
    server.awaitTermination();
  }
}
