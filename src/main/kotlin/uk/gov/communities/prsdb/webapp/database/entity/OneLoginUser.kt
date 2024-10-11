package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class OneLoginUser : ModifiableAuditableEntity() {
    @Id
    private val id: String? = null

    @Column(nullable = false)
    lateinit var name: String
        private set

    @Column(nullable = false)
    lateinit var email: String
        private set
}
