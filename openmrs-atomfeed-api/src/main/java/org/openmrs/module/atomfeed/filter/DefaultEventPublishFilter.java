package org.openmrs.module.atomfeed.filter;

public class DefaultEventPublishFilter implements EventPublishFilter {
    @Override
    public Boolean canPublish(Object returnValue, Object[] arguments) {
        return true;
    }
}
