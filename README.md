# zmqWorker [WIP]

ZeroMQ worker for backend components  

## Description

The ZeroMQ worker is a standalone worker for backend components that use ZeroMQ.  
It takes care of deserialization and serialization of request and response objects.  
Go to our [ZeroMQ page](/main/wiki/techZeroMQ) for more information concerning the flow here.

## Usage

At first you have to create your request handler that implements `ZeroMQRequestHandler`.  
In the only method this interface promises, you get a deserialized request object.  
Check the request type via `instanceof` and do your magic.  
Finally you return a `Serializable` response object that will be sent to the client automatically.
