package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Transient
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import java.time.LocalDate

@Entity
@DiscriminatorValue("1")
class OrganisationLandlord() : Landlord() {
    @get:Transient
    override val landlordType: LandlordType
        get() = LandlordType.ORGANISATION

    @Column(name = "organisation_landlord_name")
    var name: String? = null

    @ManyToOne
    @JoinColumn(name = "organisation_address_id")
    var address: Address? = null

    @Column(name = "organisation_email")
    var email: String? = null

    @Column(name = "organisation_phone_number")
    var phoneNumber: String? = null

    @Column(name = "organisation_registrant_name")
    var registrantName: String? = null

    @Column(name = "organisation_registrant_date_of_birth")
    var registrantDateOfBirth: LocalDate? = null

    @Column(name = "organisation_registrant_email")
    var registrantEmail: String? = null

    @Column(name = "organisation_registrant_phone_number")
    var registrantPhoneNumber: String? = null

    // Note that this has no relation to the companyNumber nullable col
    @Column(name = "organisation_is_company")
    var isCompany: Boolean? = null

    // Note that this has no relation to the charityRegisteredWith & charityNumber nullable col
    @Column(name = "organisation_is_charity")
    var isCharity: Boolean? = null

    // Note that this determines whether the organisation needs a lead trustee
    @Column(name = "organisation_is_trust")
    var isTrust: Boolean? = null

    @Column(name = "organisation_company_number")
    var companyNumber: String? = null

    @Column(name = "organisation_charity_registered_with")
    var charityRegisteredWith: CharityRegulator? = null

    @Column(name = "organisation_charity_number")
    var charityNumber: String? = null

    @Column(name = "organisation_lead_trustee_name")
    var leadTrusteeName: String? = null

    @Column(name = "organisation_lead_trustee_date_of_birth")
    var leadTrusteeDateOfBirth: LocalDate? = null

    @Column(name = "organisation_lead_trustee_email")
    var leadTrusteeEmail: String? = null

    @Column(name = "organisation_lead_trustee_phone")
    var leadTrusteePhone: String? = null

    @ManyToOne
    @JoinColumn(name = "organisation_lead_trustee_address_id")
    var leadTrusteeAddress: Address? = null

    @Column(name = "organisation_main_contact_name")
    var mainContactName: String? = null

    @Column(name = "organisation_main_contact_email")
    var mainContactEmail: String? = null

    @Column(name = "organisation_main_contact_phone")
    var mainContactPhone: String? = null

    constructor(
        registrationNumber: RegistrationNumber,
        name: String,
        address: Address,
        email: String,
        phoneNumber: String,
        registrantName: String,
        registrantDateOfBirth: LocalDate,
        registrantEmail: String,
        registrantPhoneNumber: String,
        isCompany: Boolean,
        isCharity: Boolean,
        isTrust: Boolean,
        companyNumber: String?,
        charityRegisteredWith: CharityRegulator?,
        charityNumber: String?,
        leadTrusteeName: String?,
        leadTrusteeDateOfBirth: LocalDate?,
        leadTrusteeEmail: String?,
        leadTrusteePhone: String?,
        leadTrusteeAddress: Address?,
        mainContactName: String,
        mainContactEmail: String,
        mainContactPhone: String,
    ) : this() {
        this.registrationNumber = registrationNumber
        this.name = name
        this.address = address
        this.email = email
        this.phoneNumber = phoneNumber
        this.registrantName = registrantName
        this.registrantDateOfBirth = registrantDateOfBirth
        this.registrantEmail = registrantEmail
        this.registrantPhoneNumber = registrantPhoneNumber
        this.isCompany = isCompany
        this.isCharity = isCharity
        this.isTrust = isTrust
        this.companyNumber = companyNumber
        this.charityRegisteredWith = charityRegisteredWith
        this.charityNumber = charityNumber
        this.leadTrusteeName = leadTrusteeName
        this.leadTrusteeDateOfBirth = leadTrusteeDateOfBirth
        this.leadTrusteeEmail = leadTrusteeEmail
        this.leadTrusteePhone = leadTrusteePhone
        this.leadTrusteeAddress = leadTrusteeAddress
        this.mainContactName = mainContactName
        this.mainContactEmail = mainContactEmail
        this.mainContactPhone = mainContactPhone
    }

    @get:Transient
    val isRegisteredCompany: Boolean
        get() = companyNumber != null

    @get:Transient
    val isRegisteredCharity: Boolean
        get() = charityNumber != null

    @get:Transient
    val hasLeadTrustee: Boolean
        get() = isTrust == true

    @get:Transient
    val hasGoverningBody: Boolean
        get() = !isRegisteredCompany

    @get:Transient
    val organisationTypes: List<OrgType>
        get() =
            buildList {
                if (isCompany == true) add(OrgType.COMPANY)
                if (isCharity == true) add(OrgType.CHARITY)
                if (isTrust == true) add(OrgType.TRUST)
                if (isEmpty()) add(OrgType.NONE)
            }
}
