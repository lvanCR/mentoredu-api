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

    // Activo cuando spring.flyway.enabled=false (perfil local): Spring Boot autoconfigure queda
    // desactivado y este bean toma el control manual de las migraciones + el orden con JPA.
    // Cuando spring.flyway.enabled=true (producción) la autoconfigure de SB gestiona Flyway.
    @Bean
    @ConditionalOnProperty(name = "app.flyway.manual", havingValue = "true")
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("17")
                .load();
        flyway.migrate();
        return flyway;
    }

    // En Spring Boot 4.x la ordenación automática Flyway → JPA está rota; este post-processor
    // la restaura manualmente. Solo aplica cuando la autoconfigure de SB está desactivada.
    @Bean
    @ConditionalOnProperty(name = "app.flyway.manual", havingValue = "true")
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
