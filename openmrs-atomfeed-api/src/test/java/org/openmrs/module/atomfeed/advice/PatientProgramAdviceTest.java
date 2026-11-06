package org.openmrs.module.atomfeed.advice;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.PatientProgram;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class PatientProgramAdviceTest {

    @Mock
    private PatientProgram returnValue;

    @Mock
    private PlatformTransactionManager platformTransactionManager;

    @Mock
    private AdministrationService administrationService;

    private MockedStatic<Context> contextMock;

    private PatientProgramAdvice patientProgramAdvice;

    @Before
    public void setUp() throws SQLException {
        contextMock = Mockito.mockStatic(Context.class);
        contextMock.when(() -> Context.getRegisteredComponents(PlatformTransactionManager.class))
                .thenReturn(Collections.singletonList(platformTransactionManager));
        contextMock.when(Context::getAdministrationService).thenReturn(administrationService);
        this.patientProgramAdvice = new PatientProgramAdvice();
    }

    @After
    public void tearDown() {
        contextMock.close();
    }

    @Test
    public void shouldCheckNameOfTheMethod() throws Throwable {
        Method method = this.getClass().getMethod("abcd");
        when(administrationService.getGlobalProperty("atomfeed.publish.eventsForPatientProgramStateChange")).thenReturn("true");
        patientProgramAdvice.afterReturning(returnValue, method, null, null);

        verify(administrationService, times(0)).getGlobalProperty("atomfeed.publish.eventsForPatientProgramStateChange");
    }

    @Test
    @Ignore
    public void shouldCheckForGlobalPropertyToRaiseTheEvent() throws Throwable {
        Method method = this.getClass().getMethod("savePatientProgram");
        when(administrationService.getGlobalProperty("atomfeed.publish.eventsForPatientProgramStateChange")).thenReturn("true");
        when(administrationService.getGlobalProperty("atomfeed.event.urlPatternForProgramStateChange")).thenReturn("/url/{uuid}");
        when(returnValue.getUuid()).thenReturn("1289313");
        patientProgramAdvice.afterReturning(returnValue, method, null, null);

        verify(administrationService, times(0)).getGlobalProperty("atomfeed.publish.eventsForPatientProgramStateChange");
    }

    public void abcd() {

    }

    public void savePatientProgram() {

    }
}
