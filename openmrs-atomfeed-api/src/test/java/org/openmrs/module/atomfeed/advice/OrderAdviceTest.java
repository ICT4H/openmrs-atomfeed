package org.openmrs.module.atomfeed.advice;

import org.aopalliance.intercept.MethodInvocation;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openmrs.*;
import org.openmrs.module.atomfeed.builder.VisitBuilder;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class OrderAdviceTest {
    private static final String ENCOUNTER_REST_URL = "/openmrs/ws/rest/v1/encounter/%s?v=full";

    private EventService eventService;
    private OrderAdvice orderAdvice;
    private Patient patient;
    private String uuid;
    private MethodInvocation methodInvocation;

    @Before
    public void setUp() throws Throwable {
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

        Visit visitAfterSave = VisitBuilder.copy(visitBeforeSave).saveEncounter(uuid).saveOrders().build();


        when(methodInvocation.getArguments()).thenReturn(visitsBeforeSave);
        when(methodInvocation.proceed()).thenReturn(visitAfterSave);

        orderAdvice.invoke(methodInvocation);

        ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

        verify(eventService).notify(argument.capture());

        assertEquals(String.format(ENCOUNTER_REST_URL, uuid), argument.getValue().getContents());
    }


    @Test
    public void shouldNotPublishEncountersWhenOrdersDidNotHaveAnyChange() throws Throwable {

        Visit visitBeforeSave = VisitBuilder.newVisit(patient)
                .encounter(uuid)
                .order(createConceptHaemoglobin())
                .order(createConceptSlickingTest())
                .build();

        Visit[] visitsBeforeSave = { visitBeforeSave };

        Visit visitAfterSave = VisitBuilder.copy(visitBeforeSave).saveEncounter(uuid).build();

        when(methodInvocation.getArguments()).thenReturn(visitsBeforeSave);
        when(methodInvocation.proceed()).thenReturn(visitAfterSave);

        orderAdvice.invoke(methodInvocation);

        verify(eventService, times(0)).notify(any(Event.class));
    }



    @Test
    public void shouldPublishEncounterWhenThereIsANewOrder() throws Throwable {

        String newUuid = UUID.randomUUID().toString();

        Visit visitBeforeSave = VisitBuilder.newVisit(patient).encounter(uuid).order(createConceptSlickingTest()).
                                                               newEncounter(newUuid).newOrder(createConceptHaemoglobin()).build();

        Visit visitAfterSave = VisitBuilder.copy(visitBeforeSave).saveEncounter(newUuid).saveOrders().build();

        Visit[] visitsBeforeSave = {visitBeforeSave};

        when(methodInvocation.getArguments()).thenReturn(visitsBeforeSave);
        when(methodInvocation.proceed()).thenReturn(visitAfterSave);

        orderAdvice.invoke(methodInvocation);

        ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

        verify(eventService).notify(argument.capture());

        assertEquals(String.format(ENCOUNTER_REST_URL, newUuid), argument.getValue().getContents());

    }


    @Test
    public void shouldPublishEncounterWhenAOrderIsDeleted() throws Throwable {
        Concept haemoglobin = createConceptHaemoglobin();
        Visit visitBeforeSave = VisitBuilder.newVisit(patient).encounter(uuid).order(haemoglobin).order(createConceptSlickingTest()).build();
        Visit visitAfterSave = VisitBuilder.copy(visitBeforeSave).deleteOrder(uuid, haemoglobin).build();

        when(methodInvocation.getArguments()).thenReturn(new Visit[]{visitBeforeSave});
        when(methodInvocation.proceed()).thenReturn(visitAfterSave);

        orderAdvice.invoke(methodInvocation);

        ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

        verify(eventService).notify(argument.capture());

        assertEquals(String.format(ENCOUNTER_REST_URL, uuid), argument.getValue().getContents());

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
