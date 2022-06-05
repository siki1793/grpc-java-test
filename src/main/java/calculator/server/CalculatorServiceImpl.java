package calculator.server;

import com.proto.calculator.AvgRequest;
import com.proto.calculator.AvgResponse;
import com.proto.calculator.CalculatorRequest;
import com.proto.calculator.CalculatorResponse;
import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.MaxRequest;
import com.proto.calculator.MaxResponse;
import com.proto.calculator.PrimeNumberRequest;
import com.proto.calculator.PrimeNumberResponse;
import com.proto.calculator.SqrtRequest;
import com.proto.calculator.SqrtResponse;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
  @Override
  public void sum(CalculatorRequest request, StreamObserver<CalculatorResponse> responseObserver) {
    responseObserver.onNext(CalculatorResponse.newBuilder()
        .setResult(request.getVal1() + request.getVal2())
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void prime(PrimeNumberRequest request,
      StreamObserver<PrimeNumberResponse> responseObserver) {
    long num = 2;
    long n = request.getNumber();
    try {
      while (n > 1) {
        if (n % num == 0) {
          responseObserver.onNext(PrimeNumberResponse.newBuilder()
              .setPrimeNumber(num)
              .build());
          n /= num;
        } else {
          num += 1;
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      responseObserver.onCompleted();
    }
  }

  @Override
  public StreamObserver<AvgRequest> avg(StreamObserver<AvgResponse> responseObserver) {
    return new StreamObserver<AvgRequest>() {
      int sum = 0;
      int count = 0;

      @Override
      public void onNext(AvgRequest request) {
        sum += request.getNumber();
        count++;
      }

      @Override
      public void onError(Throwable t) {
        responseObserver.onError(t);
      }

      @Override
      public void onCompleted() {
        responseObserver.onNext(AvgResponse.newBuilder()
            .setResult((double)sum / count)
            .build());
        responseObserver.onCompleted();
      }
    };
  }

  @Override
  public StreamObserver<MaxRequest> streamMax(StreamObserver<MaxResponse> responseObserver) {
    return new StreamObserver<MaxRequest>() {
      int maxSoFor = Integer.MIN_VALUE;
      @Override
      public void onNext(MaxRequest request) {
        maxSoFor = Math.max(request.getNumber(), maxSoFor);
        if(maxSoFor == request.getNumber()) {
          responseObserver.onNext(MaxResponse.newBuilder()
                  .setResult(maxSoFor)
              .build());
        }
      }

      @Override
      public void onError(Throwable t) {
        responseObserver.onError(t);
      }

      @Override
      public void onCompleted() {
        responseObserver.onCompleted();
      }
    };
  }

  @Override
  public void sqrt(SqrtRequest request, StreamObserver<SqrtResponse> responseObserver) {
    if (request.getNumber() < 0) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("Number should be greater than 0")
              .augmentDescription("number " + request.getNumber())
              .asRuntimeException());
      return;
    }
    responseObserver.onNext(SqrtResponse.newBuilder()
        .setResult(Math.sqrt(request.getNumber()))
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void sumWithDeadline(CalculatorRequest request, StreamObserver<CalculatorResponse> responseObserver) {
    Context context = Context.current();
    try{
      for(int i=0;i<10;i++) {
        if(context.isCancelled())
          return;
        Thread.sleep(1000);
      }
      responseObserver.onNext(CalculatorResponse.newBuilder()
          .setResult(request.getVal1() + request.getVal2())
          .build());
      responseObserver.onCompleted();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
