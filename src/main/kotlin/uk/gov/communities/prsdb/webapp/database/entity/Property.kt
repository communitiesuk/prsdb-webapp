package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus

@Entity
class Property(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) : ModifiableAuditableEntity() {
    @Column(nullable = false)
    lateinit var status: RegistrationStatus
        private set

    @Column(nullable = false)
    var isActive: Boolean = false

    @Column(nullable = false)
    lateinit var propertyBuildType: PropertyType
        private set

    @OneToOne(optional = false)
    @JoinColumn(name = "address_id", nullable = false, foreignKey = ForeignKey(name = "FK_PROPERTY_ADDRESS"))
    lateinit var address: Address
        private set

    constructor(id: Long, address: Address, isActive: Boolean) : this(id) {
        this.address = address
        this.isActive = isActive
    }
}
