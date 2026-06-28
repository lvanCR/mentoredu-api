package com.mentoredu.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Arrays;

// Gestiona Flyway manualmente en todos los perfiles.
// Spring Boot 4.x rompe la ordenación Flyway → JPA con lazy-initialization; este config la restaura.
// spring.flyway.enabled=false en application.yml para que el autoconfigure de SB no interfiera.
@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        // baselineOnMigrate: BD con tablas pero sin historial (ej. recovery local) → baseline en V17.
        // BD vacía (prod fresh) → corre todas las migraciones desde V1 normalmente.
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("17")
                .load();
        flyway.migrate();
        return flyway;
    }

    // Restaura el orden Flyway → JPA que Spring Boot 4.x no garantiza con lazy-initialization.
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
