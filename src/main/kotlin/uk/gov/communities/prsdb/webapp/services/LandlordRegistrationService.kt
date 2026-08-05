package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordRegistrationConfirmationEmail
import java.time.LocalDate

@PrsdbWebService
class LandlordRegistrationService(
    private val landlordService: LandlordService,
    private val prsdbUserService: PrsdbUserService,
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
    private val organisationGoverningBodyMemberService: OrganisationGoverningBodyMemberService,
    private val registrationConfirmationSender: EmailNotificationService<LandlordRegistrationConfirmationEmail>,
    private val organisationalRegistrationConfirmationSender: EmailNotificationService<OrganisationalLandlordRegistrationConfirmationEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) {
    @Transactional
    fun registerIndividualLandlord(
        baseUserId: String,
        name: String,
        email: String,
        phoneNumber: String,
        address: AddressDataModel,
        dateOfBirth: LocalDate,
        countryOfResidence: String,
        isVerified: Boolean,
        hasAcceptedPrivacyNotice: Boolean,
        nonEnglandOrWalesAddress: String? = null,
    ): IndividualLandlord {
        val baseUser = prsdbUserService.findOrCreatePrsdbUser(baseUserId)

        val landlord =
            landlordService.createIndividualLandlord(
                baseUser = baseUser,
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                addressDataModel = address,
                countryOfResidence = countryOfResidence,
                isVerified = isVerified,
                hasAcceptedPrivacyNotice = hasAcceptedPrivacyNotice,
                nonEnglandOrWalesAddress = nonEnglandOrWalesAddress,
                dateOfBirth = dateOfBirth,
            )

        sendRegistrationConfirmationEmail(landlord)

        return landlord
    }

    @Transactional
    fun registerOrganisationLandlord(
        baseUserId: String,
        organisationTypes: List<OrgType>,
        organisationHasCompanyNumber: Boolean,
        orgIsRegisteredCharity: Boolean,
        organisationName: String,
        organisationAddress: AddressDataModel,
        organisationEmail: String,
        organisationPhoneNumber: String,
        organisationCompanyNumber: String?,
        organisationCharityRegisteredWith: CharityRegulator?,
        organisationCharityNumber: String?,
        organisationLeadTrusteeName: String?,
        organisationLeadTrusteeDateOfBirth: LocalDate?,
        organisationLeadTrusteeEmail: String?,
        organisationLeadTrusteePhoneNumber: String?,
        organisationLeadTrusteeAddress: AddressDataModel?,
        organisationMainContactName: String,
        organisationMainContactEmail: String,
        organisationMainContactPhoneNumber: String,
        organisationRegistrantName: String,
        organisationRegistrantDateOfBirth: LocalDate,
        organisationRegistrantEmail: String,
        organisationRegistrantPhoneNumber: String,
        organisationGoverningBodyMembers: List<GoverningBodyMemberDataModel>,
    ): OrganisationLandlord {
        val baseUser = prsdbUserService.findOrCreatePrsdbUser(baseUserId)

        val isTrust = OrgType.TRUST in organisationTypes
        val hasCharityNumber =
            orgIsRegisteredCharity && organisationCharityRegisteredWith != CharityRegulator.NONE

        val landlord =
            landlordService.createOrganisationLandlord(
                organisationName = organisationName,
                organisationAddress = organisationAddress,
                organisationEmail = organisationEmail,
                organisationPhoneNumber = organisationPhoneNumber,
                isCompany = OrgType.COMPANY in organisationTypes,
                isCharity = OrgType.CHARITY in organisationTypes,
                isTrust = isTrust,
                companyNumber = if (organisationHasCompanyNumber) organisationCompanyNumber!! else null,
                charityRegisteredWith = if (orgIsRegisteredCharity) organisationCharityRegisteredWith!! else null,
                charityNumber = if (hasCharityNumber) organisationCharityNumber!! else null,
                leadTrusteeName = if (isTrust) organisationLeadTrusteeName else null,
                leadTrusteeDateOfBirth = if (isTrust) organisationLeadTrusteeDateOfBirth else null,
                leadTrusteeEmail = if (isTrust) organisationLeadTrusteeEmail else null,
                leadTrusteePhoneNumber = if (isTrust) organisationLeadTrusteePhoneNumber else null,
                leadTrusteeAddress = if (isTrust) organisationLeadTrusteeAddress else null,
                mainContactName = organisationMainContactName,
                mainContactEmail = organisationMainContactEmail,
                mainContactPhoneNumber = organisationMainContactPhoneNumber,
                registrantName = organisationRegistrantName,
                registrantDateOfBirth = organisationRegistrantDateOfBirth,
                registrantEmail = organisationRegistrantEmail,
                registrantPhoneNumber = organisationRegistrantPhoneNumber,
            )

        organisationalLandlordUserService.createOrganisationalLandlordUser(
            landlord,
            baseUser,
            organisationRegistrantName,
            organisationRegistrantEmail,
        )

        if (!organisationHasCompanyNumber) {
            organisationGoverningBodyMemberService.createGoverningBodyMembers(landlord, organisationGoverningBodyMembers)
        }

        sendOrganisationalRegistrationConfirmationEmail(landlord)

        return landlord
    }

    private fun sendRegistrationConfirmationEmail(landlord: IndividualLandlord) {
        registrationConfirmationSender.sendEmail(
            landlord.email,
            LandlordRegistrationConfirmationEmail(
                RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString(),
                absoluteUrlProvider.buildLandlordDashboardUri().toString(),
            ),
        )
    }

    private fun sendOrganisationalRegistrationConfirmationEmail(landlord: OrganisationLandlord) {
        // TODO: PDJB-1274: reassess which address and name to send to once there is a general way to email a landlord
        organisationalRegistrationConfirmationSender.sendEmail(
            landlord.registrantEmail,
            OrganisationalLandlordRegistrationConfirmationEmail(
                registrantName = landlord.registrantName,
                organisationName = landlord.name,
                lrn = RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString(),
                prsdURL = absoluteUrlProvider.buildLandlordDashboardUri().toString(),
            ),
        )
    }
}
