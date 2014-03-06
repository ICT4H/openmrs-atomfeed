package org.bahmni.module.openerpatomfeedclient.api.client.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
import org.ict4h.atomfeed.transaction.AFTransactionManager;
import org.ict4h.atomfeed.transaction.AFTransactionWork;

import java.sql.Connection;
import java.sql.SQLException;


public class AtomFeedHibernateConnectionManager implements AFTransactionManager, JdbcConnectionProvider {

    private final SessionFactory sessionFactory;

    public AtomFeedHibernateConnectionManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public <T> T executeWithTransaction(AFTransactionWork<T> action) throws RuntimeException {
        try {
            startTransaction();
            T result = action.execute();
            commit();
            return result;
        } catch (Exception e) {
            rollback();
            throw new RuntimeException(e);
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
