package org.openmrs.module.atomfeed.advice;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.VisitService;
import org.openmrs.api.impl.VisitServiceImpl;
import org.openmrs.module.atomfeed.advisor.OrderAroundAdvisor;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class OrderAroundAdvisorTest {

    private OrderAroundAdvisor orderAroundAdvisor;

    @Before
    public void setUp(){
        orderAroundAdvisor = new OrderAroundAdvisor();
    }

    @Test
    public void shouldExecuteAdviceWhenMethodSaveVisitIsBeingCalled() throws NoSuchMethodException, ClassNotFoundException {
        VisitService visitService = new VisitServiceImpl();

        Class<?> visitClass = Class.forName("org.openmrs.Visit");
        Method saveVisit = visitService.getClass().getMethod("saveVisit", visitClass);

        assertTrue(orderAroundAdvisor.matches(saveVisit, visitService.getClass()));

    }

    @Test
    public void shouldNotExecuteAdviceWhenOtherMethodIsBeingCalled() throws NoSuchMethodException {
        VisitService visitService = new VisitServiceImpl();

        Method getAllVisitTypes = visitService.getClass().getMethod("getAllVisitTypes");

        assertFalse(orderAroundAdvisor.matches(getAllVisitTypes, visitService.getClass()));

    }
}
