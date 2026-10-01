package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import java.util.UUID

@Entity
class OrganisationalLandlordInvitation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : ModifiableAuditableEntity() {
    @Column(nullable = false, unique = true)
    lateinit var token: UUID
        private set

    @Column(nullable = false)
    lateinit var invitedEmail: String
        private set

    @ManyToOne(optional = false)
    @JoinColumn(name = "organisation_landlord_id", nullable = false)
    lateinit var organisationalLandlord: OrganisationalLandlord
        private set

    @Column(nullable = false)
    lateinit var role: OrganisationalLandlordUserRole
        private set

    @Column(nullable = false)
    var invitationExpiredEmailSent: Boolean = false
        private set

    @Column(nullable = false)
    var isHidden: Boolean = false

    constructor(
        token: UUID,
        invitedEmail: String,
        organisationalLandlord: OrganisationalLandlord,
        role: OrganisationalLandlordUserRole,
    ) : this() {
        this.token = token
        this.invitedEmail = invitedEmail
        this.organisationalLandlord = organisationalLandlord
        this.role = role
    }

    constructor(
        id: Long,
        token: UUID,
        invitedEmail: String,
        organisationalLandlord: OrganisationalLandlord,
        role: OrganisationalLandlordUserRole,
    ) : this(id) {
        this.token = token
        this.invitedEmail = invitedEmail
        this.organisationalLandlord = organisationalLandlord
        this.role = role
    }

    fun markAsExpiredEmailSent() {
        invitationExpiredEmailSent = true
    }
}
