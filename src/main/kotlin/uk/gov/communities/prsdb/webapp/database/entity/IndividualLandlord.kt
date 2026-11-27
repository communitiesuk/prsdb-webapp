package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Transient
import org.hibernate.envers.Audited
import org.hibernate.envers.RelationTargetAuditMode
import uk.gov.communities.prsdb.webapp.constants.DEPRECATED
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import java.time.LocalDate

@Entity
@DiscriminatorValue("0")
@Audited
class IndividualLandlord() : Landlord() {
    @get:Transient
    override val landlordType: LandlordType
        get() = LandlordType.INDIVIDUAL

    @OneToOne
    @JoinColumn(name = "individual_subject_identifier", unique = true)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    lateinit var baseUser: PrsdbUser
        private set

    @Column(name = "individual_name")
    override lateinit var name: String

    @Column(name = "individual_email")
    lateinit var email: String

    @Column(name = "individual_phone_number")
    lateinit var phoneNumber: String

    @ManyToOne
    @JoinColumn(name = "individual_address_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    override lateinit var address: Address

    // TODO PDJB-2309: drop this column - always DEPRECATED since individual landlords moved to the international address task
    @Column(name = "individual_country_of_residence")
    lateinit var countryOfResidence: String
        private set

    // TODO PDJB-2309: drop this column - always null since individual landlords moved to the international address task
    @Column(name = "individual_non_england_or_wales_address", length = 1000)
    var nonEnglandOrWalesAddress: String? = null
        private set

    @Column(name = "individual_date_of_birth")
    var dateOfBirth: LocalDate? = null

    @Column(name = "individual_is_verified")
    var isVerified: Boolean = false
        private set

    @Column(name = "individual_has_accepted_privacy_notice")
    var hasAcceptedPrivacyNotice: Boolean = false
        private set

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
    }

    // TODO PDJB-2309: remove with the individual_country_of_residence column
    fun isEnglandOrWalesResident(): Boolean = countryOfResidence == ENGLAND_OR_WALES || countryOfResidence == DEPRECATED
}
