package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.repository.AllEventRecords;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;
import org.openmrs.module.atomfeed.mapper.EventMapper;
import org.openmrs.module.atomfeed.repository.hibernate.OpenMRSConnectionProvider;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.sql.SQLException;

public class PatientAdvice implements AfterReturningAdvice {
    private EventService eventService;
    private EventMapper eventMapper;

    public PatientAdvice(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    public PatientAdvice() throws SQLException {
        AllEventRecordsJdbcImpl records = new AllEventRecordsJdbcImpl(new OpenMRSConnectionProvider());
        records.setSchema("");
        this.eventService = new EventServiceImpl(records);
        this.eventMapper = new EventMapper();
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
        System.out.println("before returning");
        System.out.println(returnValue.toString());
        if(method.getName().equals("savePatient")){
            System.out.println("inside savePatient AOP");
            Event event = eventMapper.toEvent((Patient) returnValue);
            eventService.notify(event);
        }
        System.out.println("after returning");
    }
}
