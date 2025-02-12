package com.garnicsoft.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({R2dbcProperties.class, FlywayProperties.class})
class FlywayConfig {

  /**
   * Dado que Flyway no funciona con R2DBC, necesitaremos crear el bean Flyway con el metodo init
   * migrants(), que solicita a Spring que ejecute nuestras migraciones tan pronto como crea el
   * bean
   */
  @Bean(initMethod = "migrate")
  public Flyway flyway(FlywayProperties flywayProperties) {
    return Flyway.configure()
        .dataSource(
            flywayProperties.getUrl(), flywayProperties.getUser(), flywayProperties.getPassword())
        .locations(flywayProperties.getLocations().toArray(String[]::new))
        .schemas(flywayProperties.getSchemas().toArray(String[]::new))
        .baselineOnMigrate(true)
        .table(flywayProperties.getTable())
        .load();
  }
}