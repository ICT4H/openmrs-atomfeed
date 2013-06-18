package org.openmrs.module.atomfeed.factory;

import org.hibernate.SessionFactory;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.openmrs.module.atomfeed.repository.hibernate.EventRecords;

public class EventServiceFactory {
    public EventService get(SessionFactory sessionFactory){
        return new EventServiceImpl(new EventRecords(sessionFactory));
    }
}
