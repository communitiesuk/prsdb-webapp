package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import java.time.OffsetDateTime

@Entity
class PropertyOwnership : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    var isActive: Boolean = false

    @Temporal(TemporalType.TIMESTAMP)
    lateinit var tenancyStartDate: OffsetDateTime
        private set

    lateinit var occupancyType: OccupancyType
        private set

    lateinit var landlordType: LandlordType
        private set

    var currentNumHouseholds: Int = 0
        private set

    @OneToOne(optional = false)
    @JoinColumn(
        name = "registration_number_id",
        nullable = false,
        foreignKey = ForeignKey(name = "FK_PROPERTY_OWNERSHIP_REGISTRATION_NUMBER"),
    )
    lateinit var registrationNumber: RegistrationNumber
        private set

    @ManyToMany(mappedBy = "propertyOwnerships")
    lateinit var landlords: MutableSet<Landlord>
        private set

    @OneToOne(optional = false)
    @JoinColumn(name = "property_id", nullable = false, foreignKey = ForeignKey(name = "FK_PROPERTY_OWNERSHIP_PROPERTY"))
    lateinit var property: Property
        private set

    @ManyToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    lateinit var payment: Payment
        private set
}
