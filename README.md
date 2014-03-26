# zmqWorker [WIP]

ZeroMQ worker for backend components

## Description

The ZeroMQ worker is a standalone worker for backend components that use ZeroMQ.
It takes care of deserialization and serialization of request and response objects.
Go to our [ZeroMQ page](../../../main/wiki/techZeroMQ) for more information concerning the flow here.

## Dependency

    <dependency>
      <groupId>de.metalcon</groupId>
      <artifactId>zmq-worker</artifactId>
      <version>0.1.0</version>
    </dependency>

# Usage

At first you have to create your request handler that implements `ZeroMQRequestHandler`.
In the only method this interface promises, you get a deserialized object derived from `Request`, defined in the [backend API](../../../backendApi).
Check the request type via `instanceof` and do your magic.
Finally you return a response object derived from `Response` that will be sent to the client automatically.

In the main method of your component you create a new worker instance and start it by calling the `start`-method.
The worker stops if a `ShutdownRequest`, defined in the basic API, is received.
