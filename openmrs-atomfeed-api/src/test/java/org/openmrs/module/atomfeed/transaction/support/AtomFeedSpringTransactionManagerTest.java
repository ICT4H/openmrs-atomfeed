package org.openmrs.module.atomfeed.transaction.support;

import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

public class AtomFeedSpringTransactionManagerTest {

    @Test
    public void executeWithTransactionShouldUseTheMostRecentlySetTransactionManager() {
        PlatformTransactionManager stale = Mockito.mock(PlatformTransactionManager.class);
        PlatformTransactionManager current = Mockito.mock(PlatformTransactionManager.class);
        Mockito.when(current.getTransaction(Mockito.any())).thenReturn(Mockito.mock(TransactionStatus.class));

        AtomFeedSpringTransactionManager txManager = new AtomFeedSpringTransactionManager(stale);
        txManager.setTransactionManager(current);

        txManager.executeWithTransaction(new AFTransactionWorkWithoutResult() {
            @Override
            protected void doInTransaction() {
            }

            @Override
            public PropagationDefinition getTxPropagationDefinition() {
                return PropagationDefinition.PROPAGATION_REQUIRED;
            }
        });

        Mockito.verify(current).getTransaction(Mockito.any());
        Mockito.verify(stale, Mockito.never()).getTransaction(Mockito.any());
    }

}
