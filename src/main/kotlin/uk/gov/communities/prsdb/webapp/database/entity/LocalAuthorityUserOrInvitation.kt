package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.io.Serializable

@Entity
@Table(name = "local_authority_user_or_invitation")
@IdClass(LocalAuthorityUserOrInvitation.CompositeKey::class)
class LocalAuthorityUserOrInvitation() {
    // Types used for IdClass must:
    // - implement equals and hashCode (hence being a data class)
    // - have a no-args constructor (hence having default values)
    // - be serializable
    data class CompositeKey(
        val id: Long = 0,
        val entityType: String = "",
    ) : Serializable

    @Id
    var id: Long = 0

    @Id
    lateinit var entityType: String
        private set

    @Column(nullable = false)
    lateinit var name: String
        private set

    @Column(nullable = false)
    var isManager: Boolean = false
        private set

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "local_authority_id", referencedColumnName = "id")
    lateinit var localAuthority: LocalAuthority
        private set

    constructor(id: Long, entityType: String, name: String, isManager: Boolean, localAuthority: LocalAuthority) : this() {
        this.id = id
        this.entityType = entityType
        this.name = name
        this.isManager = isManager
        this.localAuthority = localAuthority
    }
}
