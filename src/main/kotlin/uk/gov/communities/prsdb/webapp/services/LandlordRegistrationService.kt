package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import java.time.LocalDate

@PrsdbWebService
class LandlordRegistrationService(
    private val landlordService: LandlordService,
) {
    @Transactional
    fun registerLandlord(
        baseUserId: String,
        landlordType: LandlordType,
        // Individual landlord fields
        individualName: String? = null,
        individualEmail: String? = null,
        individualPhoneNumber: String? = null,
        individualAddress: AddressDataModel? = null,
        individualDateOfBirth: LocalDate? = null,
        individualCountryOfResidence: String? = null,
        individualIsVerified: Boolean? = null,
        individualHasAcceptedPrivacyNotice: Boolean? = null,
        individualNonEnglandOrWalesAddress: String? = null,
        // Organisation landlord fields
        organisationTypes: List<OrgType> = emptyList(),
        organisationHasCompanyNumber: Boolean = false,
        organisationHasCharityNumber: Boolean = false,
        organisationName: String? = null,
        organisationAddress: AddressDataModel? = null,
        organisationEmail: String? = null,
        organisationPhoneNumber: String? = null,
        organisationCompanyNumber: String? = null,
        organisationCharityRegisteredWith: CharityRegulator? = null,
        organisationCharityNumber: String? = null,
        organisationLeadTrusteeName: String? = null,
        organisationLeadTrusteeDateOfBirth: LocalDate? = null,
        organisationLeadTrusteeEmail: String? = null,
        organisationLeadTrusteePhoneNumber: String? = null,
        organisationLeadTrusteeAddress: AddressDataModel? = null,
        organisationMainContactName: String? = null,
        organisationMainContactEmail: String? = null,
        organisationMainContactPhoneNumber: String? = null,
        organisationRegistrantName: String? = null,
        organisationRegistrantDateOfBirth: LocalDate? = null,
        organisationRegistrantEmail: String? = null,
        organisationRegistrantPhoneNumber: String? = null,
        organisationGoverningBodyMembers: List<GoverningBodyMemberDataModel> = emptyList(),
    ): Landlord =
        when (landlordType) {
            LandlordType.INDIVIDUAL -> {
                landlordService.createIndividualLandlord(
                    baseUserId = baseUserId,
                    name = individualName!!,
                    email = individualEmail!!,
                    phoneNumber = individualPhoneNumber!!,
                    addressDataModel = individualAddress!!,
                    countryOfResidence = individualCountryOfResidence!!,
                    isVerified = individualIsVerified!!,
                    hasAcceptedPrivacyNotice = individualHasAcceptedPrivacyNotice!!,
                    nonEnglandOrWalesAddress = individualNonEnglandOrWalesAddress,
                    dateOfBirth = individualDateOfBirth,
                )
            }

            LandlordType.ORGANISATION -> {
                val isTrust = OrgType.TRUST in organisationTypes
                val hasCharityNumber =
                    organisationHasCharityNumber && organisationCharityRegisteredWith != CharityRegulator.NONE

                landlordService.createOrganisationLandlord(
                    baseUserId = baseUserId,
                    organisationName = organisationName!!,
                    organisationAddress = organisationAddress!!,
                    organisationEmail = organisationEmail!!,
                    organisationPhoneNumber = organisationPhoneNumber!!,
                    isCompany = OrgType.COMPANY in organisationTypes,
                    isCharity = OrgType.CHARITY in organisationTypes,
                    isTrust = isTrust,
                    companyNumber = if (organisationHasCompanyNumber) organisationCompanyNumber!! else null,
                    charityRegisteredWith = if (organisationHasCharityNumber) organisationCharityRegisteredWith!! else null,
                    charityNumber = if (hasCharityNumber) organisationCharityNumber!! else null,
                    leadTrusteeName = if (isTrust) organisationLeadTrusteeName else null,
                    leadTrusteeDateOfBirth = if (isTrust) organisationLeadTrusteeDateOfBirth else null,
                    leadTrusteeEmail = if (isTrust) organisationLeadTrusteeEmail else null,
                    leadTrusteePhoneNumber = if (isTrust) organisationLeadTrusteePhoneNumber else null,
                    leadTrusteeAddress = if (isTrust) organisationLeadTrusteeAddress else null,
                    mainContactName = organisationMainContactName!!,
                    mainContactEmail = organisationMainContactEmail!!,
                    mainContactPhoneNumber = organisationMainContactPhoneNumber!!,
                    registrantName = organisationRegistrantName!!,
                    registrantDateOfBirth = organisationRegistrantDateOfBirth!!,
                    registrantEmail = organisationRegistrantEmail!!,
                    registrantPhoneNumber = organisationRegistrantPhoneNumber!!,
                    governingBodyMembers = if (!organisationHasCompanyNumber) organisationGoverningBodyMembers else emptyList(),
                )
            }
        }
}
