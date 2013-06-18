package org.openmrs.module.atomfeed.repository.hibernate;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.SessionFactory;
import org.ict4h.atomfeed.server.domain.EventRecord;
import org.ict4h.atomfeed.server.domain.chunking.time.TimeRange;
import org.ict4h.atomfeed.server.repository.AllEventRecords;

import java.util.List;

public class EventRecords implements AllEventRecords{

    private final SessionFactory sessionFactory;

    public EventRecords(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void add(EventRecord eventRecord) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EventRecord get(String uuid) {
        throw new NotImplementedException("use AllEventRecords implementation in atomfeed server instead.");
    }

    @Override
    public int getTotalCount() {
        throw new NotImplementedException("use AllEventRecords implementation in atomfeed server instead.");
    }

    @Override
    public List<EventRecord> getEventsFromRange(Integer first, Integer last) {
        throw new NotImplementedException("use AllEventRecords implementation in atomfeed server instead.");
    }

    @Override
    public List<EventRecord> getEventsFromTimeRange(TimeRange timeRange) {
        throw new NotImplementedException("use AllEventRecords implementation in atomfeed server instead.");
    }
}
