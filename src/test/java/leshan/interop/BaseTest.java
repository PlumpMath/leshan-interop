package leshan.interop;

import java.util.UUID;

import leshan.client.californium.LeshanClient;
import leshan.server.LwM2mServer;
import leshan.server.californium.LeshanServerBuilder;

import org.junit.After;
import org.junit.Before;

public class BaseTest {

    protected LwM2mServer server;

    protected LeshanClient client;
    protected String clientEndpoint;

    @Before
    public void start() {
        // start the server
        LeshanServerBuilder serverBuilder = new LeshanServerBuilder();
        server = serverBuilder.build();
        server.start();
        System.out.println("Server started");

        clientEndpoint = UUID.randomUUID().toString();

        TestClientBuilder clientBuilder = new TestClientBuilder();
        clientBuilder.addServerObject();
        clientBuilder.addDeviceObject();
        client = clientBuilder.build();
        client.start();
        System.out.println("Client started");
    }

    @After
    public void stop() {
        client.stop();
        System.out.println("Client stopped");
        server.stop();
        System.out.println("Server stopped");
    }

}
