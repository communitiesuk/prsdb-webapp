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
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.Address.Companion.SINGLE_LINE_ADDRESS_LENGTH
import java.math.BigDecimal

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

    @OneToOne(optional = true, orphanRemoval = true)
    @JoinColumn(name = "license_id", nullable = true, unique = true)
    var license: License? = null

    @OneToOne(optional = true, orphanRemoval = true)
    @JoinColumn(name = "incomplete_compliance_form_id", nullable = true, unique = true)
    var incompleteComplianceForm: FormContext? = null

    @Column(nullable = false, insertable = false, updatable = false, length = SINGLE_LINE_ADDRESS_LENGTH)
    private lateinit var singleLineAddress: String

    @Column(insertable = false, updatable = false)
    private val localCouncilId: Int? = null

    // We use this generated duplicate of isActive to influence the query planner into using the GIST index (as opposed to the GIN index)
    // for searches where it's likely to be more efficient
    @Column(nullable = false, insertable = false, updatable = false)
    private val isActiveDuplicateForGistIndex: Boolean = false

    @OneToOne(mappedBy = "propertyOwnership", orphanRemoval = true)
    private val propertyCompliance: PropertyCompliance? = null

    @OneToMany(mappedBy = "propertyOwnership", orphanRemoval = true)
    private val certificateUploads: MutableSet<CertificateUpload> = mutableSetOf()

    @OneToMany(mappedBy = "registeredOwnership", orphanRemoval = true)
    private val jointLandlordInvitations: MutableSet<JointLandlordInvitation> = mutableSetOf()

    var numBedrooms: Int? = null

    var billsIncludedList: String? = null

    var customBillsIncluded: String? = null

    var furnishedStatus: FurnishedStatus? = null

    var rentFrequency: RentFrequency? = null

    var customRentFrequency: String? = null

    @Column(precision = 9, scale = 2)
    var rentAmount: BigDecimal? = null

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
        numBedrooms: Int? = null,
        billsIncludedList: String? = null,
        customBillsIncluded: String? = null,
        furnishedStatus: FurnishedStatus? = null,
        rentFrequency: RentFrequency? = null,
        customRentFrequency: String? = null,
        rentAmount: BigDecimal? = null,
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
        this.numBedrooms = numBedrooms
        this.billsIncludedList = billsIncludedList
        this.customBillsIncluded = customBillsIncluded
        this.furnishedStatus = furnishedStatus
        this.rentFrequency = rentFrequency
        this.customRentFrequency = customRentFrequency
        this.rentAmount = rentAmount
    }

    // TODO PRSD-1550 once Old PropertyRegistration journey is removed revert this check to just currentNumTenants > 0
    val isOccupied: Boolean
        get() =
            currentNumTenants > 0 &&
                currentNumHouseholds > 0 &&
                numBedrooms != null &&
                numBedrooms!! > 0 &&
                furnishedStatus != null &&
                rentFrequency != null &&
                rentAmount != null

    val isComplianceIncomplete: Boolean
        get() = incompleteComplianceForm != null

    val rentIncludesBills: Boolean
        get() = billsIncludedList != null
}
