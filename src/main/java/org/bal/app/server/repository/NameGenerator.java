package org.bal.app.server.repository;

import org.bal.app.proto.internal.Person;

public interface NameGenerator {
    Person generatePerson();
}
