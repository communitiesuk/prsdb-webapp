package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType

@Entity
class RegistrationNumber() : AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = 0

    @Column(nullable = false, unique = true)
    var number: Long = 0
        private set

    @Column(nullable = false)
    lateinit var type: RegistrationNumberType
        private set

    constructor(type: RegistrationNumberType, number: Long) : this() {
        this.number = number
        this.type = type
    }
}
