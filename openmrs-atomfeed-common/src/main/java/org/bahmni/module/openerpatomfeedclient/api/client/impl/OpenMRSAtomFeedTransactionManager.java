package org.bahmni.module.openerpatomfeedclient.api.client.impl;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.ict4h.atomfeed.client.service.AFTransactionWork;
import org.ict4h.atomfeed.client.service.AFTransactionManager;
import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
import org.openmrs.api.context.ServiceContext;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

public class OpenMRSAtomFeedTransactionManager implements AFTransactionManager, JdbcConnectionProvider {
    private PlatformTransactionManager transactionManager;

    public OpenMRSAtomFeedTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void executeWithTransaction(final AFTransactionWork action) throws Exception {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        //action.getDefinition()
        transactionTemplate.execute( new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        action.execute();
                    }
                });
    }


    /**
     * @see org.ict4h.atomfeed.jdbc.JdbcConnectionProvider
     * @return
     * @throws SQLException
     */
    @Override
    public Connection getConnection() throws SQLException {
        //TODO: ensure that only connection associated with current thread current transaction is given
        return getSession().connection();
    }

    private Session getSession() {
        ServiceContext serviceContext = ServiceContext.getInstance();
        Class klass = serviceContext.getClass();
        try {
            Field field = klass.getDeclaredField("applicationContext");
            field.setAccessible(true);
            ApplicationContext applicationContext = (ApplicationContext) field.get(serviceContext);
            SessionFactory factory = (SessionFactory) applicationContext.getBean("sessionFactory");
            return factory.getCurrentSession();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.ict4h.atomfeed.jdbc.JdbcConnectionProvider
     * @param connection
     * @throws SQLException
     */
    @Override
    public void closeConnection(Connection connection) throws SQLException {
        //
    }

//
//    /**
//     *  @see org.ict4h.atomfeed.jdbc.JdbcConnectionProvider
//     */
//    @Override
//    public void startTransaction() {
//
//    }
//
//    /**
//     *  @see org.ict4h.atomfeed.jdbc.JdbcConnectionProvider
//     */
//    @Override
//    public void commit() {
//
//    }
//
//    /**
//     *  @see org.ict4h.atomfeed.jdbc.JdbcConnectionProvider
//     */
//    @Override
//    public void rollback() {
//
//    }
}
