package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class OneLoginUser(
    @Id val id: String = "",
) : ModifiableAuditableEntity() {
    @Column(nullable = false)
    lateinit var name: String
        private set

    @Column(nullable = false)
    lateinit var email: String
        private set

    constructor(id: String, name: String, email: String) : this(id) {
        this.name = name
        this.email = email
    }
}
