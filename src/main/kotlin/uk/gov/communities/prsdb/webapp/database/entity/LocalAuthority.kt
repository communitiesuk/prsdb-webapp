package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class LocalAuthority(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
) : ModifiableAuditableEntity() {
    @Column(nullable = false)
    lateinit var name: String
        private set

    constructor(id: Int, name: String) : this(id) {
        this.name = name
    }
}
