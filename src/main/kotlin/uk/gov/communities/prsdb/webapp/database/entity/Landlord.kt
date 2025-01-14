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
import java.time.LocalDate

@Entity
class Landlord() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

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

    var internationalAddress: String? = null
        private set

    var dateOfBirth: LocalDate? = null
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

    constructor(
        baseUser: OneLoginUser,
        name: String,
        email: String,
        phoneNumber: String,
        address: Address,
        registrationNumber: RegistrationNumber,
        internationalAddress: String?,
        dateOfBirth: LocalDate?,
    ) : this() {
        this.baseUser = baseUser
        this.name = name
        this.email = email
        this.phoneNumber = phoneNumber
        this.address = address
        this.registrationNumber = registrationNumber
        this.internationalAddress = internationalAddress
        this.dateOfBirth = dateOfBirth
        this.isActive = true
    }
}
