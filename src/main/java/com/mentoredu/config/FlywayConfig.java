package com.mentoredu.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        return flyway;
    }

    /**
     * Garantiza que entityManagerFactory espere al bean "flyway" antes de inicializarse.
     * En Spring Boot 4.x, el mecanismo automático de ordenación Flyway → JPA está roto;
     * este post-processor lo restaura manualmente.
     */
    @Bean
    public static BeanFactoryPostProcessor flywayJpaDependencyPostProcessor() {
        return (ConfigurableListableBeanFactory factory) -> {
            String emfBean = "entityManagerFactory";
            if (!factory.containsBeanDefinition(emfBean)) return;
            BeanDefinition def = factory.getBeanDefinition(emfBean);
            String[] existing = def.getDependsOn();
            String[] updated = existing != null
                    ? Arrays.copyOf(existing, existing.length + 1)
                    : new String[1];
            updated[updated.length - 1] = "flyway";
            def.setDependsOn(updated);
        };
    }
}
