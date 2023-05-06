package ru.streamify.microservices.song;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import ru.streamify.microservices.song.entity.SongEntity;

@ComponentScan("ru.streamify")
@SpringBootApplication
public class SongServiceApplication {
    private static final Logger LOG = LoggerFactory.getLogger(SongServiceApplication.class);

    private static final String PROPERTY_MONGODB_HOST = "spring.data.mongodb.host";
    private static final String PROPERTY_MONGODB_PORT = "spring.data.mongodb.port";

    final ReactiveMongoOperations mongoTemplate;

    @Autowired
    public SongServiceApplication(ReactiveMongoOperations mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SongServiceApplication.class, args);

        String host = context.getEnvironment().getProperty(PROPERTY_MONGODB_HOST);
        String port = context.getEnvironment().getProperty(PROPERTY_MONGODB_PORT);

        LOG.info("Connected to MongoDB: {}:{}", host, port);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {
        var mappingContext = mongoTemplate.getConverter().getMappingContext();
        IndexResolver indexResolver = new MongoPersistentEntityIndexResolver(mappingContext);

        ReactiveIndexOperations indexOps = mongoTemplate.indexOps(SongEntity.class);
        indexResolver.resolveIndexFor(SongEntity.class)
                .forEach(entity -> indexOps.ensureIndex(entity).block());
    }

}
