package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(name = "uc_ownerhship_link_uniqueness", columnNames = ["landlord_id", "landlordship_id"])])
class OwnershipLink(
    @ManyToOne(optional = false)
    @JoinColumn(name = "landlord_id", nullable = false)
    var landlord: Landlord,
    @ManyToOne(optional = false)
    @JoinColumn(name = "landlordship_id", nullable = false)
    var propertyOwnership: PropertyOwnership,
) : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
