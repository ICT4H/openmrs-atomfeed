package org.openmrs.module.atomfeed.mapper;

import org.ict4h.atomfeed.server.service.Event;
import org.joda.time.DateTime;
import org.openmrs.BaseOpenmrsData;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

@Component
public class EventMapper {
    //TODO : Read template and Host from configuration? and current request URI
    private static String TEMPLATE ="http://localhost:8080/openmrs/ws/rest/v1/patient/%s";

    public Event toEvent(BaseOpenmrsData data) throws URISyntaxException {
        return new Event(data.getUuid(),"title", DateTime.now(),String.format(TEMPLATE,data.getUuid()),null,null);
    }
}
