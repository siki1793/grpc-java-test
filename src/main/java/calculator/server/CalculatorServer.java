package calculator.server;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

public class CalculatorServer {
  public static void main(String[] args) throws IOException, InterruptedException {
    int port = 50061;
    Server server = ServerBuilder
        .forPort(port)
        .addService(new CalculatorServiceImpl())
        .addService(ProtoReflectionService.newInstance())
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
