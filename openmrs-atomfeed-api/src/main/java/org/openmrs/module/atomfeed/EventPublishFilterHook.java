package org.openmrs.module.atomfeed;

import groovy.lang.GroovyClassLoader;
import org.apache.log4j.Logger;
import org.openmrs.module.atomfeed.advice.EncounterSaveAdvice;
import org.openmrs.module.atomfeed.advice.EventPublishFilter;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.IOException;

public class EventPublishFilterHook {

    private static final Logger log = Logger.getLogger(EncounterSaveAdvice.class);
    private static GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    public static Boolean shouldPublish(Object returnValue, Object[] arguments, String fileName) {
        File eventPublisherFilterFile = new File(OpenmrsUtil.getApplicationDataDirectory(), "eventPublishFilters" + File.separator + fileName);
        if (eventPublisherFilterFile.exists()) {
            try {
                Class clazz = groovyClassLoader.parseClass(eventPublisherFilterFile);
                EventPublishFilter eventPublishFilter = (EventPublishFilter) clazz.newInstance();
                return eventPublishFilter.canPublish(returnValue, arguments);
            } catch (IOException e) {
                log.error("Problem with the groovy class " + eventPublisherFilterFile, e);
            } catch (InstantiationException e) {
                log.error("The groovy class " + eventPublisherFilterFile + " cannot be instantiated", e);
            } catch (IllegalAccessException e) {
                log.error("Problem with the groovy class " + eventPublisherFilterFile, e);
            }
        }
        return true;
    }
}
