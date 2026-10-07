package uk.gov.communities.prsdb.webapp.database.repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperty
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import java.time.Instant

interface PaymentRepository : JpaRepository<Payment, String> {
    fun findAllByAssociatedIncompleteProperty(associatedIncompleteProperty: LandlordIncompleteProperty): List<Payment>

    @Modifying
    @Transactional
    @Query(
        "UPDATE Payment p SET p.status = :newStatus, p.lastModifiedDate = :now " +
            "WHERE p.paymentId = :paymentId AND p.status IN :expectedStatuses",
    )
    fun updateStatusIfCurrentStatusIn(
        paymentId: String,
        expectedStatuses: Collection<PaymentStatus>,
        newStatus: PaymentStatus,
        now: Instant,
    ): Int

    @Query("SELECT p.status FROM Payment p WHERE p.paymentId = :paymentId")
    fun findStatusByPaymentId(paymentId: String): PaymentStatus?
}
