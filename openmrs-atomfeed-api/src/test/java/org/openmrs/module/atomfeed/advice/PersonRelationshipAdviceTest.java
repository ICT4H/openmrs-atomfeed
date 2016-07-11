package org.openmrs.module.atomfeed.advice;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Relationship;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, Method.class})
public class PersonRelationshipAdviceTest {

    @Mock
    private Relationship returnValue;

    @Mock
    private PlatformTransactionManager platformTransactionManager;

    @Mock
    private AdministrationService administrationService;

    private PersonRelationshipAdvice personRelationshipAdvice;

    @Before
    public void setUp() throws SQLException {
        mockStatic(Context.class);
        PowerMockito.when(Context.getRegisteredComponents(PlatformTransactionManager.class)).thenReturn(Collections.singletonList(platformTransactionManager));

        this.personRelationshipAdvice = new PersonRelationshipAdvice();
    }

    @Test
    public void shouldCheckNameOfTheMethod() throws Throwable {
        Method method = this.getClass().getMethod("abcd");
        PowerMockito.when(Context.getRegisteredComponents(PlatformTransactionManager.class)).thenReturn(Collections.singletonList(platformTransactionManager));
        PowerMockito.when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty("atomfeed.publish.eventsForPatientRelationshipChange")).thenReturn("true");
        personRelationshipAdvice.afterReturning(returnValue, method, null, null);

        verify(administrationService, times(0)).getGlobalProperty("atomfeed.publish.eventsForPatientRelationshipChange");
    }

    @Test
    @Ignore
    public void shouldCheckForGlobalPropertyToRaiseTheEvent() throws Throwable {
        Method method = this.getClass().getMethod("saveRelationship");
        mockStatic(Context.class);
        PowerMockito.when(Context.getRegisteredComponents(PlatformTransactionManager.class)).thenReturn(Collections.singletonList(platformTransactionManager));
        PowerMockito.when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty("atomfeed.publish.eventsForPatientRelationshipChange")).thenReturn("true");
        when(administrationService.getGlobalProperty("atomfeed.event.urlPatternForPatientRelationshipChange")).thenReturn("/url/%s");
        when(returnValue.getUuid()).thenReturn("1289313");

        personRelationshipAdvice.afterReturning(returnValue, method, null, null);

        verify(administrationService, times(1)).getGlobalProperty("atomfeed.publish.eventsForPatientRelationshipChange");
    }

    public void abcd() {

    }

    public void saveRelationship() {

    }
}