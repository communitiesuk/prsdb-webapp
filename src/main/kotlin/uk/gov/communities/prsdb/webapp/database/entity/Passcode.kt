package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
class Passcode() : ModifiableAuditableEntity() {
    @Id
    lateinit var passcode: String
        private set

    @OneToOne(optional = false)
    @JoinColumn(name = "local_authority_id", nullable = false, foreignKey = ForeignKey(name = "FK_PASSCODE_LA"))
    lateinit var localAuthority: LocalAuthority
        private set

    @OneToOne(optional = true)
    @JoinColumn(name = "subject_identifier", nullable = true, foreignKey = ForeignKey(name = "FK_PASSCODE_1L_USER"))
    lateinit var baseUser: OneLoginUser
        private set

    constructor(passcode: String, localAuthority: LocalAuthority, baseUser: OneLoginUser? = null) : this() {
        this.passcode = passcode
        this.localAuthority = localAuthority
        if (baseUser != null) {
            this.baseUser = baseUser
        }
    }
}
