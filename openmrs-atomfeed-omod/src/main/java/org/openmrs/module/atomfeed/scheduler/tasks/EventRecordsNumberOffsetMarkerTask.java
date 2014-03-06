package org.openmrs.module.atomfeed.scheduler.tasks;

import org.bahmni.module.openerpatomfeedclient.api.client.impl.AtomFeedSpringTransactionManager;
import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
import org.ict4h.atomfeed.server.repository.AllEventRecords;
import org.ict4h.atomfeed.server.repository.AllEventRecordsOffsetMarkers;
import org.ict4h.atomfeed.server.repository.ChunkingEntries;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsOffsetMarkersJdbcImpl;
import org.ict4h.atomfeed.server.repository.jdbc.ChunkingEntriesJdbcImpl;
import org.ict4h.atomfeed.server.service.NumberOffsetMarkerServiceImpl;
import org.ict4h.atomfeed.server.service.OffsetMarkerService;
import org.ict4h.atomfeed.transaction.AFTransactionWork;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.common.repository.OpenMRSJdbcConnectionProvider;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

public class EventRecordsNumberOffsetMarkerTask extends AbstractTask {
    private int OFFSET_BY_NUMBER_OF_RECORDS_PER_CATEGORY = 1000;
    @Override
    public void execute() {
        // TODO : Mujir/Sush - make sure the new hibernatetransactionmanager is ok..
        //JdbcConnectionProvider connectionProvider = new OpenMRSJdbcConnectionProvider(getSpringPlatformTransactionManager());
        AtomFeedSpringTransactionManager atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(getSpringPlatformTransactionManager());
        //JdbcConnectionProvider connectionProvider = new OpenMRSJdbcConnectionProvider(new HibernateTransactionManager());
        AllEventRecords allEventRecords = new AllEventRecordsJdbcImpl(atomFeedSpringTransactionManager);
        AllEventRecordsOffsetMarkers eventRecordsOffsetMarkers = new AllEventRecordsOffsetMarkersJdbcImpl(atomFeedSpringTransactionManager);
        ChunkingEntries chunkingEntries = new ChunkingEntriesJdbcImpl(atomFeedSpringTransactionManager);
        final OffsetMarkerService markerService = new NumberOffsetMarkerServiceImpl(allEventRecords, chunkingEntries, eventRecordsOffsetMarkers);
        atomFeedSpringTransactionManager.executeWithTransaction(new AFTransactionWorkWithoutResult() {
            @Override
            protected void doInTransaction() {
                markerService.markEvents(OFFSET_BY_NUMBER_OF_RECORDS_PER_CATEGORY);
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
