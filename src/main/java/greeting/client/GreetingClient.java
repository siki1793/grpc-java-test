package greeting.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GreetingClient {
  public static void main(String[] args) throws InterruptedException {
    if (args.length == 0) {
      System.out.println("Need Arguments");
      return;
    }

    ManagedChannel channel = ManagedChannelBuilder
        .forAddress("localhost", 50051)
        .usePlaintext()
        .build();
    switch (args[0]) {
      case "greet": doGreet(channel); break;
      case "greetMany": doGreetManyTimes(channel); break;
      case "longGreet": doLongGreet(channel); break;
      case "greetEveryone": doGreetEveryone(channel); break;
      default:
        System.out.println("Keywors invalid: "+args[0]);
    }
    System.out.println("shutting down");
    channel.shutdown();
  }

  private static void doGreetEveryone(ManagedChannel channel) throws InterruptedException {
    GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(
        channel);
    CountDownLatch latch = new CountDownLatch(1);
    System.out.println("Enter doGreetEveryone");
    List<String> names = new ArrayList<>();
    Collections.addAll(names, "Saikrishna", "Sai", "Krishna");
    StreamObserver<GreetingRequest> stream = stub.greetEveryone(
        new StreamObserver<GreetingResponse>() {
          @Override
          public void onNext(GreetingResponse response) {
            System.out.println(response.getResult());
          }

          @Override
          public void onError(Throwable t) {

          }

          @Override
          public void onCompleted() {
            latch.countDown();
          }
        });
    for(String n: names) {
      stream.onNext(GreetingRequest.newBuilder()
          .setFirstName(n)
          .build());
    }
    stream.onCompleted();
    latch.await(3, TimeUnit.SECONDS);
  }

  private static void doLongGreet(ManagedChannel channel) throws InterruptedException {
    GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(
        channel);
    System.out.println("Enter doLongGreet");
    List<String> names = new ArrayList<>();
    CountDownLatch latch = new CountDownLatch(1);
    Collections.addAll(names, "Saikrishna", "Sai", "Krishna");
    StreamObserver<GreetingRequest> stream = stub.longGreet(
        new StreamObserver<GreetingResponse>() {
          @Override
          public void onNext(GreetingResponse response) {
            System.out.println(response.getResult());
          }

          @Override
          public void onError(Throwable t) {

          }

          @Override
          public void onCompleted() {
            latch.countDown();
          }
        });
    for(String n: names) {
      stream.onNext(GreetingRequest.newBuilder()
              .setFirstName(n)
          .build());
    }
    stream.onCompleted();
    latch.await(3, TimeUnit.SECONDS);
  }

  private static void doGreetManyTimes(ManagedChannel channel) {
    System.out.println("Enter doGreetManyTimes");
    GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(
        channel);
    stub.greetManyTimes(
            GreetingRequest.newBuilder().setFirstName("Saikrishna").build())
        .forEachRemaining(greetingResponse -> System.out.println(greetingResponse.getResult()));
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
