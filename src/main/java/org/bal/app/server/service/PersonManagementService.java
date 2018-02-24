package org.bal.app.server.service;


import io.grpc.stub.StreamObserver;
import org.bal.app.proto.internal.FileContent;
import org.bal.app.proto.internal.Person;
import org.bal.app.proto.internal.PersonById;
import org.bal.app.proto.internal.PersonManagementGrpc;
import org.bal.app.server.interceptor.ZipkinServerInterceptor;
import org.bal.app.server.repository.NameGenerator;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;


@GRpcService(interceptors = {ZipkinServerInterceptor.class})
@Component
public class PersonManagementService extends PersonManagementGrpc.PersonManagementImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(PersonManagementService.class);

    @Autowired
    private NameGenerator nameGenerator;

    @Value("${grpc.file:/data}")
    private String fileName;

    @Override
    public void getPersonById(PersonById request,
                              StreamObserver<Person> responseObserver) {

        Person reply = Person.newBuilder().setFirstName("Balchu")
                .setLastName("Balchu").setId(123456).build();

        LOG.info("Hi {}", reply.getFirstName());

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    public void randomNames(com.google.protobuf.Empty request,
                            StreamObserver<Person> responseObserver) {

        Person person = nameGenerator.generatePerson();

        LOG.info("Hi {} {}", person.getDescription(), person.getFirstName());
        responseObserver.onNext(person);
        responseObserver.onCompleted();
    }


    public void whatsTheNameInTheFile(com.google.protobuf.Empty request,
                                      io.grpc.stub.StreamObserver<FileContent> responseObserver) {


        StringBuilder lines = new StringBuilder();
        LOG.info("File is : {}", Paths.get(fileName));

        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(s -> lines.append(s).append(System.lineSeparator()));

        } catch (IOException e) {
            LOG.error("Unable to find the file.", e);
        }
        FileContent fileContent = FileContent.newBuilder().setContent(lines.toString()).build();
        responseObserver.onNext(fileContent);
        responseObserver.onCompleted();
    }


}
