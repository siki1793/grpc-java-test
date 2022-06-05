package greeting.server;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.stub.StreamObserver;

public class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

  @Override
  public void greet(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
    responseObserver.onNext(
        GreetingResponse.newBuilder()
            .setResult("hello " + request.getFirstName())
            .build());
    responseObserver.onCompleted();
  }

  @Override
  public void greetManyTimes(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
    System.out.println("greetManyTimes");
    String firstName = request.getFirstName();
    try {
      for (int i = 0; i < 10; i++) {
        String result = "Hello " + firstName + ", response number: " + i;
        GreetingResponse response = GreetingResponse.newBuilder()
            .setResult(result)
            .build();

        responseObserver.onNext(response);
        Thread.sleep(1000L);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      responseObserver.onCompleted();
    }
  }

  @Override
  public StreamObserver<GreetingRequest> longGreet(
      StreamObserver<GreetingResponse> responseObserver) {
    StringBuilder sb = new StringBuilder();

    return new StreamObserver<GreetingRequest>() {
      @Override
      public void onNext(GreetingRequest request) {
        sb.append("Hello ");
        sb.append(request.getFirstName());
        sb.append("!\n");
      }

      @Override
      public void onError(Throwable t) {
        responseObserver.onError(t);
      }

      @Override
      public void onCompleted() {
        responseObserver.onNext(GreetingResponse.newBuilder()
            .setResult(sb.toString())
            .build());
        responseObserver.onCompleted();
      }
    };
  }

  @Override
  public StreamObserver<GreetingRequest> greetEveryone(StreamObserver<GreetingResponse> responseObserver) {
    return new StreamObserver<GreetingRequest>() {
      @Override
      public void onNext(GreetingRequest request) {
        responseObserver.onNext(GreetingResponse.newBuilder()
            .setResult("Hello " + request.getFirstName())
            .build());
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

}
