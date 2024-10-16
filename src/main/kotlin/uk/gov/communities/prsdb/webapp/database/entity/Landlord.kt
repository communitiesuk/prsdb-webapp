package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
class Landlord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @OneToOne(optional = false)
    @JoinColumn(
        name = "registration_number_id",
        nullable = false,
        foreignKey = ForeignKey(name = "FK_LANDLORD_REG_NUM"),
    )
    lateinit var registrationNumber: RegistrationNumber
        private set
}
