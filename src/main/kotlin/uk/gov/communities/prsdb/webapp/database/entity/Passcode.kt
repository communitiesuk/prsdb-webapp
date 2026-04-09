package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne

@Entity
class Passcode() : ModifiableAuditableEntity() {
    @Id
    lateinit var passcode: String
        private set

    @ManyToOne(optional = false)
    @JoinColumn(name = "local_council_id", nullable = false)
    lateinit var localCouncil: LocalCouncil
        private set

    @OneToOne(optional = true)
    @JoinColumn(name = "subject_identifier", nullable = true, unique = true)
    var baseUser: PrsdbUser? = null
        private set

    constructor(passcode: String, localCouncil: LocalCouncil, baseUser: PrsdbUser? = null) : this() {
        this.passcode = passcode
        this.localCouncil = localCouncil
        this.baseUser = baseUser
    }

    fun claimByUser(user: PrsdbUser) {
        this.baseUser = user
    }
}
