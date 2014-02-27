package org.bahmni.module.openerpatomfeedclient.api.client.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.ict4h.atomfeed.client.service.AFTransactionManager;
import org.ict4h.atomfeed.client.service.AFTransactionWork;
import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;


public class AtomFeedJdbcConnectionManager implements AFTransactionManager, JdbcConnectionProvider {

    private final SessionFactory sessionFactory;

    public AtomFeedJdbcConnectionManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public <T> T executeWithTransaction(AFTransactionWork<T> action) throws Exception {
        try {
            startTransaction();
            T result = action.execute();
            commit();
            return result;
        } catch (Exception e) {
            rollback();
            throw new Exception(e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        //TODO: ensure that only connection associated with current thread current transaction is given
        return getCurrentSession().connection();
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        Session currentSession = getCurrentSession();
        if (!currentSession.isOpen()) {
            currentSession.close();
        }
    }

    @Override
    public void startTransaction() {
        Transaction transaction = getCurrentSession().getTransaction();
        if (transaction == null || !transaction.isActive()) {
            getCurrentSession().beginTransaction();
        }
    }

    @Override
    public void commit() {
        Transaction transaction = getCurrentSession().getTransaction();
        if (!transaction.wasCommitted()) {
            transaction.commit();
        }
    }

    @Override
    public void rollback() {
        Transaction transaction = getCurrentSession().getTransaction();
        if (!transaction.wasRolledBack()) {
            transaction.rollback();
        }
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
