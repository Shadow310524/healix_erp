package in.healix.persistence.config;

import in.healix.persistence.rls.RlsConnectionPreparer;
import in.healix.persistence.rls.TenantAwareDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@ConditionalOnBean(DataSourceProperties.class)
public class PersistenceConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource rawDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSource rawDataSource, RlsConnectionPreparer connectionPreparer) {
        return new TenantAwareDataSource(rawDataSource, connectionPreparer);
    }
}
