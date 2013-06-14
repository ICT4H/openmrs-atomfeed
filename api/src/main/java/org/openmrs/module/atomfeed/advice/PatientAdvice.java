package org.openmrs.module.atomfeed.advice;

import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

public class PatientAdvice implements AfterReturningAdvice {
    @Override
    public void afterReturning(Object o, Method method, Object[] objects, Object o2) throws Throwable {
        System.out.println("inside after returning");
    }
}
