package org.openmrs.module.atomfeed.repository.hibernate;

import org.hibernate.SessionFactory;
import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
import org.openmrs.api.context.ServiceContext;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

public class OpenMRSConnectionProvider implements JdbcConnectionProvider {

    @Override
    public Connection getConnection() throws SQLException {
        ServiceContext serviceContext = ServiceContext.getInstance();
        Class klass = serviceContext.getClass();
        try {
            Field field = klass.getDeclaredField("applicationContext");
            field.setAccessible(true);
            ApplicationContext applicationContext = (ApplicationContext) field.get(serviceContext);
            SessionFactory factory = (SessionFactory) applicationContext.getBean("sessionFactory");
            return factory.getCurrentSession().connection();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
