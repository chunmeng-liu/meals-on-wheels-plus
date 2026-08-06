package com.example.mealsplus.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@ConditionalOnProperty(name = "DATABASE_URL")
public class HerokuDatabaseConfiguration {
    @Bean
    @Primary
    DataSource herokuDataSource() {
        URI uri = URI.create(System.getenv("DATABASE_URL"));
        String[] credentials = uri.getUserInfo().split(":", 2);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://" + uri.getHost() + ":" + (uri.getPort() == -1 ? 5432 : uri.getPort()) + uri.getPath());
        dataSource.setUsername(URLDecoder.decode(credentials[0], StandardCharsets.UTF_8));
        dataSource.setPassword(URLDecoder.decode(credentials[1], StandardCharsets.UTF_8));
        return dataSource;
    }
}
