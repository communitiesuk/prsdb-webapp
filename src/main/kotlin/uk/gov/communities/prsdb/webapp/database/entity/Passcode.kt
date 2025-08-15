package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType

@Entity
class Passcode() : ModifiableAuditableEntity() {
    @Id
    lateinit var passcode: String
        private set

    @ManyToOne(optional = false)
    @JoinColumn(name = "local_authority_id", nullable = false, foreignKey = ForeignKey(name = "FK_PASSCODE_LA"))
    lateinit var localAuthority: LocalAuthority
        private set

    @OneToOne(optional = true)
    @Cascade(CascadeType.MERGE)
    @JoinColumn(name = "subject_identifier", nullable = true, foreignKey = ForeignKey(name = "FK_PASSCODE_1L_USER"))
    var baseUser: OneLoginUser? = null
        private set

    constructor(passcode: String, localAuthority: LocalAuthority, baseUser: OneLoginUser? = null) : this() {
        this.passcode = passcode
        this.localAuthority = localAuthority
        this.baseUser = baseUser
    }

    fun claimByUser(user: OneLoginUser) {
        this.baseUser = user
    }
}
