package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPayment
import java.time.Instant
import java.time.LocalDate

@Entity
class Payment() : ModifiableAuditableEntity() {
    @Id
    lateinit var paymentId: String

    @Column
    var amountInPence: Int = 0

    @Column(nullable = false)
    lateinit var reference: String

    @Column(nullable = false)
    lateinit var paymentCreatedAt: Instant

    @Column(nullable = false)
    lateinit var forPeriodEnding: LocalDate

    @Column(nullable = false)
    lateinit var status: PaymentStatus

    @ManyToOne
    var payingUser: PrsdbUser? = null

    @ManyToOne
    var associatedIncompleteProperty: LandlordIncompleteProperty? = null
        private set

    @ManyToOne
    var associatedProperty: PropertyOwnership? = null
        private set

    public fun associateWithProperty(property: PropertyOwnership) {
        associatedProperty = property
        associatedIncompleteProperty = null
    }

    constructor(
        paymentId: String,
        amountInPence: Int,
        reference: String,
        paymentCreatedAt: Instant,
        forPeriodEnding: LocalDate,
        status: PaymentStatus,
        incompleteProperty: LandlordIncompleteProperty,
    ) : this() {
        this.paymentId = paymentId
        this.amountInPence = amountInPence
        this.reference = reference
        this.paymentCreatedAt = paymentCreatedAt
        this.forPeriodEnding = forPeriodEnding
        this.status = status
        associatedIncompleteProperty = incompleteProperty
        this.payingUser = incompleteProperty.user
    }

    companion object {
        fun fromGovUkPay(
            payment: GovUkPayPayment,
            periodEnding: LocalDate,
            incompleteProperty: LandlordIncompleteProperty,
        ): Payment =
            Payment(
                payment.paymentId,
                payment.amount,
                payment.reference,
                payment.createdDate,
                periodEnding,
                PaymentStatus.fromGovUKPayStatus(payment.state.status),
                incompleteProperty,
            )
    }
}
