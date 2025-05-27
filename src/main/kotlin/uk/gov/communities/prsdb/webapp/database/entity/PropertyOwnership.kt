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
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import java.time.OffsetDateTime

@Entity
class PropertyOwnership() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    var isActive: Boolean = false

    @Temporal(TemporalType.TIMESTAMP)
    lateinit var tenancyStartDate: OffsetDateTime
        private set

    @Column(nullable = false)
    lateinit var occupancyType: OccupancyType
        private set

    @Column(nullable = false)
    lateinit var ownershipType: OwnershipType

    @Column(nullable = false)
    var currentNumHouseholds: Int = 0

    @Column(nullable = false)
    var currentNumTenants: Int = 0

    @OneToOne(optional = false)
    @JoinColumn(
        name = "registration_number_id",
        nullable = false,
        foreignKey = ForeignKey(name = "FK_PROPERTY_OWNERSHIP_REGISTRATION_NUMBER"),
    )
    lateinit var registrationNumber: RegistrationNumber
        private set

    @ManyToOne
    @JoinColumn(
        name = "primary_landlord_id",
        nullable = false,
        foreignKey = ForeignKey(name = "FK_PROPERTY_OWNERSHIP_PRIMARY_LANDLORD"),
    )
    lateinit var primaryLandlord: Landlord
        private set

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "property_id",
        nullable = false,
        foreignKey = ForeignKey(name = "FK_PROPERTY_OWNERSHIP_PROPERTY"),
    )
    lateinit var property: Property
        private set

    @OneToOne
    @JoinColumn(name = "license_id", nullable = true, foreignKey = ForeignKey(name = "FK_PROPERTY_OWNERSHIP_LICENSE"))
    var license: License? = null

    @OneToOne
    @JoinColumn(
        name = "incomplete_compliance_form_id",
        nullable = true,
        foreignKey = ForeignKey(name = "FK_PROPERTY_OWNERSHIP_INCOMPLETE_COMPLIANCE_FORM"),
    )
    var incompleteComplianceForm: FormContext? = null
        private set

    constructor(
        occupancyType: OccupancyType,
        ownershipType: OwnershipType,
        currentNumHouseholds: Int,
        currentNumTenants: Int,
        registrationNumber: RegistrationNumber,
        primaryLandlord: Landlord,
        property: Property,
        license: License?,
        incompleteComplianceForm: FormContext?,
        isActive: Boolean = true,
    ) : this() {
        this.isActive = isActive
        this.occupancyType = occupancyType
        this.ownershipType = ownershipType
        this.currentNumHouseholds = currentNumHouseholds
        this.currentNumTenants = currentNumTenants
        this.registrationNumber = registrationNumber
        this.primaryLandlord = primaryLandlord
        this.property = property
        this.license = license
        this.incompleteComplianceForm = incompleteComplianceForm
    }

    val isOccupied: Boolean
        get() = currentNumTenants > 0
}
