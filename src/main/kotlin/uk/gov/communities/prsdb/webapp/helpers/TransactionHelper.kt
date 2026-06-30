package uk.gov.communities.prsdb.webapp.helpers

import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

// TODO PDJB-1232: Use the transaction helper anywhere we need to send an email iff a database update is committed.
class TransactionHelper {
    companion object {
        fun runAfterTransactionCommits(action: () -> Unit) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                    object : TransactionSynchronization {
                        override fun afterCommit() = action()
                    },
                )
            } else {
                action()
            }
        }
    }
}
