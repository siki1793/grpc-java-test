package calculator.client;

import java.rmi.StubNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.proto.calculator.AvgRequest;
import com.proto.calculator.AvgResponse;
import com.proto.calculator.CalculatorRequest;
import com.proto.calculator.CalculatorResponse;
import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.MaxRequest;
import com.proto.calculator.MaxResponse;
import com.proto.calculator.PrimeNumberRequest;
import com.proto.calculator.SqrtRequest;
import com.proto.calculator.SqrtResponse;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class CalculatorClient {
  public static void main(String[] args) throws InterruptedException {
    ManagedChannel channel = ManagedChannelBuilder
        .forAddress("localhost", 50061)
        .usePlaintext()
        .build();
//    doSum(channel,3,10);
//    doPrime(channel, 120);
//    doAvg(channel, Arrays.asList(1,2,3,4));
//    doMax(channel, Arrays.asList(1,5,3,6,2,20));
//    doSqrt(channel);
    doSumWithDeadline(channel);
    channel.shutdown();
  }

  private static void doSumWithDeadline(ManagedChannel channel) {
    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub
        = CalculatorServiceGrpc.newBlockingStub(channel);
    CalculatorResponse response = stub.withDeadline(Deadline.after(11, TimeUnit.SECONDS))
        .sumWithDeadline(CalculatorRequest.newBuilder()
            .setVal1(10)
            .setVal2(20)
            .build());
    System.out.println("Result within Deadline: " + response.getResult());
    try {
      response = stub.withDeadline(Deadline.after(3, TimeUnit.SECONDS))
          .sumWithDeadline(CalculatorRequest.newBuilder()
              .setVal1(20)
              .setVal2(20)
              .build());
      System.out.println(response.getResult());
    } catch (StatusRuntimeException ex) {
      if (ex.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
        System.out.println("Deadline has been exceeded");
      } else {
        System.out.println("got the exception in doSumWithDeadline");
        ex.printStackTrace();
      }
    }
  }

  private static void doSqrt(ManagedChannel channel) {
    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
    SqrtResponse response = stub.sqrt(SqrtRequest.newBuilder().setNumber(144).build());
    System.out.println(response.getResult());
    try {
      SqrtResponse negResponse = stub.sqrt(SqrtRequest.newBuilder().setNumber(-1).build());
      System.out.println(negResponse.getResult());
    } catch (Exception ex) {

      ex.printStackTrace();
    }

  }

  private static void doMax(ManagedChannel channel, List<Integer> numbers)
      throws InterruptedException {
    CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
    CountDownLatch latch = new CountDownLatch(1);
    StreamObserver<MaxRequest> stream = stub.streamMax(new StreamObserver<MaxResponse>() {
      @Override
      public void onNext(MaxResponse response) {
        System.out.println(response.getResult());
      }

      @Override
      public void onError(Throwable t) {}

      @Override
      public void onCompleted() {
        latch.countDown();
      }
    });
    for (int n : numbers) {
      stream.onNext(MaxRequest.newBuilder()
          .setNumber(n)
          .build());
    }
    stream.onCompleted();
    latch.await(3, TimeUnit.SECONDS);
  }

  private static void doAvg(ManagedChannel channel, List<Integer> numbers)
      throws InterruptedException {
    CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
    CountDownLatch latch = new CountDownLatch(1);
    StreamObserver<AvgRequest> stream = stub.avg(new StreamObserver<AvgResponse>() {
      @Override
      public void onNext(AvgResponse response) {
        System.out.println(response.getResult());
      }

      @Override
      public void onError(Throwable t) {}

      @Override
      public void onCompleted() {
        latch.countDown();
      }
    });
    for (int n : numbers) {
      stream.onNext(AvgRequest.newBuilder()
          .setNumber(n)
          .build());
    }
    stream.onCompleted();
    latch.await(3, TimeUnit.SECONDS);
  }

  private static void doPrime(ManagedChannel channel, long number) {
    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub
        = CalculatorServiceGrpc.newBlockingStub(channel);
    stub.prime(PrimeNumberRequest.newBuilder()
            .setNumber(number)
            .build())
        .forEachRemaining(primeNumberResponse -> System.out.println(
            "Prime Number :" + primeNumberResponse.getPrimeNumber()));
  }

  private static void doSum(ManagedChannel channel, int a, int b) {
    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
    CalculatorResponse response = stub.sum(CalculatorRequest.newBuilder()
            .setVal1(a)
            .setVal2(b)
        .build());
    System.out.println("Result : " + response.getResult());
  }
}
