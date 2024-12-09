package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import java.time.OffsetDateTime

@Entity
class Payment : AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @Temporal(TemporalType.TIMESTAMP)
    lateinit var paymentDateTime: OffsetDateTime
        private set

    var paymentAmount: Double = 0.00

    @OneToOne(optional = false)
    @JoinColumn(name = "property_ownership_id", nullable = false, foreignKey = ForeignKey(name = "FK_PAYMENT_PROPERTY_OWNERSHIP"))
    lateinit var propertyOwnership: PropertyOwnership
        private set
}
