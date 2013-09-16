package org.openmrs.module.atomfeed.advice;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.aopalliance.intercept.MethodInvocation;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.module.atomfeed.builder.VisitBuilder;

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class OrderAdviceTest {

    private EventService eventService;
    private OrderAdvice orderAdvice;
    private Patient patient;
    private String uuid;
    private MethodInvocation methodInvocation;

    @Before
    public void setUp() throws Exception {
        eventService = mock(EventService.class);
        orderAdvice = new OrderAdvice(eventService);
        patient = createPatient();
        uuid = UUID.randomUUID().toString();
        methodInvocation = mock(MethodInvocation.class);
    }


    private Patient createPatient() {
        Patient patient = new Patient();
        patient.setId(10);

        Set<PersonName> names = new HashSet<PersonName>();
        names.add(new PersonName("Peter", null, "Parker"));

        patient.setNames(names);
        return patient;
    }

    @Test
    public void shouldPublishEncountersWhenOrdersAreNew() throws Throwable {

        Visit visitBeforeSave = VisitBuilder.newVisit(patient).newEncounter(uuid).newOrder(createConceptHaemoglobin()).newOrder(createConceptSlickingTest()).build();

        Visit[] visitsBeforeSave = { visitBeforeSave };

        Visit[] visitsAfterSave = { VisitBuilder.copy(visitBeforeSave).saveEncounter(uuid).saveOrders().build() };

        when(methodInvocation.getArguments()).thenReturn(visitsBeforeSave).thenReturn(visitsAfterSave);

        orderAdvice.invoke(methodInvocation);

        verify(eventService).notify(any(Event.class));
    }


    @Test
    public void shouldNotPublishEncountersWhenOrdersDidNotHaveAnyChange() throws Throwable {

        Visit visitBeforeSave = VisitBuilder.newVisit(patient)
                .encounter(uuid)
                .order(createConceptHaemoglobin())
                .order(createConceptSlickingTest())
                .build();

        Visit[] visitsBeforeSave = { visitBeforeSave };

        Visit[] visitsAfterSave = { VisitBuilder.copy(visitBeforeSave).saveEncounter(uuid).build() };

        when(methodInvocation.getArguments()).thenReturn(visitsBeforeSave).thenReturn(visitsAfterSave);

        orderAdvice.invoke(methodInvocation);

        verify(eventService, times(0)).notify(any(Event.class));
    }




    private Concept createConceptHaemoglobin() {
        Concept concept = new Concept();
        concept.setConceptId(38);

        concept.setFullySpecifiedName(new ConceptName("Haemoglobin", Locale.ENGLISH));
        concept.setConceptClass(createConceptClass());

        concept.setDateCreated(new Date());
        return concept;
    }

    private Concept createConceptSlickingTest() {
        Concept concept = new Concept();
        concept.setConceptId(33);

        concept.setFullySpecifiedName(new ConceptName("Slicking Test", Locale.ENGLISH));
        concept.setConceptClass(createConceptClass());

        concept.setDateCreated(new Date());
        return concept;
    }

    private ConceptClass createConceptClass() {
        ConceptClass conceptClass = new ConceptClass();
        conceptClass.setName("Test");
        return conceptClass;
    }
}
