package leshan.interop;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import leshan.client.californium.LeshanClient;
import leshan.client.exchange.LwM2mExchange;
import leshan.client.resource.LwM2mClientObjectDefinition;
import leshan.client.resource.LwM2mClientResource;
import leshan.client.resource.LwM2mClientResourceDefinition;
import leshan.client.resource.SingleResourceDefinition;
import leshan.client.resource.integer.IntegerLwM2mExchange;
import leshan.client.resource.integer.IntegerLwM2mResource;
import leshan.client.resource.string.StringLwM2mExchange;
import leshan.client.resource.string.StringLwM2mResource;
import leshan.client.response.ExecuteResponse;
import leshan.client.response.ReadResponse;
import leshan.client.response.WriteResponse;
import leshan.server.californium.LeshanServerBuilder;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.PskStore;

public class TestClientBuilder {

    private final InetSocketAddress clientAddress = new InetSocketAddress(0);

    private CoapServer localCoapServer = new CoapServer();

    private Map<Integer, LwM2mClientObjectDefinition> objectDefs = new HashMap<Integer, LwM2mClientObjectDefinition>();

    private int serverPort = LeshanServerBuilder.PORT;

    public void addServerObject() {
        if (!objectDefs.containsKey(1)) {
            LwM2mClientObjectDefinition def = this.buildServerDefinition();
            objectDefs.put(1, def);
        }
    }

    public void addDeviceObject() {
        if (!objectDefs.containsKey(3)) {
            LwM2mClientObjectDefinition def = this.buildDeviceDefinition();
            objectDefs.put(3, def);
        }
    }

    public void addConnectivityMonitoringObject() {
        if (!objectDefs.containsKey(4)) {
            LwM2mClientObjectDefinition def = this.buildConnectivityMonitoringDefinition();
            objectDefs.put(4, def);
        }
    }

    public void setSecure(final String pskIdentity, final byte[] pskSecret) {
        final DTLSConnector connector = new DTLSConnector(clientAddress);
        connector.getConfig().setPreferredCipherSuite(CipherSuite.TLS_PSK_WITH_AES_128_CCM_8);
        connector.getConfig().setPskStore(new PskStore() {

            @Override
            public String getIdentity(InetSocketAddress inetAddress) {
                return pskIdentity;
            }

            @Override
            public byte[] getKey(String identity) {
                return pskSecret;
            }

        });

        final Endpoint secureEndpoint = new CoAPEndpoint(connector, NetworkConfig.getStandard());
        localCoapServer = new CoapServer();
        localCoapServer.addEndpoint(secureEndpoint);

        serverPort = LeshanServerBuilder.PORT_DTLS;
    }

    public LeshanClient build() {
        return new LeshanClient(clientAddress, new InetSocketAddress("localhost", serverPort), localCoapServer,
                objectDefs.values().toArray(new LwM2mClientObjectDefinition[] {}));
    }

    // ---------------------------------------------------------------------
    // Object definitions

    // /1 SERVER

    static final int SHORT_SERVER_ID = 1;

    private LwM2mClientObjectDefinition buildServerDefinition() {

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

    private LwM2mClientObjectDefinition buildDeviceDefinition() {

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

        // reboot
        rscDefs.add(new SingleResourceDefinition(4, new LwM2mClientResource() {

            @Override
            public void execute(LwM2mExchange exchange) {
                System.out.println("Reboot request received");
                exchange.respond(ExecuteResponse.success());
            }

            @Override
            public void write(LwM2mExchange exchange) {
                exchange.respond(WriteResponse.notAllowed());
            }

            @Override
            public void read(LwM2mExchange exchange) {
                exchange.respond(ReadResponse.notAllowed());
            }

            @Override
            public boolean isReadable() {
                return false;
            }

            @Override
            public void notifyResourceUpdated() {
                //
            }

        }, true));

        return new LwM2mClientObjectDefinition(3, true, true, rscDefs.toArray(new LwM2mClientResourceDefinition[0]));
    }

    // /4 CONNECTIVITY MONITORING

    private LwM2mClientObjectDefinition buildConnectivityMonitoringDefinition() {

        List<LwM2mClientResourceDefinition> rscDefs = new ArrayList<LwM2mClientResourceDefinition>();

        // radio signal strength
        rscDefs.add(new SingleResourceDefinition(2, new IntegerLwM2mResource() {
            Random random = new Random();

            @Override
            protected void handleRead(final IntegerLwM2mExchange exchange) {
                // random value: -50 > val > -110
                exchange.respondContent(-1 * (random.nextInt(60) + 50));
            }
        }, true));

        return new LwM2mClientObjectDefinition(4, true, true, rscDefs.toArray(new LwM2mClientResourceDefinition[0]));
    }

}
