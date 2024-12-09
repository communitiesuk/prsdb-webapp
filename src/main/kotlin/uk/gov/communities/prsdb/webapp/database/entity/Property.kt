package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.Status

@Entity
class Property : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    lateinit var status: Status
        private set

    var isActive: Boolean = false

    lateinit var propertyBuildType: PropertyType
        private set

    var hasGasSupply: Boolean? = null

    @OneToOne(optional = false)
    @JoinColumn(name = "address_id", nullable = false, foreignKey = ForeignKey(name = "FK_PROPERTY_ADDRESS"))
    lateinit var address: Address
        private set
}
