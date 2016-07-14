package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.repository.AllEventRecordsQueue;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.joda.time.DateTime;
import org.openmrs.PatientProgram;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PatientProgramAdvice implements AfterReturningAdvice {
    private static final String CATEGORY = "program_enrollment";
    private static final String TITLE = "Progam Enrollment";
    private static final String SAVE_PATIENT_PROGRAM_METHOD = "savePatientProgram";
    private static final String RAISE_PATIENT_PROGRAM_EVENT_GLOBAL_PROPERTY = "atomfeed.publish.eventsForPatientProgramStateChange";
    private static final String PATIENT_PROGRAM_EVENT_URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForProgramStateChange";
    private static final String DEFAULT_PATIENT_PROGRAM_URL_PATTERN = "/openmrs/ws/rest/v1/programenrollment/{uuid}?v=full";
    private final AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;
    private final EventService eventService;

    public PatientProgramAdvice() throws SQLException {
        atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(getSpringPlatformTransactionManager());
        AllEventRecordsQueue allEventRecordsQueue = new AllEventRecordsQueueJdbcImpl(atomFeedSpringTransactionManager);
        this.eventService = new EventServiceImpl(allEventRecordsQueue);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
        if (method.getName().equals(SAVE_PATIENT_PROGRAM_METHOD) && shouldRaiseRelationshipEvent()) {
            String contents = getUrlPattern().replace("{uuid}",((PatientProgram) returnValue).getUuid());
            final Event event = new Event(UUID.randomUUID().toString(), TITLE, DateTime.now(), (URI) null, contents, CATEGORY);

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

    private boolean shouldRaiseRelationshipEvent() {
        String raiseEvent = Context.getAdministrationService().getGlobalProperty(RAISE_PATIENT_PROGRAM_EVENT_GLOBAL_PROPERTY);
        return Boolean.valueOf(raiseEvent);
    }

    private String getUrlPattern() {
        String urlPattern = Context.getAdministrationService().getGlobalProperty(PATIENT_PROGRAM_EVENT_URL_PATTERN_GLOBAL_PROPERTY);
        if (urlPattern == null || urlPattern.equals("")) {
            return DEFAULT_PATIENT_PROGRAM_URL_PATTERN;
        }
        return urlPattern;
    }

    private PlatformTransactionManager getSpringPlatformTransactionManager() {
        List<PlatformTransactionManager> platformTransactionManagers = Context.getRegisteredComponents(PlatformTransactionManager.class);
        return platformTransactionManagers.get(0);
    }
}
