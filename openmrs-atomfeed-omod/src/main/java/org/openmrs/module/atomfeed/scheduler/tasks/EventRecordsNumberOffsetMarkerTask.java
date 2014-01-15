package org.openmrs.module.atomfeed.scheduler.tasks;

import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
import org.ict4h.atomfeed.server.repository.AllEventRecords;
import org.ict4h.atomfeed.server.repository.AllEventRecordsOffsetMarkers;
import org.ict4h.atomfeed.server.repository.ChunkingEntries;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsOffsetMarkersJdbcImpl;
import org.ict4h.atomfeed.server.repository.jdbc.ChunkingEntriesJdbcImpl;
import org.ict4h.atomfeed.server.service.NumberOffsetMarkerServiceImpl;
import org.ict4h.atomfeed.server.service.OffsetMarkerService;
import org.openmrs.module.atomfeed.common.repository.OpenMRSJdbcConnectionProvider;
import org.openmrs.module.atomfeed.repository.hibernate.OpenMRSConnectionProvider;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.sql.Connection;
import java.sql.SQLException;

public class EventRecordsNumberOffsetMarkerTask extends AbstractTask {
    private int OFFSET_BY_NUMBER_OF_RECORDS_PER_CATEGORY = 1000;
    @Override
    public void execute() {
        System.out.println("Executing task: EventRecordsNumberOffsetMarkerTask");
        JdbcConnectionProvider connectionProvider = new OpenMRSJdbcConnectionProvider();
        AllEventRecords allEventRecords = new AllEventRecordsJdbcImpl(connectionProvider);
        AllEventRecordsOffsetMarkers eventRecordsOffsetMarkers = new AllEventRecordsOffsetMarkersJdbcImpl(connectionProvider);
        ChunkingEntries chunkingEntries = new ChunkingEntriesJdbcImpl(connectionProvider);
        OffsetMarkerService markerService = new NumberOffsetMarkerServiceImpl(allEventRecords, chunkingEntries, eventRecordsOffsetMarkers);
        markerService.markEvents(OFFSET_BY_NUMBER_OF_RECORDS_PER_CATEGORY);
    }


}
