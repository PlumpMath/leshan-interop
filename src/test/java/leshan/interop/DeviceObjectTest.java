package leshan.interop;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import leshan.ResponseCode;
import leshan.client.request.RegisterRequest;
import leshan.core.node.LwM2mObjectInstance;
import leshan.core.node.LwM2mResource;
import leshan.core.response.ClientResponse;
import leshan.core.response.ValueResponse;
import leshan.server.client.Client;
import leshan.server.request.ExecuteRequest;
import leshan.server.request.ReadRequest;

import org.junit.Test;

public class DeviceObjectTest extends BaseTest {

    @Test
    public void LightweightM2M_1_0_int_201_Querying_basic_information_from_the_client_in_TLV_format() {
        System.out.println("## LightweightM2M-1.0-int-201 – Querying basic information from the client in TLV format");

        // client registration
        client.send(new RegisterRequest(clientEndpoint, new HashMap<String, String>()));

        // send read request for device object
        Client client = server.getClientRegistry().get(clientEndpoint);
        ValueResponse response = server.send(new ReadRequest(client, 3, 0));
        System.out.println("Received response: code=" + response.getCode() + ", content=" + response.getContent());

        // verify the result
        assertEquals(ResponseCode.CONTENT, response.getCode());

        LwM2mObjectInstance instance = (LwM2mObjectInstance) response.getContent();
        assertEquals(0, instance.getId());
        assertEquals(TestClientBuilder.MANUFACTURER_MODEL, instance.getResources().get(0).getValue().value);
        assertEquals(TestClientBuilder.MODEL_NUMBER, instance.getResources().get(1).getValue().value);
        assertEquals(TestClientBuilder.SERIAL_NUMBER, instance.getResources().get(2).getValue().value);
    }

    @Test
    public void LightweightM2M_1_0_int_202_Querying_the_firmware_version_from_the_client() {
        System.out.println("## LightweightM2M-1.0-int-202 – Querying the firmware version from the client");

        // client registration
        client.send(new RegisterRequest(clientEndpoint, new HashMap<String, String>()));

        // send read request for device object
        Client client = server.getClientRegistry().get(clientEndpoint);
        ValueResponse response = server.send(new ReadRequest(client, 3, 0, 3));
        System.out.println("Received response: code=" + response.getCode() + ", content=" + response.getContent());

        // verify the result
        assertEquals(ResponseCode.CONTENT, response.getCode());

        LwM2mResource resource = (LwM2mResource) response.getContent();
        assertEquals(3, resource.getId());
        assertEquals(TestClientBuilder.FIRMWARE_VERSION, resource.getValue().value);
    }

    @Test
    public void LightweightM2M_1_0_int_203_Rebooting_the_device() {
        System.out.println("##  LightweightM2M-1.0-int-203 – Rebooting the device");

        // client registration
        client.send(new RegisterRequest(clientEndpoint, new HashMap<String, String>()));

        // send reboot request
        Client regClient = server.getClientRegistry().get(clientEndpoint);
        ClientResponse response = server.send(new ExecuteRequest(regClient, 3, 0, 4));
        System.out.println("Response to reboot request: " + response);

        assertEquals(ResponseCode.CHANGED, response.getCode());

        // TODO client re-registration?
    }

}
