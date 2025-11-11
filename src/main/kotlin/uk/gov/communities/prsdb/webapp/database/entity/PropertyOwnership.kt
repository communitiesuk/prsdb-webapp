package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType

@Entity
class PropertyOwnership() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    var isActive: Boolean = false

    @Column(nullable = false)
    lateinit var ownershipType: OwnershipType

    @Column(nullable = false)
    var currentNumHouseholds: Int = 0

    @Column(nullable = false)
    var currentNumTenants: Int = 0

    @OneToOne(optional = false)
    @JoinColumn(name = "registration_number_id", nullable = false, unique = true)
    lateinit var registrationNumber: RegistrationNumber
        private set

    @ManyToOne(optional = false)
    @JoinColumn(name = "primary_landlord_id", nullable = false)
    lateinit var primaryLandlord: Landlord
        private set

    @Column(nullable = false)
    lateinit var propertyBuildType: PropertyType

    @ManyToOne(optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    lateinit var address: Address
        private set

    @OneToOne(optional = true)
    @JoinColumn(name = "license_id", nullable = true, unique = true)
    var license: License? = null

    @OneToOne(optional = true)
    @JoinColumn(name = "incomplete_compliance_form_id", nullable = true, unique = true)
    var incompleteComplianceForm: FormContext? = null

    @OneToMany(mappedBy = "propertyOwnership", orphanRemoval = true)
    var certificateUploads: MutableSet<CertificateUpload> = mutableSetOf()
        private set

    constructor(
        ownershipType: OwnershipType,
        currentNumHouseholds: Int,
        currentNumTenants: Int,
        registrationNumber: RegistrationNumber,
        primaryLandlord: Landlord,
        propertyBuildType: PropertyType,
        address: Address,
        license: License?,
        incompleteComplianceForm: FormContext?,
        isActive: Boolean = true,
    ) : this() {
        this.ownershipType = ownershipType
        this.currentNumHouseholds = currentNumHouseholds
        this.currentNumTenants = currentNumTenants
        this.registrationNumber = registrationNumber
        this.primaryLandlord = primaryLandlord
        this.propertyBuildType = propertyBuildType
        this.address = address
        this.license = license
        this.incompleteComplianceForm = incompleteComplianceForm
        this.isActive = isActive
    }

    val isOccupied: Boolean
        get() = currentNumTenants > 0

    val isComplianceIncomplete: Boolean
        get() = incompleteComplianceForm != null
}
