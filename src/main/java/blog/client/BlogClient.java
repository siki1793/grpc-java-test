package blog.client;

import java.util.Date;

import com.google.protobuf.Empty;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class BlogClient {
  public static void main(String[] args) {
    ManagedChannel channel = ManagedChannelBuilder
        .forAddress("localhost", 50051)
        .usePlaintext()
        .build();
    run(channel);
    System.out.println("shutting down");
    channel.shutdown();
  }

  private static void run(ManagedChannel channel) {
    BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);
    BlogId blogId = createBlog(stub);
    Blog blog = null;
    if (blogId != null) {
      System.out.println(blogId.getId());
      blog = readBlog(stub, blogId);
    }
    if (blog != null) {
      printBlogContent(blog);
    }
    BlogId updateBlogId = BlogId.newBuilder()
        .setId("629b74ea6e6df93ebb0b75da")
        .build();
    updateBlog(stub, updateBlogId);
    Blog updateBlog = null;
    if (updateBlogId != null) {
      System.out.println(updateBlogId.getId());
      updateBlog = readBlog(stub, updateBlogId);
    }
    if (updateBlog != null) {
      printBlogContent(updateBlog);
    }
    deleteBlog(stub, blogId);
//    blog = readBlog(stub, blogId);
    readAllBlogs(stub);
  }

  private static void readAllBlogs(BlogServiceGrpc.BlogServiceBlockingStub stub) {
    System.out.println("-----------------------------List Of Blogs-----------------------------");
    stub.listBlogs(Empty.getDefaultInstance()).forEachRemaining(blog -> printBlogContent(blog));
  }

  private static void deleteBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId blogId) {
    try {
      stub.deleteBlog(blogId);
    } catch (StatusRuntimeException ex) {
      System.out.println("Couldn't find the blog");
      ex.printStackTrace();
    }
  }

  private static void updateBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId blogId) {
    try {
      stub.updateBlog(Blog.newBuilder()
          .setId(blogId.getId())
          .setAuthor("Saikrishna")
          .setTitle("New Blog")
          .setContent("Hello this is new Blog create at " + (new Date()))
          .build());
    } catch (StatusRuntimeException ex) {
      System.out.println("Couldn't create the blog");
      ex.printStackTrace();
    }
  }

  private static void printBlogContent(Blog blog) {
    System.out.println(
        new StringBuilder().append("Title: ").append(blog.getTitle()).append("\t\t").append("Author: ").append(blog.getAuthor())
            .append("\n").append("Content: ").append(blog.getContent()).toString());
  }

  private static Blog readBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId blogId) {
    try {
      return stub.readBlog(blogId);
    } catch (StatusRuntimeException ex) {
      System.out.println("Couldn't find the blog");
      ex.printStackTrace();
      return null;
    }
  }

  private static BlogId createBlog(BlogServiceGrpc.BlogServiceBlockingStub stub) {
    try {
      return stub.createBlog(Blog.newBuilder()
          .setAuthor("Saikrishna")
          .setTitle("New Blog")
          .setContent("Hello this is new Blog create at " + (new Date()))
          .build());
    } catch (StatusRuntimeException ex) {
      System.out.println("Couldn't create the blog");
      ex.printStackTrace();
      return null;
    }
  }
}
