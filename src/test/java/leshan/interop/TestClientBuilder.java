package leshan.interop;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leshan.client.californium.LeshanClient;
import leshan.client.resource.LwM2mClientObjectDefinition;
import leshan.client.resource.LwM2mClientResourceDefinition;
import leshan.client.resource.SingleResourceDefinition;
import leshan.client.resource.integer.IntegerLwM2mExchange;
import leshan.client.resource.integer.IntegerLwM2mResource;
import leshan.client.resource.string.StringLwM2mExchange;
import leshan.client.resource.string.StringLwM2mResource;
import leshan.server.californium.LeshanServerBuilder;

public class TestClientBuilder {

    private Map<Integer, LwM2mClientObjectDefinition> objectDefs = new HashMap<Integer, LwM2mClientObjectDefinition>();

    public void addServerObject() {
        if (!objectDefs.containsKey(1)) {
            LwM2mClientObjectDefinition def = this.buildServerObjectDefinition();
            objectDefs.put(1, def);
        }
    }

    public void addDeviceObject() {
        if (!objectDefs.containsKey(3)) {
            LwM2mClientObjectDefinition def = this.buildDeviceObjectDefinition();
            objectDefs.put(3, def);
        }
    }

    public LeshanClient build() {
        return new LeshanClient(new InetSocketAddress(0), new InetSocketAddress("localhost", LeshanServerBuilder.PORT),
                objectDefs.values().toArray(new LwM2mClientObjectDefinition[] {}));
    }

    // ---------------------------------------------------------------------
    // Object definitions

    // /1 SERVER

    static final int SHORT_SERVER_ID = 1;

    private LwM2mClientObjectDefinition buildServerObjectDefinition() {

        List<LwM2mClientResourceDefinition> rscDefs = new ArrayList<LwM2mClientResourceDefinition>();

        // short server id
        rscDefs.add(new SingleResourceDefinition(0, new IntegerLwM2mResource() {
            @Override
            protected void handleRead(final IntegerLwM2mExchange exchange) {
                exchange.respondContent(SHORT_SERVER_ID);
            }
        }, true));

        return new LwM2mClientObjectDefinition(1, true, false, rscDefs.toArray(new LwM2mClientResourceDefinition[0]));

    }

    // /3 DEVICE
    static final String MANUFACTURER_MODEL = "Manufacturer Model Test";
    static final String MODEL_NUMBER = "Model Number Test";
    static final String SERIAL_NUMBER = "TEST0123456789";
    static final String FIRMWARE_VERSION = "1.1";

    private LwM2mClientObjectDefinition buildDeviceObjectDefinition() {

        List<LwM2mClientResourceDefinition> rscDefs = new ArrayList<LwM2mClientResourceDefinition>();

        // manufacturer model
        rscDefs.add(new SingleResourceDefinition(0, new StringLwM2mResource() {
            @Override
            protected void handleRead(final StringLwM2mExchange exchange) {
                exchange.respondContent(MANUFACTURER_MODEL);
            }
        }, true));

        // model number
        rscDefs.add(new SingleResourceDefinition(1, new StringLwM2mResource() {
            @Override
            protected void handleRead(final StringLwM2mExchange exchange) {
                exchange.respondContent(MODEL_NUMBER);
            }
        }, true));

        // serial number
        rscDefs.add(new SingleResourceDefinition(2, new StringLwM2mResource() {
            @Override
            protected void handleRead(final StringLwM2mExchange exchange) {
                exchange.respondContent(SERIAL_NUMBER);
            }
        }, true));

        // firmware version
        rscDefs.add(new SingleResourceDefinition(3, new StringLwM2mResource() {
            @Override
            protected void handleRead(final StringLwM2mExchange exchange) {
                exchange.respondContent(FIRMWARE_VERSION);
            }
        }, true));

        return new LwM2mClientObjectDefinition(3, true, true, rscDefs.toArray(new LwM2mClientResourceDefinition[0]));

    }

}
