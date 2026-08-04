package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "organisation_landlord_user",
    uniqueConstraints = [UniqueConstraint(columnNames = ["organisation_landlord_id", "subject_identifier"])],
)
class OrganisationLandlordUser() : AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(optional = false)
    @JoinColumn(name = "organisation_landlord_id", nullable = false)
    lateinit var organisationLandlord: OrganisationLandlord

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_identifier", nullable = false)
    lateinit var baseUser: PrsdbUser

    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false)
    lateinit var email: String

    constructor(
        organisationLandlord: OrganisationLandlord,
        baseUser: PrsdbUser,
        name: String,
        email: String,
    ) : this() {
        this.organisationLandlord = organisationLandlord
        this.baseUser = baseUser
        this.name = name
        this.email = email
        organisationLandlord.addOrganisationLandlordUser(this)
    }
}
