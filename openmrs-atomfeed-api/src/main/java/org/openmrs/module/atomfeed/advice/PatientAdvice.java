package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.joda.time.DateTime;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.repository.hibernate.OpenMRSConnectionProvider;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PatientAdvice implements AfterReturningAdvice {
    private static final String TEMPLATE = "/openmrs/ws/rest/v1/patient/%s?v=full";
    public static final String CATEGORY = "patient";
    public static final String TITLE = "Patient";
    public static final String SAVE_PATIENT_METHOD = "savePatient";
    private EventService eventService;

    public PatientAdvice(EventService eventService) {
        this.eventService = eventService;
    }

    public PatientAdvice() throws SQLException {
        List<PlatformTransactionManager> platformTransactionManagers = Context.getRegisteredComponents(PlatformTransactionManager.class);
        AllEventRecordsJdbcImpl records = new AllEventRecordsJdbcImpl(new OpenMRSConnectionProvider(platformTransactionManagers.get(0)));
        this.eventService = new EventServiceImpl(records);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
        if (method.getName().equals(SAVE_PATIENT_METHOD)) {
            String contents = String.format(TEMPLATE, ((Patient) returnValue).getUuid());
            Event event = new Event(UUID.randomUUID().toString(), TITLE, DateTime.now(), (URI) null, contents, CATEGORY);
            eventService.notify(event);
        }
    }
}
