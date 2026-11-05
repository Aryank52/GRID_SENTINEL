package com.gridsentinel.infrastructure.config;

import org.flywaydb.core.Flyway;
import org.hibernate.cfg.AvailableSettings;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database and JPA configuration for PostGIS spatial support.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public Flyway flyway(@FlywayDataSource DataSource dataSource,
                         @Value("${spring.flyway.locations:classpath:db/migration}") String locations,
                         @Value("${spring.flyway.baseline-on-migrate:true}") boolean baselineOnMigrate,
                         @Value("${spring.flyway.validate-on-migrate:true}") boolean validateOnMigrate) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(baselineOnMigrate)
                .validateOnMigrate(validateOnMigrate)
                .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return properties -> {
            properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgisPG95Dialect");
            properties.put(AvailableSettings.FORMAT_SQL, true);
            properties.put(AvailableSettings.USE_SQL_COMMENTS, true);
            properties.put(AvailableSettings.ORDER_INSERTS, true);
            properties.put(AvailableSettings.ORDER_UPDATES, true);
            properties.put(AvailableSettings.BATCH_VERSIONED_DATA, true);
            // BATCH_SIZE and JDBC_BATCH_VERSIONED_DATA not available in Hibernate 6
            // Spatial handling
            properties.put("hibernate.spatial.use_geolatte", false); // Use JTS
        };
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabasePlatform("org.hibernate.dialect.PostgisPG95Dialect");
        adapter.setShowSql(false);
        adapter.setGenerateDdl(false); // We use Flyway
        return adapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                        JpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setJpaVendorAdapter(jpaVendorAdapter);
        em.setPackagesToScan(
                "com.gridsentinel.asset",
                "com.gridsentinel.grid",
                "com.gridsentinel.observation",
                "com.gridsentinel.risk",
                "com.gridsentinel.detection",
                "com.gridsentinel.threat",
                "com.gridsentinel.incident",
                "com.gridsentinel.simulation",
                "com.gridsentinel.response",
                "com.gridsentinel.recovery",
                "com.gridsentinel.protection",
                "com.gridsentinel.copilot",
                "com.gridsentinel.audit",
                "com.gridsentinel.auth",
                "com.gridsentinel.user"
        );
        return em;
    }

    @Bean
    public GeometryFactory geometryFactory() {
        return new GeometryFactory(new PrecisionModel(), 4326);
    }
}