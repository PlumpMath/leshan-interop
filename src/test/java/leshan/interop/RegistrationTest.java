package leshan.interop;

import static org.junit.Assert.*;

import java.util.HashMap;

import leshan.client.californium.impl.CaliforniumClientIdentifier;
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
        Client client = server.getClientRegistry().get(clientEndpoint);
        assertNotNull(client);
        System.out.println("Registered client: " + client);

        // verify the response received by the client
        assertEquals(ResponseCode.CREATED, response.getResponseCode());
        String location = ((CaliforniumClientIdentifier) response.getClientIdentifier()).getLocation().split("/")[2];
        assertEquals(client.getRegistrationId(), location);
    }

}
