package es.tfg.codeguard.configuration.imp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

import es.tfg.codeguard.configuration.DataSourceConfig;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "es.tfg.codeguard.model.repository.user" },
                        entityManagerFactoryRef = "entityManagerFactoryUsers",
                        transactionManagerRef = "transactionManagerUser")
public class UserDataSourceConfig implements DataSourceConfig {

    @Override
    @Primary
    @Bean(name = "usersDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.users")
    public DataSource dataSource(){

        return DataSourceBuilder.create().build();
    }

    @Override
    @Primary
    @Bean(name = "entityManagerFactoryUsers")
    public LocalContainerEntityManagerFactoryBean entityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("usersDataSource")DataSource dataSource) {

        return builder
                .dataSource(dataSource)
                .packages("es.tfg.codeguard.model.entity.user")
                .persistenceUnit("users")
                .build();
    }

    @Override
    @Primary
    @Bean(name = "transactionManagerUser")
    public PlatformTransactionManager platformTransactionManager(
            @Qualifier("entityManagerFactoryUsers") EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(entityManagerFactory);
    }

}
