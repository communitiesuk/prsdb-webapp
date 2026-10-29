package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import uk.gov.communities.prsdb.webapp.constants.enums.AdditionalPropertyOwnerType

@Entity
class AdditionalPropertyOwner() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    lateinit var ownerType: AdditionalPropertyOwnerType

    var name: String? = null

    var email: String? = null

    @ManyToOne
    @JoinColumn(name = "address_id")
    var address: Address? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_ownership_id", nullable = false)
    lateinit var propertyOwnership: PropertyOwnership
        private set

    constructor(
        ownerType: AdditionalPropertyOwnerType,
        propertyOwnership: PropertyOwnership,
        name: String? = null,
        email: String? = null,
        address: Address? = null,
    ) : this() {
        this.ownerType = ownerType
        this.propertyOwnership = propertyOwnership
        this.name = name
        this.email = email
        this.address = address
    }
}
