package org.openmrs.module.atomfeed.filter;

public interface EventPublishFilter {
    Boolean canPublish(Object returnValue, Object[] arguments);
}
