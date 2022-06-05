package blog.server;

import com.google.protobuf.Empty;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

  private final MongoCollection<Document> mongoCollection;

  public BlogServiceImpl(MongoClient mongoClient) {
    MongoDatabase db = mongoClient.getDatabase("blogdb");
    mongoCollection = db.getCollection("blog");
  }

  @Override
  public void createBlog(Blog request, StreamObserver<BlogId> responseObserver) {
    Document doc = new Document("author", request.getAuthor())
        .append("title", request.getTitle())
        .append("content", request.getContent());
    InsertOneResult result;
    try {
      result = mongoCollection.insertOne(doc);
    } catch (MongoException ex) {
      responseObserver.onError(Status.INVALID_ARGUMENT
          .withDescription(ex.getLocalizedMessage())
          .asRuntimeException());
      return;
    }
    if (!result.wasAcknowledged() || result.getInsertedId() == null) {
      responseObserver.onError(Status.INTERNAL
          .withDescription("Blog couldn't be created")
          .asRuntimeException());
      return;
    }
    String id = result.getInsertedId().asObjectId().getValue().toString();
    responseObserver.onNext(BlogId.newBuilder()
        .setId(id)
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void readBlog(BlogId request, StreamObserver<Blog> responseObserver) {
    if (request.getId().isEmpty()) {
      responseObserver.onError(Status.INVALID_ARGUMENT
          .withDescription("Blog id cannot be empty")
          .asRuntimeException());
      return;
    }
    System.out.println("readId: " + request.getId());
    Document doc = mongoCollection.find(eq("_id", new ObjectId(request.getId()))).first();
    if (doc == null) {
      responseObserver.onError(Status.NOT_FOUND
          .withDescription("Blog was not found")
          .augmentDescription("Blog Id:" + request.getId())
          .asRuntimeException());
      return;
    }
    responseObserver.onNext(Blog.newBuilder()
        .setAuthor(doc.getString("author"))
        .setTitle(doc.getString("title"))
        .setContent(doc.getString("content"))
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void updateBlog(Blog request, StreamObserver<Empty> responseObserver) {
    if (request.getId().isEmpty()) {
      responseObserver.onError(Status.INVALID_ARGUMENT
          .withDescription("Blog id cannot be empty")
          .asRuntimeException());
      return;
    }
    System.out.println("updateId: " + request.getId());
    Document doc = mongoCollection.findOneAndUpdate(eq("_id", new ObjectId(request.getId())),
        combine(
            set("author", request.getAuthor()),
            set("title", request.getTitle()),
            set("content", request.getContent())
        ));
    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public void deleteBlog(BlogId request, StreamObserver<Empty> responseObserver) {
    if (request.getId().isEmpty()) {
      responseObserver.onError(Status.INVALID_ARGUMENT
          .withDescription("Blog id cannot be empty")
          .asRuntimeException());
      return;
    }
    mongoCollection.deleteOne(eq("_id", new ObjectId(request.getId())));
    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public void listBlogs(Empty request, StreamObserver<Blog> responseObserver) {
    for(Document doc: mongoCollection.find()) {
      responseObserver.onNext(Blog.newBuilder()
          .setAuthor(doc.getString("author"))
          .setTitle(doc.getString("title"))
          .setContent(doc.getString("content"))
          .build());
    }
    responseObserver.onCompleted();
  }
}
