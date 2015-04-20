package cz.muni.fi.pv168.dragon;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.DERBY;


/**
* Spring Java configuration class. See http://static.springsource.org/spring/docs/current/spring-framework-reference/html/beans.html#beans-java
*
* @author Martin Kuba makub@ics.muni.cz
*/

//import org.apache.derby.jdbc.ClientDriver
@Configuration  //je to konfigurace pro Spring
@EnableTransactionManagement //bude řídit transakce u metod označených @Transactional
public class SpringConfig {

    @Bean
    public DataSource dataSource(){
        /*//sítová databáze
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
        bds.setUrl("jdbc:derby://localhost:1527/dragonDB");
        //bds.setUsername("admin");
        //bds.setPassword("admin");
        return bds;*/

        return new EmbeddedDatabaseBuilder()
                .setType(DERBY)
                .setName("dragonDB")
                .addScript("classpath:dragon_schema.sql")
                .addScript("classpath:fill_table.sql")
                .build();

    }

    @Bean //potřeba pro @EnableTransactionManagement
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean //náš manager, bude obalen řízením transakcí
    public CustomerManager customerManager() {
        return new CustomerManagerImpl(dataSource());
    }

    @Bean
    public DragonManager dragonManager() {
        return new DragonManagerImpl(new TransactionAwareDataSourceProxy(dataSource()), new TimeServiceImpl());
    }

    @Bean
    public LeaseManager leaseManager() {
        return new LeaseManagerImpl(dataSource(), new TimeServiceImpl());
    }
}
