package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.repository.AllEventRecordsQueue;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.joda.time.DateTime;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PersonRelationshipAdvice implements AfterReturningAdvice {
    private static final String CATEGORY = "relationship";
    private static final String TITLE = "Relationship";
    private static final String SAVE_RELATIONSHIP_METHOD = "saveRelationship";
    private static final String RAISE_RELATIONSHIP_EVENT_GLOBAL_PROPERTY = "atomfeed.publish.eventsForPatientRelationshipChange";
    private static final String RELATIONSHIP_EVENT_URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForPatientRelationshipChange";
    private static final String DEFAULT_RELATIONSHIP_URL_PATTERN = "/openmrs/ws/rest/v1/relationship/%s";
    private final AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;
    private final EventService eventService;

    public PersonRelationshipAdvice() throws SQLException {
        atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(getSpringPlatformTransactionManager());
        AllEventRecordsQueue allEventRecordsQueue = new AllEventRecordsQueueJdbcImpl(atomFeedSpringTransactionManager);
        this.eventService = new EventServiceImpl(allEventRecordsQueue);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
        if (method.getName().equals(SAVE_RELATIONSHIP_METHOD) && shouldRaiseRelationshipEvent()) {
            String contents = String.format(getUrlPattern(), ((Relationship) returnValue).getUuid());
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
        String raiseEvent = Context.getAdministrationService().getGlobalProperty(RAISE_RELATIONSHIP_EVENT_GLOBAL_PROPERTY);
        return Boolean.valueOf(raiseEvent);
    }

    private String getUrlPattern() {
        String urlPattern = Context.getAdministrationService().getGlobalProperty(RELATIONSHIP_EVENT_URL_PATTERN_GLOBAL_PROPERTY);
        if (urlPattern == null || urlPattern.equals("")) {
            return DEFAULT_RELATIONSHIP_URL_PATTERN;
        }
        return urlPattern;
    }

    private PlatformTransactionManager getSpringPlatformTransactionManager() {
        List<PlatformTransactionManager> platformTransactionManagers = Context.getRegisteredComponents(PlatformTransactionManager.class);
        return platformTransactionManagers.get(0);
    }
}
