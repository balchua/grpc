package org.bal.app.server.service;


import io.grpc.stub.StreamObserver;
import org.bal.app.proto.internal.Person;
import org.bal.app.proto.internal.PersonById;
import org.bal.app.proto.internal.PersonManagementGrpc;
import org.bal.app.server.interceptor.ZipkinServerInterceptor;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@GRpcService(interceptors = {ZipkinServerInterceptor.class})
public class PersonManagementService extends PersonManagementGrpc.PersonManagementImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(PersonManagementService.class);

    @Override
    public void getPersonById(PersonById request,
                              StreamObserver<Person> responseObserver) {

        Person reply = Person.newBuilder().setFirstName("Balchu")
                .setLastName("Balchu").setId(123456).build();

        LOG.info("Hi {}", reply.getFirstName());

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
