package org.trading.market;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;

import java.util.Optional;

import static java.lang.System.getenv;

public class ConsulProvisioning {

    public static void main(String[] args) {
        KeyValueClient kvClient;

        Consul consul = Consul.builder()
                .withHostAndPort(HostAndPort.fromParts(Optional.ofNullable(getenv("CONSUL_URL")).orElse("localhost"), 8500))
                .build();

        kvClient = consul.keyValueClient();
        kvClient.putValue("symbol/EURUSD/price", "1.1578");
        kvClient.putValue("symbol/EURGBP/price", "0.8813");
        kvClient.putValue("symbol/EURJPY/price", "124.57");
        kvClient.putValue("symbol/EURUSD/precision", "5");
        kvClient.putValue("symbol/EURGBP/precision", "5");
        kvClient.putValue("symbol/EURJPY/precision", "3");
        kvClient.putValue("ladder/quantities", "5000000,10000000,25000000");

    }
}
