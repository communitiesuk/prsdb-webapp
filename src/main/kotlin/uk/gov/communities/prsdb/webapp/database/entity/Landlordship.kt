package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType

@Embeddable
class Landlordship() {
    @Column(nullable = false)
    var isActive: Boolean = false

    @Column(nullable = false)
    lateinit var ownershipType: OwnershipType

    @OneToOne(optional = false)
    @JoinColumn(name = "registration_number_id", nullable = false, unique = true)
    lateinit var registrationNumber: RegistrationNumber
        private set

    @ManyToOne(optional = false)
    @JoinColumn(name = "primary_landlord_id", nullable = false)
    lateinit var primaryLandlord: Landlord
        private set

    @OneToOne(optional = true, orphanRemoval = true)
    @JoinColumn(name = "license_id", nullable = true, unique = true)
    var license: License? = null

    // We use this generated duplicate of isActive to influence the query planner into using the GIST index
    // (as opposed to the GIN index) for searches where it's likely to be more efficient
    @Column(nullable = false, insertable = false, updatable = false)
    private val isActiveDuplicateForGistIndex: Boolean = false

    constructor(
        ownershipType: OwnershipType,
        registrationNumber: RegistrationNumber,
        primaryLandlord: Landlord,
        license: License?,
        isActive: Boolean = true,
    ) : this() {
        this.ownershipType = ownershipType
        this.registrationNumber = registrationNumber
        this.primaryLandlord = primaryLandlord
        this.license = license
        this.isActive = isActive
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Landlordship) return false
        return isActive == other.isActive &&
            ownershipType == other.ownershipType &&
            registrationNumber == other.registrationNumber &&
            primaryLandlord == other.primaryLandlord &&
            license == other.license
    }

    override fun hashCode(): Int {
        var result = isActive.hashCode()
        result = 31 * result + ownershipType.hashCode()
        result = 31 * result + registrationNumber.hashCode()
        result = 31 * result + primaryLandlord.hashCode()
        result = 31 * result + (license?.hashCode() ?: 0)
        return result
    }
}
