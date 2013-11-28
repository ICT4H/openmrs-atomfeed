package org.openmrs.module.atomfeed.advice;

import org.apache.commons.beanutils.PropertyUtils;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.joda.time.DateTime;
import org.openmrs.module.atomfeed.repository.hibernate.OpenMRSConnectionProvider;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.net.URI;
import java.sql.SQLException;
import java.util.UUID;

public class EncounterSaveAdvice implements AfterReturningAdvice {

    public static final String ENCOUNTER_REST_URL = "/openmrs/ws/rest/v1/encounter/%s?v=custom:(uuid,patient,visit,orders:(uuid,orderType,concept,voided))";
    public static final String TITLE = "Encounter";
    public static final String CATEGORY = "Encounter";

    private final EventService eventService;

    public EncounterSaveAdvice() throws SQLException {
        AllEventRecordsJdbcImpl records = new AllEventRecordsJdbcImpl(new OpenMRSConnectionProvider());
        this.eventService = new EventServiceImpl(records);
    }

    @Override
    public void afterReturning(Object returnValue, Method save, Object[] args, Object emrEncounterService) throws Throwable {
        Object encounterUuid = PropertyUtils.getProperty(returnValue, "encounterUuid");
        String url = String.format(ENCOUNTER_REST_URL, encounterUuid);
        Event event = new Event(UUID.randomUUID().toString(), TITLE, DateTime.now(), (URI) null, url, CATEGORY);
        eventService.notify(event);
    }
}
