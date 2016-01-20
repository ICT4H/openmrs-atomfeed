package org.openmrs.module.atomfeed;

import groovy.lang.GroovyClassLoader;
import org.apache.log4j.Logger;
import org.openmrs.module.atomfeed.advice.EncounterSaveAdvice;
import org.openmrs.module.atomfeed.filter.DefaultEventPublishFilter;
import org.openmrs.module.atomfeed.filter.EventPublishFilter;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EventPublishFilterHook {

    private static final Logger log = Logger.getLogger(EncounterSaveAdvice.class);
    private static GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    private static Map<String, EventPublishFilter> eventPublishFilterMap = new HashMap<String, EventPublishFilter>();

    public static Boolean shouldPublish(Object returnValue, Object[] arguments, String fileName) {
        try {
            return getEventPublishFilter(fileName).canPublish(returnValue, arguments);
        } catch (Exception e) {
            log.error("Problem while filtering events:", e);
        }
        return true;
    }

    private static EventPublishFilter getEventPublishFilter(String fileName) {
        if(eventPublishFilterMap.get(fileName) == null) {
            eventPublishFilterMap.put(fileName, initializeEventPublishFilter(fileName));
        }
        return eventPublishFilterMap.get(fileName);
    }

    private static EventPublishFilter initializeEventPublishFilter(String fileName) {
        EventPublishFilter eventPublishFilter = new DefaultEventPublishFilter();
        File eventPublisherFilterFile = new File(OpenmrsUtil.getApplicationDataDirectory(), "eventPublishFilters" + File.separator + fileName);
        if (eventPublisherFilterFile.exists()) {
            try {
                Class clazz = groovyClassLoader.parseClass(eventPublisherFilterFile);
                eventPublishFilter = (EventPublishFilter) clazz.newInstance();
            } catch (IOException e) {
                log.error("Problem with the groovy class " + eventPublisherFilterFile, e);
            } catch (InstantiationException e) {
                log.error("The groovy class " + eventPublisherFilterFile + " cannot be instantiated", e);
            } catch (IllegalAccessException e) {
                log.error("Problem with the groovy class " + eventPublisherFilterFile, e);
            }
        }
        return eventPublishFilter;
    }
}
