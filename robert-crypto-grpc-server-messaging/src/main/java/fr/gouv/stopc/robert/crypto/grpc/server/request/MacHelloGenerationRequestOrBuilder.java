// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: crypto_service.proto

package fr.gouv.stopc.robert.crypto.grpc.server.request;

public interface MacHelloGenerationRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:robert.server.crypto.MacHelloGenerationRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * byte[] KA;
   * byte[] helloMessage;
   * </pre>
   *
   * <code>bytes ka = 1;</code>
   * @return The ka.
   */
  com.google.protobuf.ByteString getKa();

  /**
   * <code>bytes helloMessage = 2;</code>
   * @return The helloMessage.
   */
  com.google.protobuf.ByteString getHelloMessage();
}
