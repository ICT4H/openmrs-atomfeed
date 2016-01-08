package org.openmrs.module.atomfeed.advice;

public interface EventPublishFilter {
    Boolean canPublish(Object returnValue, Object[] arguments);
}
