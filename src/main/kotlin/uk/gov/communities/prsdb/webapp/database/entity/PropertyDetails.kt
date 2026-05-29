package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.Address.Companion.SINGLE_LINE_ADDRESS_LENGTH

@Embeddable
class PropertyDetails() {
    @ManyToOne(optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    lateinit var address: Address
        private set

    @Column(nullable = false)
    lateinit var propertyBuildType: PropertyType

    var numBedrooms: Int? = null

    var customPropertyType: String? = null

    @Column(nullable = false, insertable = false, updatable = false, length = SINGLE_LINE_ADDRESS_LENGTH)
    private lateinit var singleLineAddress: String

    @Column(insertable = false, updatable = false)
    private val localCouncilId: Int? = null

    constructor(
        address: Address,
        propertyBuildType: PropertyType,
        numBedrooms: Int? = null,
        customPropertyType: String? = null,
    ) : this() {
        this.address = address
        this.propertyBuildType = propertyBuildType
        this.numBedrooms = numBedrooms
        this.customPropertyType = customPropertyType
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PropertyDetails) return false
        return address == other.address &&
            propertyBuildType == other.propertyBuildType &&
            numBedrooms == other.numBedrooms &&
            customPropertyType == other.customPropertyType
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + propertyBuildType.hashCode()
        result = 31 * result + (numBedrooms ?: 0)
        result = 31 * result + (customPropertyType?.hashCode() ?: 0)
        return result
    }
}
