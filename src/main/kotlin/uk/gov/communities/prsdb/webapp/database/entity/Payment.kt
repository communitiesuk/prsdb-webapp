package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import java.time.OffsetDateTime

@Entity
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) : AuditableEntity() {
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    lateinit var paymentDateTime: OffsetDateTime
        private set

    @Column(nullable = false)
    var paymentAmount: Double = 0.00

    @OneToMany(mappedBy = "payment")
    lateinit var propertyOwnerships: MutableSet<PropertyOwnership>
        private set
}
