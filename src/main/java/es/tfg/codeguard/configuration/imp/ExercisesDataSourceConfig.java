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

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

import es.tfg.codeguard.configuration.DataSourceConfig;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "es.tfg.codeguard.model.repository.exercise" },
                        entityManagerFactoryRef = "entityManagerFactoryExercises",
                        transactionManagerRef = "transactionManagerExercises")
public class ExercisesDataSourceConfig implements DataSourceConfig {

    @Override
    @Bean(name = "exercisesDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.exercises")
    public DataSource dataSource(){

        return DataSourceBuilder.create().build();
    }

    @Override
    @Bean(name = "entityManagerFactoryExercises")
    public LocalContainerEntityManagerFactoryBean entityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("exercisesDataSource")DataSource dataSource) {

        return builder
                .dataSource(dataSource)
                .packages("es.tfg.codeguard.model.entity.exercise")
                .persistenceUnit("exercises")
                .build();
    }

    @Override
    @Bean(name = "transactionManagerExercises")
    public PlatformTransactionManager platformTransactionManager(
            @Qualifier("entityManagerFactoryExercises") EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(entityManagerFactory);
    }

}
