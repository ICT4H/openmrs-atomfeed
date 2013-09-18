package org.openmrs.module.atomfeed.advice;

import org.openmrs.Encounter;
import org.openmrs.Order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class EncounterSnapshot {

    private final ArrayList<DummyEncounter> dummyEncounters = new ArrayList<DummyEncounter>();

    private class DummyOrder {

        private final String uuid;
        private final Date dateCreated;
        private final boolean voided;

        public DummyOrder(Order order) {
            this.uuid = order.getUuid();
            Date date = order.getDateCreated();
            this.dateCreated = (date == null) ? null : new Date(date.getTime());
            this.voided = order.getVoided().booleanValue();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DummyOrder that = (DummyOrder) o;

            if (!uuid.equals(that.uuid)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }
    }

    private class DummyEncounter {
        private final String uuid;
        private final ArrayList<DummyOrder> orders = new ArrayList<DummyOrder>();

        public DummyEncounter(Encounter encounter) {
            this.uuid = encounter.getUuid();
            for (Order order : encounter.getOrders()) {
                this.orders.add(new DummyOrder(order));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DummyEncounter that = (DummyEncounter) o;

            if (!uuid.equals(that.uuid)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }
    }

    public EncounterSnapshot(Set<Encounter> encounters) {
        for (Encounter encounter : encounters) {
            this.dummyEncounters.add(new DummyEncounter(encounter));
        }
    }

    public List<String> changedEncounters(EncounterSnapshot snapshotAfterSave) {
        return getPublishableEncounters(this.dummyEncounters, snapshotAfterSave.dummyEncounters);
    }

    private List<String> getPublishableEncounters(List<DummyEncounter> beforeSaveEncounters, List<DummyEncounter> afterSaveEncounters) {
        List<String> publishedEncounters = new ArrayList<String>();

        for (DummyEncounter beforeSaveEncounter : beforeSaveEncounters) {

            for (DummyEncounter afterSaveEncounter : afterSaveEncounters) {

                //the equals method only verifies the UUID
                if (beforeSaveEncounter.equals(afterSaveEncounter)){

                    if (areAnyOfOrdersPublishable(beforeSaveEncounter.orders, afterSaveEncounter.orders)){

                        publishedEncounters.add(afterSaveEncounter.uuid);

                        break;

                    }

                }
            }
        }


        return publishedEncounters;
    }

    private boolean areAnyOfOrdersPublishable(ArrayList<DummyOrder> beforeSaveOrders, ArrayList<DummyOrder> afterSaveOrders) {
        for (DummyOrder beforeSaveOrder : beforeSaveOrders) {

            for (DummyOrder afterSaveOrder : afterSaveOrders) {

                if (IsOrderPublishable(beforeSaveOrder, afterSaveOrder)) {

                    return true;
                }
            }

        }
        return false;
    }

    private boolean IsOrderPublishable(DummyOrder beforeSaveOrder, DummyOrder afterSaveOrder) {

        if (beforeSaveOrder.dateCreated == null) {
            return true;
        }

        if (beforeSaveOrder.equals(afterSaveOrder)){

            if (!afterSaveOrder.voided == beforeSaveOrder.voided ){
                return true;
            }

        }
        return false;
    }
}
