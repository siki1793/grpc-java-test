package greeting.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import io.grpc.stub.StreamObserver;

public class GreetingClientTls {
  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println("Need Arguments");
      return;
    }

    ChannelCredentials creds = TlsChannelCredentials.newBuilder()
        .trustManager(new File("ssl/ca.crt"))
        .build();
    ManagedChannel channel = Grpc
        .newChannelBuilderForAddress("localhost", 50051, creds)
        .build();
    switch (args[0]) {
      case "greet":
        doGreet(channel);
        break;
      default:
        System.out.println("Keywors invalid: " + args[0]);
    }
    System.out.println("shutting down");
    channel.shutdown();
  }

  private static void doGreet(ManagedChannel channel) {
    System.out.println("Enter doGreat");
    GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(
        channel);
    GreetingResponse response = stub.greet(
        GreetingRequest.newBuilder().setFirstName("Saikrishna").build());
    System.out.println("Greeting: " + response.getResult());
  }
}
