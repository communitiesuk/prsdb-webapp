package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import java.time.Instant
import java.util.UUID

@Entity
@Audited
class LettingAgentAccess(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : ModifiableAuditableEntity() {
    @Column(nullable = false, unique = true)
    @NotAudited
    lateinit var token: UUID
        private set

    @Column(nullable = false)
    lateinit var invitedEmail: String
        private set

    @OneToOne(optional = false)
    @JoinColumn(name = "property_ownership_id", nullable = false, unique = true)
    lateinit var propertyOwnership: PropertyOwnership
        private set

    @Column(name = "encoded_password")
    @NotAudited
    var encodedPassword: String? = null
        private set

    // Audited in place of encodedPassword, so the audit log shows when the password was set
    var passwordSetDate: Instant? = null
        private set

    fun setEncodedPassword(encoded: String) {
        this.encodedPassword = encoded
        this.passwordSetDate = Instant.now()
    }

    constructor(
        token: UUID,
        invitedEmail: String,
        propertyOwnership: PropertyOwnership,
    ) : this() {
        this.token = token
        this.invitedEmail = invitedEmail
        this.propertyOwnership = propertyOwnership
    }
}
