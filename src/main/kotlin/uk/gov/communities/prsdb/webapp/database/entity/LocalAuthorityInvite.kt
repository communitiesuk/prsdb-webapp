package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import java.util.UUID

@Entity
class LocalAuthorityInvite() : AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @Column(nullable = false, unique = true)
    var token: UUID? = null
        private set

    @OneToOne(optional = false)
    @JoinColumn(nullable = false, foreignKey = ForeignKey(name = "FK_LA_INVITE_LA"))
    lateinit var invitingAuthority: LocalAuthority
        private set

    constructor(token: UUID, invitingAuthority: LocalAuthority) : this() {
        this.token = token
        this.invitingAuthority = invitingAuthority
    }
}
