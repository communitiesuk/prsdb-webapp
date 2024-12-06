package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import java.util.Date

@Entity
class Landlord : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @OneToOne(optional = false)
    @JoinColumn(name = "subject_identifier", nullable = false, foreignKey = ForeignKey(name = "FK_LANDLORD_1L_USER"))
    lateinit var baseUser: OneLoginUser
        private set

    @Column(nullable = false)
    lateinit var name: String
        private set

    @Column(nullable = false)
    lateinit var email: String
        private set

    @Column(nullable = false)
    lateinit var phoneNumber: String
        private set

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "address_id",
        nullable = false,
        foreignKey = ForeignKey(name = "FK_LANDLORD_ADDRESS"),
    )
    lateinit var address: Address
        private set

    lateinit var internationalAddress: String
        private set

    lateinit var dateOfBirth: Date
        private set

    @OneToOne(optional = false)
    @JoinColumn(
        name = "registration_number_id",
        nullable = false,
        foreignKey = ForeignKey(name = "FK_LANDLORD_REG_NUM"),
    )
    lateinit var registrationNumber: RegistrationNumber
        private set

    @Column(nullable = false)
    var isActive: Boolean? = null
        private set
}
