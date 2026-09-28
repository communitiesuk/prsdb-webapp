package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole

@Entity
@Table(
    name = "organisational_landlord_user",
    uniqueConstraints = [UniqueConstraint(columnNames = ["organisation_landlord_id", "subject_identifier"])],
)
class OrganisationalLandlordUser() : AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(optional = false)
    @JoinColumn(name = "organisation_landlord_id", nullable = false)
    lateinit var organisationalLandlord: OrganisationalLandlord

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_identifier", nullable = false)
    lateinit var baseUser: PrsdbUser

    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false)
    lateinit var email: String

    @Enumerated
    @Column(name = "role", nullable = false)
    lateinit var role: OrganisationalLandlordUserRole

    constructor(
        organisationalLandlord: OrganisationalLandlord,
        baseUser: PrsdbUser,
        name: String,
        email: String,
        role: OrganisationalLandlordUserRole,
    ) : this() {
        this.organisationalLandlord = organisationalLandlord
        this.baseUser = baseUser
        this.name = name
        this.email = email
        this.role = role
        organisationalLandlord.addOrganisationalLandlordUser(this)
    }
}
