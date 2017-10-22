package org.bal.app.client.config;


import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.bal.app.proto.internal.PersonManagementGrpc;
import org.bal.app.proto.internal.PersonManagementGrpc.PersonManagementBlockingStub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

@org.springframework.context.annotation.Configuration
@ComponentScan("org.bal.app.client")
public class Configuration {

    @Value("${zipkin.external-host}")
    private String zipkinExternalHost;

    @Value("${zipkin.nodePort}")
    private int zipkinNodePort;

    @Value("${person-service.external-host}")
    private String personServiceExternalHost;

    @Value("${person-service.external-port}")
    private int personServiceExternalPort;


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
        return OkHttpSender.create("http://" + zipkinExternalHost + ":" + zipkinNodePort + "/api/v1/spans");
    }

    @Bean
    public GrpcTracing grpcTracing() {
        return GrpcTracing.create(tracing());
    }


    @Bean
    public ManagedChannelBuilder managedChannelBuilder() {
        return ManagedChannelBuilder.forAddress(personServiceExternalHost, personServiceExternalPort).intercept(grpcTracing().newClientInterceptor())
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
