package org.openmrs.module.atomfeed.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.joda.time.DateTime;
import org.openmrs.Visit;
import org.openmrs.module.atomfeed.repository.hibernate.OpenMRSConnectionProvider;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class OrderAdvice implements MethodInterceptor {
    public static final String ENCOUNTER_REST_URL = "/openmrs/ws/rest/v1/encounter/%s?v=custom:(uuid,patient,orders:(uuid,orderType,concept))";
    public static final String TITLE = "Encounter";
    public static final String CATEGORY = "Encounter";

    private final EventService eventService;

    public OrderAdvice(EventService eventService){
        this.eventService = eventService;
    }

    public OrderAdvice() throws SQLException {
        AllEventRecordsJdbcImpl records = new AllEventRecordsJdbcImpl(new OpenMRSConnectionProvider());
        this.eventService = new EventServiceImpl(records);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        Visit visitBeforeSave = (Visit) methodInvocation.getArguments()[0];
        EncounterSnapshot encounterSnapshotBeforeSave = new EncounterSnapshot(visitBeforeSave.getEncounters());
        Visit visitAfterSave = (Visit) methodInvocation.proceed();
        EncounterSnapshot encounterSnapshotAfterSave = new EncounterSnapshot(visitAfterSave.getEncounters());

        List<String> changedEncounters = encounterSnapshotBeforeSave.changedEncounters(encounterSnapshotAfterSave);

        for (String encounterUuid : changedEncounters) {
            String url = String.format(ENCOUNTER_REST_URL, encounterUuid);
            Event event = new Event(UUID.randomUUID().toString(), TITLE, DateTime.now(), (URI) null, url, CATEGORY);
            eventService.notify(event);
        }

        return visitAfterSave;
    }




}
