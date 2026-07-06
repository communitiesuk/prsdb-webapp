package uk.gov.communities.prsdb.webapp.helpers

import org.springframework.transaction.support.TransactionSynchronizationManager
import uk.gov.communities.prsdb.webapp.helpers.TransactionHelper.Companion.runAfterTransactionCommits
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransactionHelperTests {
    @AfterTest
    fun tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization()
        }
    }

    @Test
    fun `runAfterTransactionCommits runs the action immediately when no transaction synchronization is active`() {
        var ran = false

        runAfterTransactionCommits { ran = true }

        assertTrue(ran)
    }

    @Test
    fun `runAfterTransactionCommits defers the action until commit when transaction synchronization is active`() {
        TransactionSynchronizationManager.initSynchronization()
        var ran = false

        runAfterTransactionCommits { ran = true }

        assertFalse(ran)

        val synchronizations = TransactionSynchronizationManager.getSynchronizations()
        assertEquals(1, synchronizations.size)
        synchronizations.forEach { it.afterCommit() }

        assertTrue(ran)
    }
}
