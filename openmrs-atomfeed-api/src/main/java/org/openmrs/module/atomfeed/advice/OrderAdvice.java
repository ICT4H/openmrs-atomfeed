package org.openmrs.module.atomfeed.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.joda.time.DateTime;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Visit;
import org.openmrs.module.atomfeed.repository.hibernate.OpenMRSConnectionProvider;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OrderAdvice implements MethodInterceptor {

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

        Object proceed = methodInvocation.proceed();

        Visit visitAfterSave = (Visit) methodInvocation.getArguments()[0];

        List<Encounter> encounterThatShouldBePublished = getPublishableEncounters(visitBeforeSave.getEncounters(), visitAfterSave.getEncounters());

        for (Encounter encounter : encounterThatShouldBePublished) {
            URI uri = new URI("");
            Event event = new Event(UUID.randomUUID().toString(), "Encounter", DateTime.now(), uri, null, "Encounter");
            eventService.notify(event);
        }

        return proceed;
    }

    private List<Encounter> getPublishableEncounters(Set<Encounter> beforeSaveEncounters, Set<Encounter> afterSaveEncounters) {
        List<Encounter> publishedEncounters = new ArrayList<Encounter>();

        for (Encounter beforeSaveEncounter : beforeSaveEncounters) {

            for (Encounter afterSaveEncounter : afterSaveEncounters) {

                //the equals method only verifies the UUID
                if (beforeSaveEncounter.equals(afterSaveEncounter)){

                    if (areAnyOfOrdersPublishable(beforeSaveEncounter.getOrders(), afterSaveEncounter.getOrders())){

                        publishedEncounters.add(afterSaveEncounter);

                        break;

                    }

                }
            }
        }


        return publishedEncounters;
    }

    private boolean areAnyOfOrdersPublishable(Set<Order> beforeSaveOrders, Set<Order> afterSaveOrders) {
        for (Order beforeSaveOrder : beforeSaveOrders) {

            for (Order afterSaveOrder : afterSaveOrders) {

                if (IsOrderPublishable(beforeSaveOrder, afterSaveOrder)) {

                    return true;
                }
            }

        }
        return false;
    }

    private boolean IsOrderPublishable(Order beforeSaveOrder, Order afterSaveOrder) {

        if (beforeSaveOrder.getDateCreated() == null) {
            return true;
        }

        if (beforeSaveOrder.equals(afterSaveOrder)){

            if (!afterSaveOrder.getVoided().equals(beforeSaveOrder.getVoided()) ){
                return true;
            }

        }
        return false;
    }


}
