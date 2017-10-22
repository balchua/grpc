package org.bal.app.server.config;


import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerInterceptor;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.bal.app.proto.internal.PersonManagementGrpc;
import org.bal.app.proto.internal.PersonManagementGrpc.PersonManagementBlockingStub;
import org.bal.app.server.service.PersonManagementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

import static org.apache.ignite.Ignition.start;

@org.springframework.context.annotation.Configuration
@ComponentScan("org.bal.app.server")
public class Configuration {

    @Value("${zipkin.service-host}")
    private String zipkinServiceHost;

    @Bean
    public PersonManagementService personManagementService() {
        return new PersonManagementService();
    }

    @Bean
    public CacheConfiguration cacheConfiguration() {

        CacheConfiguration cacheCfg = new CacheConfiguration("myCache");

        cacheCfg.setCacheMode(CacheMode.PARTITIONED);

        return cacheCfg;

    }

    @Bean
    public TcpDiscoverySpi discoverySpi() {
        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();

        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();


        ipFinder.setMulticastGroup("228.10.10.157");

        discoverySpi.setIpFinder(ipFinder);

        discoverySpi.setForceServerMode(true);

        return discoverySpi;

    }


    @Bean
    public IgniteConfiguration igniteConfiguration() {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setDiscoverySpi(discoverySpi());
        cfg.setCacheConfiguration(cacheConfiguration());
        return cfg;
    }

    @Bean
    public Ignite ignite() {
        return start(igniteConfiguration());

    }

    /**
     * Configuration for how to buffer spans into messages for Zipkin
     */
    @Bean
    public Reporter<Span> reporter() {
        return AsyncReporter.builder(sender()).build();
    }

    @Bean
    public Tracing tracing() {
        return Tracing.newBuilder()
                .localServiceName("person-grpc")
                .reporter(reporter()).build();
    }

    /**
     * Configuration for how to send spans to Zipkin
     */
    @Bean
    public Sender sender() {
        return OkHttpSender.create("http://" + zipkinServiceHost + ":9411/api/v1/spans");
    }

    @Bean
    public GrpcTracing grpcTracing() {
        return GrpcTracing.create(tracing());
    }

    @Bean(name = "grpcServerInterceptor")
    public ServerInterceptor grpcServerInterceptor() {

        return grpcTracing().newServerInterceptor();
    }

    @Bean
    public ManagedChannelBuilder managedChannelBuilder() {
        return ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext(true);
    }

    @Bean
    public ManagedChannel managedChannel() {
        return managedChannelBuilder().build();
    }

    @Bean("personManagementBlockingStub")
    public PersonManagementBlockingStub personManagementBlockingStub() {
        return PersonManagementGrpc.newBlockingStub(managedChannel());
    }
}
