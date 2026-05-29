package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import java.math.BigDecimal
import java.time.LocalDate

@Entity
class PropertyOwnership() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Embedded
    lateinit var landlordship: Landlordship

    @Embedded
    lateinit var propertyDetails: PropertyDetails

    @Embedded
    lateinit var tenancyDetails: TenancyDetails

    @OneToOne(mappedBy = "propertyOwnership", orphanRemoval = true)
    val propertyCompliance: PropertyCompliance? = null

    @OneToMany(mappedBy = "registeredOwnership", orphanRemoval = true)
    private val jointLandlordInvitations: MutableSet<JointLandlordInvitation> = mutableSetOf()

    constructor(
        ownershipType: OwnershipType,
        currentNumHouseholds: Int,
        currentNumTenants: Int,
        registrationNumber: RegistrationNumber,
        primaryLandlord: Landlord,
        propertyBuildType: PropertyType,
        address: Address,
        license: License?,
        isActive: Boolean = true,
        numBedrooms: Int? = null,
        billsIncludedList: String? = null,
        customBillsIncluded: String? = null,
        furnishedStatus: FurnishedStatus? = null,
        rentFrequency: RentFrequency? = null,
        customRentFrequency: String? = null,
        rentAmount: BigDecimal? = null,
        customPropertyType: String? = null,
        lastOccupiedDate: LocalDate? = null,
    ) : this() {
        this.landlordship =
            Landlordship(
                ownershipType = ownershipType,
                registrationNumber = registrationNumber,
                primaryLandlord = primaryLandlord,
                license = license,
                isActive = isActive,
            )
        this.propertyDetails =
            PropertyDetails(
                address = address,
                propertyBuildType = propertyBuildType,
                numBedrooms = numBedrooms,
                customPropertyType = customPropertyType,
            )
        this.tenancyDetails =
            TenancyDetails(
                currentNumHouseholds = currentNumHouseholds,
                currentNumTenants = currentNumTenants,
                lastOccupiedDate = lastOccupiedDate,
                furnishedStatus = furnishedStatus,
                rentFrequency = rentFrequency,
                customRentFrequency = customRentFrequency,
                rentAmount = rentAmount,
                billsIncludedList = billsIncludedList,
                customBillsIncluded = customBillsIncluded,
            )
    }
}
