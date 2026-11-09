package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

/**
 * Someone further up the ownership chain than the landlord. A row with no name, email or address
 * means "no details known".
 */
@Entity
class SuperiorLandlord() :
    ModifiableAuditableEntity(),
    AssociatedParty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    override var name: String? = null

    override var email: String? = null

    @ManyToOne
    @JoinColumn(name = "address_id")
    override var address: Address? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_ownership_id", nullable = false)
    lateinit var propertyOwnership: PropertyOwnership
        private set

    constructor(
        propertyOwnership: PropertyOwnership,
        name: String? = null,
        email: String? = null,
        address: Address? = null,
    ) : this() {
        this.propertyOwnership = propertyOwnership
        this.name = name
        this.email = email
        this.address = address
    }

    // TODO PDJB-1997: use this on the property record
    fun detailsKnown(): Boolean = name != null
}
