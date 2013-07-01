package org.openmrs.module.atomfeed.repository.hibernate;

import org.hibernate.SessionFactory;
import org.ict4h.atomfeed.server.repository.jdbc.JdbcConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

public class OpenMRSConnectionProvider implements JdbcConnectionProvider{

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return sessionFactory.getCurrentSession().connection();
    }
}
