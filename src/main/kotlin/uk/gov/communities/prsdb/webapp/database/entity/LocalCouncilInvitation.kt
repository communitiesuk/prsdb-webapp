package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
class LocalCouncilInvitation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : AuditableEntity() {
    @Column(nullable = false, unique = true)
    lateinit var token: UUID
        private set

    @Column(nullable = false)
    lateinit var invitedEmail: String
        private set

    @Column(nullable = false)
    var invitedAsAdmin: Boolean = false

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    lateinit var invitingCouncil: LocalCouncil
        private set

    constructor(
        token: UUID,
        email: String,
        invitingAuthority: LocalCouncil,
        invitedAsAdmin: Boolean = false,
    ) : this() {
        this.token = token
        this.invitedEmail = email
        this.invitingCouncil = invitingAuthority
        this.invitedAsAdmin = invitedAsAdmin
    }

    constructor(id: Long, token: UUID, email: String, invitingAuthority: LocalCouncil, invitedAsAdmin: Boolean = false) : this(id) {
        this.token = token
        this.invitedEmail = email
        this.invitingCouncil = invitingAuthority
        this.invitedAsAdmin = invitedAsAdmin
    }
}
