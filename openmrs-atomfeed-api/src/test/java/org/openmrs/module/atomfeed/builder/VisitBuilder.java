package org.openmrs.module.atomfeed.builder;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.Visit;

import java.util.Date;

public class VisitBuilder {

    private Visit visit;
    private String lastEncounterUuid;

    private VisitBuilder(Visit visit){

        this.visit = visit;
    }

    public static VisitBuilder newVisit(Patient patient){
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setDateCreated(null);
        visit.setDateChanged(null);

        VisitBuilder visitBuilder = new VisitBuilder(visit);
        return visitBuilder;
    }



    public VisitBuilder newEncounter(String uuid) {
        Encounter encounter = createEncounter(uuid);
        this.visit.addEncounter(encounter);
        return this;
    }

    private Encounter createEncounter(String uuid) {
        Encounter encounter = new Encounter();
        encounter.setDateCreated(null);
        encounter.setDateChanged(null);

        encounter.setPatient(visit.getPatient());
        encounter.setEncounterDatetime(new Date());
        encounter.setUuid(uuid);
        this.lastEncounterUuid = uuid;
        return encounter;
    }

    public VisitBuilder newOrder(Concept concept) {
        createOrder(concept);
        return this;
    }

    private void createOrder(Concept concept) {
        Order order = new TestOrder();
        order.setDateCreated(null);
        order.setDateChanged(null);
        order.setConcept(concept);

        Encounter encounter = findEncounterByUuid(lastEncounterUuid);
        encounter.addOrder(order);
    }

    private Encounter findEncounterByUuid(String uuid) {
        for (Encounter encounter : this.visit.getEncounters()) {
            if (encounter.getUuid().equals(uuid)) {
                return encounter;
            }
        }
        return null;
    }

    public Visit build() {
        return visit;
    }

    public static VisitBuilder copy(Visit visitBeforeSave) {
        XStream xStream = new XStream(new DomDriver());
        String xml = xStream.toXML(visitBeforeSave);
        Visit visitCopy = (Visit) xStream.fromXML(xml);
        return new VisitBuilder(visitCopy);
    }

    public VisitBuilder saveEncounter(String encounterUuid) {
        Encounter encounter = findEncounterByUuid(encounterUuid);
        if (encounter.getDateCreated() == null ){
            encounter.setDateCreated(new Date());
        }
        encounter.setDateChanged(new Date());
        this.lastEncounterUuid = encounterUuid;
        return this;
    }

    public VisitBuilder saveOrders() {
        Encounter encounter = findEncounterByUuid(lastEncounterUuid);
        for (Order order : encounter.getOrders()) {
            if (order.getDateCreated() == null) {
                order.setDateCreated(new Date());
            }
        }
        return this;
    }

    public VisitBuilder encounter(String uuid) {
        Encounter encounter = createEncounter(uuid);
        visit.addEncounter(encounter);
        return saveEncounter(uuid);
    }

    public VisitBuilder order(Concept concept) {
        createOrder(concept);
        saveOrders();
        return this;
    }

    public VisitBuilder deleteOrder(String encounterId, Concept concept) {
        Encounter encounter = findEncounterByUuid(encounterId);
        for (Order order : encounter.getOrders()) {
            if (order.getConcept().equals(concept)) {
                order.setVoided(true);
                order.setDateVoided(new Date());
                break;
            }
        }
        return this;
    }
}
