package greeting.server;

import java.io.File;
import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GreetingServerTls {
  public static void main(String[] args) throws IOException, InterruptedException {
    int port  = 50051;
    Server server = ServerBuilder.forPort(port)
        .useTransportSecurity(
            new File("ssl/server.crt"),
            new File("ssl/server.pem")
        )
        .addService(new GreetingServiceImpl())
        .build();
    server.start();
    System.out.println("Server Started");
    System.out.println("Listening on port: "+port);
    Runtime.getRuntime().addShutdownHook(new Thread(()->{
      System.out.println("server shutdown received");
      server.shutdown();
      System.out.println("server shutdown");
    }));
    server.awaitTermination();
  }
}
