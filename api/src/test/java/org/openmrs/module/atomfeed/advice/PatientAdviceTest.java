package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.mapper.EventMapper;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PatientAdviceTest{
    @Test
    public void shouldDoSomething() throws Throwable {
        EventService service = mock(EventService.class);
        EventMapper eventMapper = mock(EventMapper.class);
        Patient patient = new Patient();
        Event event = new Event(null,null,null, new URI(""),null,null);
        when(eventMapper.toEvent(patient)).thenReturn(event);
        new PatientAdvice(service, eventMapper).afterReturning(patient,null,null,null);
        verify(service).notify(event);
    }
}
