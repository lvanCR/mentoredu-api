package com.mentoredu.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class FlywayConfig {

    // matchIfMissing = true → activo por defecto; false cuando spring.flyway.enabled=false (tests H2)
    @Bean
    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        return flyway;
    }

    // En Spring Boot 4.x la ordenación automática Flyway → JPA está rota; este post-processor
    // la restaura manualmente. Solo aplica cuando Flyway está habilitado.
    @Bean
    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
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
