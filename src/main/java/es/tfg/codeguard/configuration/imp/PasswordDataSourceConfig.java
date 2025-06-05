package es.tfg.codeguard.configuration.imp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import es.tfg.codeguard.configuration.DataSourceConfig;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "es.tfg.codeguard.model.repository.userpass" },
                        entityManagerFactoryRef = "entityManagerFactoryPassword",
                        transactionManagerRef = "transactionManagerPassword")
public class PasswordDataSourceConfig implements DataSourceConfig {

    @Override
    @Bean(name = "passwordDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.passwords")
    public DataSource dataSource(){

        return DataSourceBuilder.create().build();
    }

    @Override
    @Bean(name = "entityManagerFactoryPassword")
    public LocalContainerEntityManagerFactoryBean entityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("passwordDataSource")DataSource dataSource) {

        return builder
                .dataSource(dataSource)
                .packages("es.tfg.codeguard.model.entity.userpass")
                .persistenceUnit("password")
                .build();
    }

    @Override
    @Bean(name = "transactionManagerPassword")
    public PlatformTransactionManager platformTransactionManager(
            @Qualifier("entityManagerFactoryPassword") EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(entityManagerFactory);
    }
}
