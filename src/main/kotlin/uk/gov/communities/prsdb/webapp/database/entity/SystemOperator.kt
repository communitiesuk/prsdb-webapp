package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
class SystemOperator(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : ModifiableAuditableEntity() {
    @OneToOne(optional = false)
    @JoinColumn(name = "subject_identifier", nullable = false, unique = true)
    lateinit var baseUser: OneLoginUser
        private set

    constructor(
        id: Long,
        baseUser: OneLoginUser,
    ) : this(id) {
        this.baseUser = baseUser
    }
}
