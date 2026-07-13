package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "landlord_type", discriminatorType = DiscriminatorType.INTEGER)
sealed class Landlord : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @OneToOne(optional = false)
    @JoinColumn(name = "registration_number_id", nullable = false, unique = true)
    lateinit var registrationNumber: RegistrationNumber
        protected set

    @OneToMany(mappedBy = "landlord", orphanRemoval = true)
    private var ownershipLinks: MutableSet<OwnershipLink> = mutableSetOf()

    val landlordships: Set<PropertyOwnership> get() = ownershipLinks.map { it.propertyOwnership }.toSet()
}
