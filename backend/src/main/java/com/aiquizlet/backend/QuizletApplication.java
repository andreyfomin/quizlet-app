package com.aiquizlet.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

// DataSource/JPA autoconfiguration is excluded until a Postgres connection and
// entities exist; the dependencies are already wired in build.gradle for when that lands.
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class QuizletApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizletApplication.class, args);
    }
}
