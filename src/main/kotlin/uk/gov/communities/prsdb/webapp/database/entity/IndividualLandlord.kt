package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Transient
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import java.time.LocalDate

@Entity
@DiscriminatorValue("0")
class IndividualLandlord() : Landlord() {
    @get:Transient
    override val landlordType: LandlordType
        get() = LandlordType.INDIVIDUAL

    @OneToOne
    @JoinColumn(name = "individual_subject_identifier", unique = true)
    lateinit var baseUser: PrsdbUser
        private set

    @Column(name = "individual_name")
    lateinit var name: String

    @Column(name = "individual_email")
    lateinit var email: String

    @Column(name = "individual_phone_number")
    lateinit var phoneNumber: String

    @ManyToOne
    @JoinColumn(name = "individual_address_id")
    lateinit var address: Address

    @Column(name = "individual_country_of_residence")
    lateinit var countryOfResidence: String
        private set

    @Column(name = "individual_non_england_or_wales_address", length = 1000)
    var nonEnglandOrWalesAddress: String? = null
        private set

    @Column(name = "individual_date_of_birth")
    var dateOfBirth: LocalDate? = null

    @Column(name = "individual_is_active")
    var isActive: Boolean = false
        private set

    @Column(name = "individual_is_verified")
    var isVerified: Boolean = false
        private set

    @Column(name = "individual_has_accepted_privacy_notice")
    var hasAcceptedPrivacyNotice: Boolean = false
        private set

    // TODO: PDJB-1294: Remove this
    @Column(name = "individual_has_responded_to_feedback")
    var hasRespondedToFeedback: Boolean = false

    @OneToMany(
        mappedBy = "landlord",
        orphanRemoval = true,
    )
    private lateinit var landlordIncompleteProperties: MutableSet<LandlordIncompleteProperties>

    val incompleteProperties: List<SavedJourneyState>
        get() = landlordIncompleteProperties.map { it.savedJourneyState }.toList()

    constructor(
        baseUser: PrsdbUser,
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
