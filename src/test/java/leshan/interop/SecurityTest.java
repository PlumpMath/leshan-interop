package leshan.interop;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.UUID;

import leshan.ResponseCode;
import leshan.client.californium.LeshanClient;
import leshan.client.request.RegisterRequest;
import leshan.core.node.LwM2mResource;
import leshan.core.response.ValueResponse;
import leshan.server.LwM2mServer;
import leshan.server.californium.LeshanServerBuilder;
import leshan.server.client.Client;
import leshan.server.request.ReadRequest;
import leshan.server.security.NonUniqueSecurityInfoException;
import leshan.server.security.SecurityInfo;
import leshan.util.RandomStringUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SecurityTest {

    private LwM2mServer server;

    private LeshanClient client;
    private String clientEndpoint;

    @Before
    public void start() throws NonUniqueSecurityInfoException {
        // start the server
        LeshanServerBuilder serverBuilder = new LeshanServerBuilder();
        server = serverBuilder.build();
        server.start();
        System.out.println("Server started");

        clientEndpoint = UUID.randomUUID().toString();

        // store PSK security info for the client
        String identity = RandomStringUtils.randomAlphanumeric(16);
        byte[] preSharedKey = RandomStringUtils.random(16).getBytes();
        server.getSecurityRegistry().add(SecurityInfo.newPreSharedKeyInfo(clientEndpoint, identity, preSharedKey));

        TestClientBuilder clientBuilder = new TestClientBuilder();
        clientBuilder.addDeviceObject();
        clientBuilder.setSecure(identity, preSharedKey);
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

    @Test
    public void LightweightM2M_1_0_int_601_UDP_Channel_Security_Pre_shared_Key_Mode() {
        System.out.println("## LightweightM2M-1.0-int-601 – UDP Channel Security – Pre-shared Key Mode");

        // client registration
        client.send(new RegisterRequest(clientEndpoint, new HashMap<String, String>()));

        // send a request to read the manufacturer model
        Client client = server.getClientRegistry().get(clientEndpoint);
        ValueResponse response = server.send(new ReadRequest(client, 3, 0, 0));
        System.out.println("Received response: code=" + response.getCode() + ", content=" + response.getContent());

        // verify the result
        assertEquals(ResponseCode.CONTENT, response.getCode());

        LwM2mResource resource = (LwM2mResource) response.getContent();
        assertEquals(0, resource.getId());
        assertEquals(TestClientBuilder.MANUFACTURER_MODEL, resource.getValue().value);
    }

}
