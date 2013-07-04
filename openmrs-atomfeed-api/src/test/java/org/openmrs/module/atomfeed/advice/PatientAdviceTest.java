package org.openmrs.module.atomfeed.advice;

import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.junit.Ignore;
import org.openmrs.Patient;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PatientAdviceTest {
    @Ignore
    public void shouldDoSomething() throws Throwable {
        EventService service = mock(EventService.class);
        Patient patient = new Patient();
        Event event = new Event(null, null, null, new URI(""), null, null);
        new PatientAdvice(service).afterReturning(patient, null, null, null);
        verify(service).notify(event);
    }
}
