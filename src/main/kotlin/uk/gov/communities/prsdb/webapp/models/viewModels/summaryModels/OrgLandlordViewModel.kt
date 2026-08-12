package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController.Companion.UPDATE_COMPANIES_HOUSE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordEmailController.Companion.UPDATE_ORG_EMAIL_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordNameController.Companion.UPDATE_ORG_NAME_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordPhoneNumberController.Companion.UPDATE_ORG_PHONE_NUMBER_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class OrgLandlordViewModel(
    landlord: OrganisationalLandlord,
    messageSource: MessageSource,
) {
    val name: String = landlord.name

    val organisationDetails: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "landlordDetails.org.registrationDate",
                    DateTimeHelper.getDateInUK(landlord.createdDate.toKotlinInstant()),
                )
                addRow(
                    "landlordDetails.org.lrn",
                    RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber),
                )
                addRow(
                    "landlordDetails.org.landlordType",
                    "landlordDetails.org.landlordTypeValue",
                )
                addRow(
                    "landlordDetails.org.name",
                    landlord.name,
                    CHANGE_LINK_MESSAGE_KEY,
                    UPDATE_ORG_NAME_URL,
                )
                addRow(
                    "landlordDetails.org.address",
                    landlord.address.toMultiLineAddress().split("\n"),
                    CHANGE_LINK_MESSAGE_KEY,
                    // TODO: PDJB-1444: Add update journey
                    null,
                )
                addRow(
                    "landlordDetails.org.email",
                    landlord.wholeOrgEmail,
                    CHANGE_LINK_MESSAGE_KEY,
                    UPDATE_ORG_EMAIL_URL,
                )
                addRow(
                    "landlordDetails.org.phone",
                    landlord.phoneNumber,
                    CHANGE_LINK_MESSAGE_KEY,
                    UPDATE_ORG_PHONE_NUMBER_URL,
                )
                addRow(
                    "landlordDetails.org.organisationType",
                    landlord.organisationTypes.joinToString(", ") { orgType ->
                        messageSource.getMessageForKey(getOrgTypeMessageKey(orgType))
                    },
                    CHANGE_LINK_MESSAGE_KEY,
                    UPDATE_ORG_TYPE_URL,
                )
                addRow(
                    "landlordDetails.org.registeredCharity",
                    MessageKeyConverter.convert(landlord.isRegisteredCharity),
                    CHANGE_LINK_MESSAGE_KEY,
                    UPDATE_ORG_CHARITY_URL,
                    // charityNumber and charityRegisteredWith are independently nullable, so the
                    // charity row section can start with either of them present
                    withoutBottomBorder = landlord.isRegisteredCharity || landlord.hasCharityNumber,
                )
                if (landlord.isRegisteredCharity) {
                    addRow(
                        key = "landlordDetails.org.charityCommission",
                        value = landlord.charityRegisteredWith,
                        withActionLink = false,
                        withoutBottomBorder = landlord.hasCharityNumber,
                    )
                }
                if (landlord.hasCharityNumber) {
                    addRow("landlordDetails.org.charityNumber", landlord.charityNumber)
                }
                addRow(
                    "landlordDetails.org.registeredWithCompaniesHouse",
                    MessageKeyConverter.convert(landlord.isRegisteredCompany),
                    CHANGE_LINK_MESSAGE_KEY,
                    UPDATE_COMPANIES_HOUSE_URL,
                    withoutBottomBorder = landlord.isRegisteredCompany,
                )
                if (landlord.isRegisteredCompany) {
                    addRow("landlordDetails.org.companyNumber", landlord.companyNumber)
                }
            }.toList()

    // The details page labels OrgType.NONE as "Other", unlike the registration form and its check
    // answers page, which use the "None of these" checkbox text the landlord actually selected.
    private fun getOrgTypeMessageKey(orgType: OrgType): String =
        if (orgType == OrgType.NONE) ORG_TYPE_OTHER_MESSAGE_KEY else MessageKeyConverter.convert(orgType)

    companion object {
        private const val CHANGE_LINK_MESSAGE_KEY = "forms.links.change"

        private const val ORG_TYPE_OTHER_MESSAGE_KEY = "landlordDetails.org.organisationTypeOther"

        private const val UPDATE_ORG_NAME_URL = "$UPDATE_ORG_NAME_ROUTE/${OrgNameStep.ROUTE_SEGMENT}"

        private const val UPDATE_ORG_EMAIL_URL = "$UPDATE_ORG_EMAIL_ROUTE/${OrgEmailStep.ROUTE_SEGMENT}"

        private const val UPDATE_ORG_PHONE_NUMBER_URL = "$UPDATE_ORG_PHONE_NUMBER_ROUTE/${OrgPhoneNumberStep.ROUTE_SEGMENT}"

        private const val UPDATE_ORG_TYPE_URL = "$UPDATE_ORG_TYPE_ROUTE/${OrgTypeStep.ROUTE_SEGMENT}"

        private const val UPDATE_ORG_CHARITY_URL = "$UPDATE_ORG_CHARITY_ROUTE/${OrgIsRegisteredCharityStep.ROUTE_SEGMENT}"

        private const val UPDATE_COMPANIES_HOUSE_URL = "$UPDATE_COMPANIES_HOUSE_ROUTE/${OrgIsRegisteredCompanyStep.ROUTE_SEGMENT}"
    }
}
