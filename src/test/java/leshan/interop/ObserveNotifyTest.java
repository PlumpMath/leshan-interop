package leshan.interop;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import leshan.ObserveSpec;
import leshan.ResponseCode;
import leshan.client.request.RegisterRequest;
import leshan.core.node.LwM2mNode;
import leshan.core.node.LwM2mPath;
import leshan.core.node.LwM2mResource;
import leshan.core.response.ClientResponse;
import leshan.core.response.ValueResponse;
import leshan.server.client.Client;
import leshan.server.observation.Observation;
import leshan.server.observation.ObservationRegistryListener;
import leshan.server.request.ObserveRequest;
import leshan.server.request.WriteAttributesRequest;

import org.junit.Test;

public class ObserveNotifyTest extends BaseTest {

    @Test
    public void LightweightM2M_1_0_int_501_Observation_and_notification_of_parameter_values() throws Exception {
        System.out.println("## LightweightM2M-1.0-int-501 – Observation and notification of parameter values");

        // client registration
        client.send(new RegisterRequest(clientEndpoint, new HashMap<String, String>()));

        final LwM2mPath observerRscPath = new LwM2mPath(4, 0, 2);

        // the server sends a request to write the firmware resource
        final Client regClient = server.getClientRegistry().get(clientEndpoint);
        ObserveSpec.Builder observeSpecBuilder = new ObserveSpec.Builder();
        observeSpecBuilder.maxPeriod(2); // every two seconds
        ClientResponse writeAttResponse = server.send(new WriteAttributesRequest(regClient, observerRscPath.toString(),
                observeSpecBuilder.build()));
        System.out.println("Response to write attribute request: " + writeAttResponse);

        assertEquals(ResponseCode.CHANGED, writeAttResponse.getCode());

        // listen for observation events
        final List<LwM2mNode> notifyValues = new ArrayList<LwM2mNode>();
        server.getObservationRegistry().addListener(new ObservationRegistryListener() {

            @Override
            public void newValue(Observation observation, LwM2mNode value) {
                System.out.println("New notification from client " + observation.getClient().getEndpoint() + ": "
                        + value);
                if (observation.getClient().equals(regClient)) {
                    notifyValues.add(value);
                }
            }

            @Override
            public void newObservation(Observation observation) {
                //
            }

            @Override
            public void cancelled(Observation observation) {
                //
            }

        });

        // the server sends a request to activate the reporting
        ValueResponse observeResponse = server.send(new ObserveRequest(regClient, observerRscPath.toString()));
        System.out.println("Response to observe request: code=" + observeResponse.getCode() + ", content="
                + observeResponse.getContent());

        assertEquals(ResponseCode.CONTENT, observeResponse.getCode());
        assertEquals(1, server.getObservationRegistry().getObservations(regClient).size());

        // wait a few seconds before checking the notifications
        Thread.sleep(5000);

        assertFalse(notifyValues.isEmpty());
        for (LwM2mNode value : notifyValues) {
            LwM2mResource resource = (LwM2mResource) value;
            assertEquals(2, resource.getId());
            int signalStrength = (Integer) resource.getValue().value;
            assertTrue(signalStrength <= -50 && signalStrength > -110);
        }

    }

    @Test
    public void LightweightM2M_1_0_int_502_Cancel_observations_using_Cancel_Observation_operation() throws Exception {
        System.out.println("## LightweightM2M-1.0-int-502 – Cancel observations using “Cancel Observation” operation");

        // client registration
        client.send(new RegisterRequest(clientEndpoint, new HashMap<String, String>()));

        final LwM2mPath observerRscPath = new LwM2mPath(4, 0, 2);

        // the server sends a request to write the firmware resource
        final Client regClient = server.getClientRegistry().get(clientEndpoint);
        ObserveSpec.Builder observeSpecBuilder = new ObserveSpec.Builder();
        observeSpecBuilder.maxPeriod(2); // every two seconds
        ClientResponse writeAttResponse = server.send(new WriteAttributesRequest(regClient, observerRscPath.toString(),
                observeSpecBuilder.build()));
        assertEquals(ResponseCode.CHANGED, writeAttResponse.getCode());

        // listen for observation events
        final List<LwM2mNode> notifyValues = new ArrayList<LwM2mNode>();
        server.getObservationRegistry().addListener(new ObservationRegistryListener() {

            @Override
            public void newValue(Observation observation, LwM2mNode value) {
                System.out.println("New notification from client " + observation.getClient().getEndpoint() + ": "
                        + value);
                notifyValues.add(value);
            }

            @Override
            public void newObservation(Observation observation) {
                //
            }

            @Override
            public void cancelled(Observation observation) {
                //
            }

        });

        // the server sends a request to activate the reporting
        server.send(new ObserveRequest(regClient, observerRscPath.toString()));

        // wait a few seconds before checking the notifications
        Thread.sleep(5000);

        assertFalse(notifyValues.isEmpty());

        // cancel the observation (COAP RESET)
        server.getObservationRegistry().cancelObservation(regClient, observerRscPath.toString());

        // wait a few seconds to be sure that notifications are not received anymore
        notifyValues.clear();
        Thread.sleep(5000);

        assertTrue(notifyValues.isEmpty());
        assertTrue(server.getObservationRegistry().getObservations(regClient).isEmpty());
    }

    @Test
    public void LightweightM2M_1_0_int_503_Cancel_observations_using_Write_Attributes_with_Cancel_parameter()
            throws Exception {
        System.out
                .println("## LightweightM2M-1.0-int-503 – Cancel observations using “Write Attributes” with Cancel parameter");

        // client registration
        client.send(new RegisterRequest(clientEndpoint, new HashMap<String, String>()));

        final LwM2mPath observerRscPath = new LwM2mPath(4, 0, 2);

        // the server sends a request to write the firmware resource
        final Client regClient = server.getClientRegistry().get(clientEndpoint);
        ObserveSpec.Builder observeSpecBuilder = new ObserveSpec.Builder();
        observeSpecBuilder.maxPeriod(2); // every two seconds
        ClientResponse writeAttResponse = server.send(new WriteAttributesRequest(regClient, observerRscPath.toString(),
                observeSpecBuilder.build()));
        assertEquals(ResponseCode.CHANGED, writeAttResponse.getCode());

        // listen for observation events
        final List<LwM2mNode> notifyValues = new ArrayList<LwM2mNode>();
        server.getObservationRegistry().addListener(new ObservationRegistryListener() {

            @Override
            public void newValue(Observation observation, LwM2mNode value) {
                System.out.println("New notification from client " + observation.getClient().getEndpoint() + ": "
                        + value);
                notifyValues.add(value);
            }

            @Override
            public void newObservation(Observation observation) {
                //
            }

            @Override
            public void cancelled(Observation observation) {
                //
            }

        });

        // the server sends a request to activate the reporting
        server.send(new ObserveRequest(regClient, observerRscPath.toString()));

        // wait a few seconds before checking the notifications
        Thread.sleep(5000);

        assertFalse(notifyValues.isEmpty());

        // cancel the observation (Write attribute request)
        observeSpecBuilder = new ObserveSpec.Builder();
        observeSpecBuilder.cancel();
        ClientResponse writeAttCancelResponse = server.send(new WriteAttributesRequest(regClient, observerRscPath
                .toString(), observeSpecBuilder.build()));
        System.out.println("Response to cancel request (write attribute): " + writeAttCancelResponse);
        assertEquals(ResponseCode.CHANGED, writeAttCancelResponse.getCode());

        // wait a few seconds to be sure that notifications are not received anymore
        notifyValues.clear();
        Thread.sleep(5000);

        assertTrue(notifyValues.isEmpty());
        assertTrue(server.getObservationRegistry().getObservations(regClient).isEmpty());
    }
}
