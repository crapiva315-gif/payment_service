package dev.alexeev.payment_service.config;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquibaseMongoConfig {

    @Bean
    ApplicationRunner liquibaseMongoMigration(
            @Value("${app.liquibase.mongo-url}") String mongoUrl,
            @Value("${app.liquibase.change-log:db/changelog/db.changelog-master.xml}") String changeLog) {
        return args -> {
            var accessor = new ClassLoaderResourceAccessor();
            try (Database database = DatabaseFactory.getInstance()
                    .openDatabase(mongoUrl, null, null, null, null, null, null, accessor);
                 Liquibase liquibase = new Liquibase(changeLog, accessor, database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        };
    }
}