package org.openmrs.module.atomfeed.advice;


import org.ict4h.atomfeed.server.repository.AllEventRecordsQueue;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.EventPublishFilterHook;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EncounterServiceSaveAdvice implements AfterReturningAdvice {

    public static final String ENCOUNTER_REST_URL = getEncounterFeedUrl();
    public static final String TITLE = "Encounter";
    public static final String CATEGORY = "Encounter";
    private final AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;

    private static final String SAVE_METHOD = "saveEncounter";
    private static final String ENCOUNTER_TYPE_INVESTIGATION = "INVESTIGATION";

    private EventService eventService;

    public EncounterServiceSaveAdvice() throws SQLException {
        PlatformTransactionManager platformTransactionManager = getSpringPlatformTransactionManager();
        atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(platformTransactionManager);
        AllEventRecordsQueue allEventRecordsQueue = new AllEventRecordsQueueJdbcImpl(atomFeedSpringTransactionManager);
        this.eventService = new EventServiceImpl(allEventRecordsQueue);
    }


    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object encounterService) throws Throwable {
        if (method.getName().equals(SAVE_METHOD)) {
            Encounter encounter = (Encounter) args[0]; // Assuming encounter is the first argument
            String encounterType = encounter.getEncounterType().getName(); // Assuming encounterType is a string
            if (ENCOUNTER_TYPE_INVESTIGATION.equals(encounterType)) {

                String url = String.format(ENCOUNTER_REST_URL, encounter.getUuid());
                final Event event = new Event(UUID.randomUUID().toString(), TITLE, LocalDateTime.now(), (URI) null, url, CATEGORY);
                if (EventPublishFilterHook.shouldPublish(returnValue, args, "EncounterPublishCondition.groovy")) {
                    atomFeedSpringTransactionManager.executeWithTransaction(
                            new AFTransactionWorkWithoutResult() {
                                @Override
                                protected void doInTransaction() {
                                    eventService.notify(event);
                                }

                                @Override
                                public PropagationDefinition getTxPropagationDefinition() {
                                    return PropagationDefinition.PROPAGATION_REQUIRED;
                                }
                            }
                    );
                }
            }
        }
    }

    private static String getEncounterFeedUrl() {
        return Context.getAdministrationService().getGlobalProperty("encounter.feed.publish.url");
    }

    private PlatformTransactionManager getSpringPlatformTransactionManager() {
        List<PlatformTransactionManager> platformTransactionManagers = Context.getRegisteredComponents(PlatformTransactionManager.class);
        return platformTransactionManagers.get(0);
    }
}

