package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import java.time.OffsetDateTime

@Entity
class PropertyOwnership(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) : ModifiableAuditableEntity() {
    var isActive: Boolean = false

    @Temporal(TemporalType.TIMESTAMP)
    lateinit var tenancyStartDate: OffsetDateTime
        private set

    @Column(nullable = false)
    lateinit var occupancyType: OccupancyType
        private set

    @Column(nullable = false)
    lateinit var landlordType: LandlordType
        private set

    @Column(nullable = false)
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

    @ManyToOne
    @JoinColumn(name = "primary_landlord_id", nullable = false, foreignKey = ForeignKey(name = "FK_PROPERTY_OWNERSHIP_PRIMARY_LANDLORD"))
    lateinit var primaryLandlord: Landlord
        private set

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id", nullable = false, foreignKey = ForeignKey(name = "FK_PROPERTY_OWNERSHIP_PROPERTY"))
    lateinit var property: Property
        private set

    @ManyToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false, foreignKey = ForeignKey(name = "FK_PROPERTY_OWNERSHIP_PAYMENT"))
    lateinit var payment: Payment
        private set

    constructor(id: Long, isActive: Boolean) : this(id) {
        this.isActive = isActive
    }
}
