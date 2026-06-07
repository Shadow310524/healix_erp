package in.healix.persistence.rls;

import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TenantAwareDataSource extends DelegatingDataSource {

    private final RlsConnectionPreparer connectionPreparer;

    public TenantAwareDataSource(DataSource targetDataSource, RlsConnectionPreparer connectionPreparer) {
        super(targetDataSource);
        this.connectionPreparer = connectionPreparer;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        connectionPreparer.prepare(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        connectionPreparer.prepare(connection);
        return connection;
    }
}
