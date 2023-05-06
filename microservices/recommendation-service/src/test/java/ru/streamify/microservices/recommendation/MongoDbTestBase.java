package ru.streamify.microservices.recommendation;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

public abstract class MongoDbTestBase {
    private static final MongoDBContainer DATABASE = new MongoDBContainer("mongo:4.4.2");

    static {
        DATABASE.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", DATABASE::getHost);
        registry.add("spring.data.mongodb.port", () -> DATABASE.getMappedPort(27017));
        registry.add("spring.data.mongodb.database", () -> "test");
    }
}