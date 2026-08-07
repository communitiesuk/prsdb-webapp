package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import java.time.LocalDate

@Entity
@Table(name = "organisation_governing_body_member")
class OrganisationGoverningBodyMember() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(optional = false)
    @JoinColumn(name = "organisation_landlord_id", nullable = false)
    lateinit var organisationalLandlord: OrganisationalLandlord

    @Column(nullable = false)
    lateinit var type: GoverningBodyMemberType

    @Column(nullable = false)
    lateinit var name: String

    @Column(name = "date_of_birth", nullable = false)
    lateinit var dateOfBirth: LocalDate

    @ManyToOne(optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    lateinit var address: Address

    constructor(
        organisationalLandlord: OrganisationalLandlord,
        type: GoverningBodyMemberType,
        name: String,
        dateOfBirth: LocalDate,
        address: Address,
    ) : this() {
        this.organisationalLandlord = organisationalLandlord
        this.type = type
        this.name = name
        this.dateOfBirth = dateOfBirth
        this.address = address
    }
}
