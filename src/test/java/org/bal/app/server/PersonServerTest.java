/*
 * Copyright 2016, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bal.app.server;


import com.google.protobuf.Empty;
import io.grpc.testing.GrpcServerRule;
import org.bal.app.proto.internal.Person;
import org.bal.app.proto.internal.PersonById;
import org.bal.app.proto.internal.PersonManagementGrpc;
import org.bal.app.server.service.PersonManagementService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = org.bal.app.server.config.Configuration.class)
public class PersonServerTest {
    /**
     * This creates and starts an in-process server, and creates a starters with an in-process channel.
     * When the app is done, it also shuts down the in-process starters and server.
     */
    @Rule
    public final GrpcServerRule grpcServerRule = new GrpcServerRule().directExecutor();

    @Autowired
    private PersonManagementService service;

    /**
     * To app the server, make calls with a real stub using the in-process channel, and verify
     * behaviors or state changes from the starters side.
     */
    @Test
    public void should_find_the_person_by_id() throws Exception {
        // Add the service to the in-process server.
        grpcServerRule.getServiceRegistry().addService(service);

        PersonManagementGrpc.PersonManagementBlockingStub blockingStub =
                PersonManagementGrpc.newBlockingStub(grpcServerRule.getChannel());


        Person person = blockingStub.getPersonById(PersonById.newBuilder().setId(2).build());

        assertThat(person.getFirstName()).isEqualTo("Balchu");
    }

    @Test
    public void should_find_random_names() throws Exception {
        // Add the service to the in-process server.
        grpcServerRule.getServiceRegistry().addService(service);

        PersonManagementGrpc.PersonManagementBlockingStub blockingStub =
                PersonManagementGrpc.newBlockingStub(grpcServerRule.getChannel());


        Person person = blockingStub.randomNames(Empty.newBuilder().build());

        assertThat(person.getFirstName()).isNotEmpty();
        assertThat(person.getDescription()).isNotEmpty();
    }
}