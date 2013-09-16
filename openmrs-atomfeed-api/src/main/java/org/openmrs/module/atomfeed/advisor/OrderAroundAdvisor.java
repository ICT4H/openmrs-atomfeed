package org.openmrs.module.atomfeed.advisor;


import org.aopalliance.aop.Advice;
import org.openmrs.module.atomfeed.advice.OrderAdvice;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

import java.lang.reflect.Method;
import java.sql.SQLException;

public class OrderAroundAdvisor extends StaticMethodMatcherPointcutAdvisor implements Advisor {

    private static final String SAVE_VISIT = "saveVisit";

    @Override
    public boolean matches(Method method, Class<?> aClass) {

        if (SAVE_VISIT.equals(method.getName())){
            return true;
        }

        return false;
    }

    @Override
    public Advice getAdvice() {
        try {
            return new OrderAdvice();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
