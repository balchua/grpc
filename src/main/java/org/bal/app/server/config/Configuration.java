package org.bal.app.server.config;


import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.ServerInterceptor;
import org.bal.app.server.repository.NameGenerator;
import org.bal.app.server.repository.NameGeneratorImpl;
import org.bal.app.server.service.PersonManagementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

@ComponentScan("org.bal.app.server")
@SpringBootConfiguration
public class Configuration {

    @Value("${zipkin.host}")
    private String zipkinHost;

    @Value("${zipkin.port:9411}")
    private int zipkinPort;

    @Bean
    public PersonManagementService personManagementService() {
        return new PersonManagementService();
    }

    @Bean
    public NameGenerator nameGenerator() {
        return new NameGeneratorImpl();
    }

//    @Bean
//    public CacheConfiguration cacheConfiguration() {
//
//        CacheConfiguration cacheCfg = new CacheConfiguration("myCache");
//
//        cacheCfg.setCacheMode(CacheMode.PARTITIONED);
//
//        return cacheCfg;
//
//    }
//
//    @Bean
//    public TcpDiscoverySpi discoverySpi() {
//        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
//
//        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
//
//
//        ipFinder.setMulticastGroup("228.10.10.157");
//
//        discoverySpi.setIpFinder(ipFinder);
//
//        discoverySpi.setForceServerMode(true);
//
//        return discoverySpi;
//
//    }
//
//
//    @Bean
//    public IgniteConfiguration igniteConfiguration() {
//        IgniteConfiguration cfg = new IgniteConfiguration();
//        cfg.setDiscoverySpi(discoverySpi());
//        cfg.setCacheConfiguration(cacheConfiguration());
//        return cfg;
//    }
//
//    @Bean
//    public Ignite ignite() {
//        return start(igniteConfiguration());
//
//    }

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
                .localServiceName("grpc-backend-service")
                .spanReporter(reporter()).build();
    }

    /**
     * Configuration for how to send spans to Zipkin
     */
    @Bean
    public Sender sender() {
        return OkHttpSender.create("http://" + zipkinHost + ":" + zipkinPort + "/api/v2/spans");
    }

    @Bean
    public GrpcTracing grpcTracing() {
        return GrpcTracing.create(tracing());
    }

    @Bean(name = "grpcServerInterceptor")
    public ServerInterceptor grpcServerInterceptor() {

        return grpcTracing().newServerInterceptor();
    }

}
