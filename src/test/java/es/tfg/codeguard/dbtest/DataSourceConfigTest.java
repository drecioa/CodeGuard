package es.tfg.codeguard.dbtest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.sql.SQLException;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DataSourceConfigTest {

    @Autowired
    private ApplicationContext context;

    @Value("${spring.datasource.users.jdbc-url}")
    private String usersJdbcUrl;

    @Value("${spring.datasource.deleted-users.jdbc-url}")
    private String deletedUsersJdbcUrl;

    @Value("${spring.datasource.passwords.jdbc-url}")
    private String passwordJdbcUrl;

    @Value("${spring.datasource.exercises.jdbc-url}")
    private String exercisesJdbcUrl;

    @Test
    void correctBeans(){

        DataSource usersDataSource = (DataSource) context.getBean("usersDataSource");
        DataSource deletedUsersDataSource = (DataSource) context.getBean("deletedUsersDataSource");
        DataSource passwordDataSource = (DataSource) context.getBean("passwordDataSource");
        DataSource exercisesDataSource = (DataSource) context.getBean("exercisesDataSource");
        EntityManagerFactory usersEntityManagerFactory = (EntityManagerFactory) context.getBean("entityManagerFactoryUsers");
        EntityManagerFactory deletedUsersEntityManagerFactory = (EntityManagerFactory) context.getBean("entityManagerFactoryDeletedUsers");
        EntityManagerFactory passwordEntityManagerFactory = (EntityManagerFactory) context.getBean("entityManagerFactoryPassword");
        EntityManagerFactory exercisesEntityManagerFactory = (EntityManagerFactory) context.getBean("entityManagerFactoryExercises");

        assertNotNull(usersDataSource);
        assertNotNull(deletedUsersDataSource);
        assertNotNull(passwordDataSource);
        assertNotNull(exercisesDataSource);
        assertNotNull(usersEntityManagerFactory);
        assertNotNull(deletedUsersEntityManagerFactory);
        assertNotNull(passwordEntityManagerFactory);
        assertNotNull(exercisesEntityManagerFactory);

    }

    @Test
    void correctDataBase(){
        DataSource usersDataSource = (DataSource) context.getBean("usersDataSource");
        DataSource deletedUsersDataSource = (DataSource) context.getBean("deletedUsersDataSource");
        DataSource passwordDataSource = (DataSource) context.getBean("passwordDataSource");
        DataSource exercisesDataSource = (DataSource) context.getBean("exercisesDataSource");

        String usersURL = ((HikariDataSource) usersDataSource).getJdbcUrl();
        String deletedUsersURL = ((HikariDataSource) deletedUsersDataSource).getJdbcUrl();
        String passwordURL = ((HikariDataSource) passwordDataSource).getJdbcUrl();
        String exercisesURL = ((HikariDataSource) exercisesDataSource).getJdbcUrl();

        assertEquals(usersJdbcUrl, usersURL);
        assertEquals(deletedUsersJdbcUrl, deletedUsersURL);
        assertEquals(passwordJdbcUrl, passwordURL);
        assertEquals(exercisesJdbcUrl, exercisesURL);
    }

    @Test
    void openDbTest() throws SQLException {
        DataSource usersDataSource = (DataSource) context.getBean("usersDataSource");
        DataSource deletedUsersDataSource = (DataSource) context.getBean("deletedUsersDataSource");
        DataSource passwordDataSource = (DataSource) context.getBean("passwordDataSource");
        DataSource exercisesDataSource = (DataSource) context.getBean("exercisesDataSource");

        assertFalse(usersDataSource.getConnection().isClosed());
        assertFalse(deletedUsersDataSource.getConnection().isClosed());
        assertFalse(passwordDataSource.getConnection().isClosed());
        assertFalse(exercisesDataSource.getConnection().isClosed());
    }
}
