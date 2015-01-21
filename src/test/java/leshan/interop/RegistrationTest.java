package leshan.interop;

import static org.junit.Assert.*;

import java.util.HashMap;

import leshan.client.californium.impl.CaliforniumClientIdentifier;
import leshan.client.request.DeregisterRequest;
import leshan.client.request.RegisterRequest;
import leshan.client.response.OperationResponse;
import leshan.server.client.Client;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.junit.Test;

public class RegistrationTest extends BaseTest {

    @Test
    public void LightweightM2M_1_0_int_101_Initial_Registration() {
        System.out.println("## LightweightM2M-1.0-int-101 – Initial Registration");

        OperationResponse response = client.send(new RegisterRequest(clientEndpoint, new HashMap<String, String>()));
        System.out.println("Registration result: " + response);

        // verify registration on server side
        Client regClient = server.getClientRegistry().get(clientEndpoint);
        assertNotNull(regClient);
        System.out.println("Registered client: " + regClient);

        // verify the response received by the client
        assertEquals(ResponseCode.CREATED, response.getResponseCode());
        String location = ((CaliforniumClientIdentifier) response.getClientIdentifier()).getLocation().split("/")[2];
        assertEquals(regClient.getRegistrationId(), location);
    }

    @Test
    public void LightweightM2M_1_0_int_103_Deregistration() {
        System.out.println("## LightweightM2M-1.0-int-103 – Deregistration");

        // client registration
        OperationResponse regResponse = client.send(new RegisterRequest(clientEndpoint, new HashMap<String, String>()));

        Client regClient = server.getClientRegistry().get(clientEndpoint);
        assertNotNull(regClient);

        OperationResponse deregResponse = client.send(new DeregisterRequest(regResponse.getClientIdentifier()));
        System.out.println("Deregistration result: " + deregResponse);

        // verify the response received by the client
        assertEquals(ResponseCode.DELETED, deregResponse.getResponseCode());

        // verify the registration on server side
        regClient = server.getClientRegistry().get(clientEndpoint);
        assertNull(regClient);
    }

}
