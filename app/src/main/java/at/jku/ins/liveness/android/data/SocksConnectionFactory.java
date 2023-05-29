package at.jku.ins.liveness.android.data;

// directly copied from https://github.com/heishiroh/jersey-client-socks-sample/blob/master/src/main/java/ws/sicklemoon/sample/SocksConnectionFactory.java
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketAddress;
import java.net.URL;

import org.glassfish.jersey.client.HttpUrlConnectorProvider.ConnectionFactory;

public class SocksConnectionFactory implements ConnectionFactory {
    private final Proxy proxy;

    public SocksConnectionFactory(String hostname, int port) {
        SocketAddress socksAddr = new InetSocketAddress(hostname, port);
        this.proxy = new Proxy(Type.SOCKS, socksAddr);
    }

    public HttpURLConnection getConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection(proxy);
    }
}
