package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class OneLoginUser : AuditableEntity() {
    @Id
    private val id: String? = null

    @Column(nullable = false)
    var name: String? = null
        private set

    @Column(nullable = false)
    var email: String? = null
        private set
}
