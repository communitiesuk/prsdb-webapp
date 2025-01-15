package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
class LocalAuthorityInvitation(
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

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, foreignKey = ForeignKey(name = "FK_LA_INVITATION_LA"))
    lateinit var invitingAuthority: LocalAuthority
        private set

    constructor(token: UUID, email: String, invitingAuthority: LocalAuthority) : this() {
        this.token = token
        this.invitedEmail = email
        this.invitingAuthority = invitingAuthority
    }

    constructor(id: Long, token: UUID, email: String, invitingAuthority: LocalAuthority) : this(id) {
        this.token = token
        this.invitedEmail = email
        this.invitingAuthority = invitingAuthority
    }
}
