package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
class LocalAuthorityUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : ModifiableAuditableEntity() {
    @OneToOne(optional = false)
    @JoinColumn(name = "subject_identifier", nullable = false, foreignKey = ForeignKey(name = "FK_LA_USER_1L_USER"))
    lateinit var baseUser: OneLoginUser
        private set

    @Column(nullable = false)
    var isManager: Boolean = false

    @Column(nullable = false)
    var name: String = ""

    @Column(nullable = false)
    var email: String = ""

    @OneToOne(optional = false)
    @JoinColumn(name = "local_authority_id", nullable = false, foreignKey = ForeignKey(name = "FK_LA_USER_LA"))
    lateinit var localAuthority: LocalAuthority
        private set

    @Column(nullable = false)
    var hasAcceptedPrivacyNotice: Boolean = false
        private set

    constructor(
        id: Long,
        baseUser: OneLoginUser,
        isManager: Boolean,
        localAuthority: LocalAuthority,
        name: String,
        email: String,
        hasAcceptedPrivacyNotice: Boolean,
    ) :
        this(id) {
        this.baseUser = baseUser
        this.isManager = isManager
        this.localAuthority = localAuthority
        this.name = name
        this.email = email
        this.hasAcceptedPrivacyNotice = hasAcceptedPrivacyNotice
    }

    constructor(
        baseUser: OneLoginUser,
        isManager: Boolean,
        localAuthority: LocalAuthority,
        name: String,
        email: String,
        hasAcceptedPrivacyNotice: Boolean,
    ) : this() {
        this.baseUser = baseUser
        this.isManager = isManager
        this.localAuthority = localAuthority
        this.name = name
        this.email = email
        this.hasAcceptedPrivacyNotice = hasAcceptedPrivacyNotice
    }
}
