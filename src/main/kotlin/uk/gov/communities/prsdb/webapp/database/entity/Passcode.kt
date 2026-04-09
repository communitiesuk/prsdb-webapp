package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
class Passcode() : ModifiableAuditableEntity() {
    @Id
    lateinit var passcode: String
        private set

    @OneToOne(optional = true)
    @JoinColumn(name = "subject_identifier", nullable = true, unique = true)
    var baseUser: PrsdbUser? = null
        private set

    constructor(passcode: String, baseUser: PrsdbUser? = null) : this() {
        this.passcode = passcode
        this.baseUser = baseUser
    }

    fun claimByUser(user: PrsdbUser) {
        this.baseUser = user
    }
}
