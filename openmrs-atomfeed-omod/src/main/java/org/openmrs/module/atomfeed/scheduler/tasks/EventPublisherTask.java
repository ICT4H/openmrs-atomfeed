package org.openmrs.module.atomfeed.scheduler.tasks;

import org.ict4h.atomfeed.server.repository.AllEventRecords;
import org.ict4h.atomfeed.server.repository.AllEventRecordsQueue;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.publisher.EventRecordsPublishingService;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

public class EventPublisherTask extends AbstractTask {
    @Override
    public void execute() {
        final AtomFeedSpringTransactionManager atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(getSpringPlatformTransactionManager());
        atomFeedSpringTransactionManager.executeWithTransaction(new AFTransactionWorkWithoutResult() {
            @Override
            protected void doInTransaction() {
                AllEventRecords allEventRecords = new AllEventRecordsJdbcImpl(atomFeedSpringTransactionManager);
                AllEventRecordsQueue allEventRecordsQueue = new AllEventRecordsQueueJdbcImpl(atomFeedSpringTransactionManager);
                EventRecordsPublishingService.publish(allEventRecords, allEventRecordsQueue);
            }

            @Override
            public PropagationDefinition getTxPropagationDefinition() {
                return PropagationDefinition.PROPAGATION_REQUIRED;
            }
        });
    }

    private PlatformTransactionManager getSpringPlatformTransactionManager() {
        List<PlatformTransactionManager> platformTransactionManagers = Context.getRegisteredComponents(PlatformTransactionManager.class);
        return platformTransactionManagers.get(0);
    }

}
