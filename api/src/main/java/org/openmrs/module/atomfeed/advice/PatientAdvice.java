package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.module.atomfeed.mapper.EventMapper;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

public class PatientAdvice implements AfterReturningAdvice {
    private final EventService eventService;
    private final EventMapper eventMapper;

    @Autowired
    public PatientAdvice(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
        System.out.println("before returning");
        BaseOpenmrsData data = (BaseOpenmrsData) returnValue;
        Event event = eventMapper.toEvent(data);
        eventService.notify(event);
        System.out.println("after returning");
    }
}
