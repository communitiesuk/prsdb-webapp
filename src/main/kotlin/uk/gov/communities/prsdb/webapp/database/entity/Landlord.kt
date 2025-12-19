package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import java.time.LocalDate

@Entity
class Landlord() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @OneToOne(optional = false)
    @JoinColumn(name = "subject_identifier", nullable = false, unique = true)
    lateinit var baseUser: OneLoginUser
        private set

    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false)
    lateinit var email: String

    @Column(nullable = false)
    lateinit var phoneNumber: String

    @ManyToOne(optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    lateinit var address: Address

    @Column(nullable = false)
    lateinit var countryOfResidence: String
        private set

    @Column(length = 1000)
    var nonEnglandOrWalesAddress: String? = null
        private set

    var dateOfBirth: LocalDate? = null

    @OneToOne(optional = false)
    @JoinColumn(name = "registration_number_id", nullable = false, unique = true)
    lateinit var registrationNumber: RegistrationNumber
        private set

    @Column(nullable = false)
    var isActive: Boolean = false
        private set

    @Column(nullable = false)
    var isVerified: Boolean = false
        private set

    @Column(nullable = false)
    var hasAcceptedPrivacyNotice: Boolean = false
        private set

    @Column(nullable = false)
    var hasRespondedToFeedback: Boolean = false

    @OneToMany(mappedBy = "primaryLandlord", orphanRemoval = true)
    private lateinit var propertyOwnerships: MutableSet<PropertyOwnership>

    @OneToMany(orphanRemoval = true)
    @JoinTable(
        name = "landlord_incomplete_properties",
        joinColumns = [JoinColumn(name = "landlord_id")],
        inverseJoinColumns = [JoinColumn(name = "saved_journey_state_id")],
    )
    lateinit var incompleteProperties: MutableSet<SavedJourneyState>

    constructor(
        baseUser: OneLoginUser,
        name: String,
        email: String,
        phoneNumber: String,
        address: Address,
        registrationNumber: RegistrationNumber,
        countryOfResidence: String,
        isVerified: Boolean,
        hasAcceptedPrivacyNotice: Boolean,
        nonEnglandOrWalesAddress: String?,
        dateOfBirth: LocalDate?,
    ) : this() {
        this.baseUser = baseUser
        this.name = name
        this.email = email
        this.phoneNumber = phoneNumber
        this.address = address
        this.registrationNumber = registrationNumber
        this.countryOfResidence = countryOfResidence
        this.isVerified = isVerified
        this.hasAcceptedPrivacyNotice = hasAcceptedPrivacyNotice
        this.nonEnglandOrWalesAddress = nonEnglandOrWalesAddress
        this.dateOfBirth = dateOfBirth
        this.isActive = true
    }

    fun isEnglandOrWalesResident(): Boolean = countryOfResidence == ENGLAND_OR_WALES

    val shouldSeeFeedback: Boolean
        get() = !hasRespondedToFeedback
}
