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
class LettingAgentAccess(
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
    @JoinColumn(name = "property_ownership_id", nullable = false)
    lateinit var propertyOwnership: PropertyOwnership
        private set

    constructor(
        token: UUID,
        invitedEmail: String,
        propertyOwnership: PropertyOwnership,
    ) : this() {
        this.token = token
        this.invitedEmail = invitedEmail
        this.propertyOwnership = propertyOwnership
    }

    constructor(
        id: Long,
        token: UUID,
        invitedEmail: String,
        propertyOwnership: PropertyOwnership,
    ) : this(id) {
        this.token = token
        this.invitedEmail = invitedEmail
        this.propertyOwnership = propertyOwnership
    }
}
