package de.metalcon.zmqworker;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.zeromq.ZMQ;

public class ZMQWorker {

    private static int NUM_WORKERS = 0;

    private static ZMQ.Context CONTEXT = ZMQ.context(1);

    private ZMQ.Socket socket;

    private String endpoint;

    private ZMQRequestHandler requestHandler;

    public ZMQWorker(
            String endpoint,
            ZMQRequestHandler requestHandler) {
        this.endpoint = endpoint;
        this.requestHandler = requestHandler;
        // TODO: ensure reply for each request
        socket = CONTEXT.socket(ZMQ.REP);
        socket.bind(endpoint);
        NUM_WORKERS += 1;
    }

    public void doWork() {
        System.out.println("i am serving on endpoint: " + endpoint);
        while (true) {
            byte[] serializedRequest = socket.recv();
            if (serializedRequest == null) {
                break;
            }

            Serializable response;
            Object request = SerializationUtils.deserialize(serializedRequest);
            if (request != null) {
                response = requestHandler.handleRequest(request);

                if (response == null) {
                    response = "ohoh";
                }
            } else {
                // TODO: deserialization failed
                response = "deserialization failed";
            }

            socket.send(SerializationUtils.serialize(response));
        }

        socket.close();
        NUM_WORKERS -= 1;
        if (NUM_WORKERS == 0) {
            CONTEXT.term();
        }
    }
}
