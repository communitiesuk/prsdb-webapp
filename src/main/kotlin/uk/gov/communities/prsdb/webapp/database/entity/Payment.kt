package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
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

    @OneToMany(mappedBy = "payment")
    lateinit var propertyOwnerships: MutableSet<PropertyOwnership>
        private set
}
