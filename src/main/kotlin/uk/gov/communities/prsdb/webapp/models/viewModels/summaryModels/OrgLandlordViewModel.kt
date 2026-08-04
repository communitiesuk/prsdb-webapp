package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordNameController.Companion.UPDATE_ORG_NAME_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class OrgLandlordViewModel(
    landlord: OrganisationLandlord,
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
                // TODO: PDJB-1444: Add update journey
                addRow(
                    "landlordDetails.org.address",
                    landlord.address.toMultiLineAddress().split("\n"),
                    CHANGE_LINK_MESSAGE_KEY,
                    PLACEHOLDER_CHANGE_URL,
                )
                // TODO: PDJB-1235: Add update journey
                addRow(
                    "landlordDetails.org.email",
                    landlord.wholeOrgEmail,
                    CHANGE_LINK_MESSAGE_KEY,
                    PLACEHOLDER_CHANGE_URL,
                )
                // TODO: PDJB-1236: Add update journey
                addRow(
                    "landlordDetails.org.phone",
                    landlord.phoneNumber,
                    CHANGE_LINK_MESSAGE_KEY,
                    PLACEHOLDER_CHANGE_URL,
                )
                // TODO: PDJB-1237: Add update journey
                addRow(
                    "landlordDetails.org.organisationType",
                    landlord.organisationTypes.map { orgTypeMessageKey(it) },
                    CHANGE_LINK_MESSAGE_KEY,
                    PLACEHOLDER_CHANGE_URL,
                )
                // TODO: PDJB-1239: Add update journey
                addRow(
                    "landlordDetails.org.registeredCharity",
                    MessageKeyConverter.convert(landlord.isRegisteredCharity),
                    CHANGE_LINK_MESSAGE_KEY,
                    PLACEHOLDER_CHANGE_URL,
                )
                if (landlord.isRegisteredCharity) {
                    addRow(
                        "landlordDetails.org.charityCommission",
                        regulatorMessageKey(landlord.charityRegisteredWith!!),
                    )
                }
                if (landlord.charityNumber != null) {
                    addRow("landlordDetails.org.charityNumber", landlord.charityNumber)
                }
                // TODO: PDJB-1238: Add update journey
                addRow(
                    "landlordDetails.org.registeredWithCompaniesHouse",
                    MessageKeyConverter.convert(landlord.isRegisteredCompany),
                    CHANGE_LINK_MESSAGE_KEY,
                    PLACEHOLDER_CHANGE_URL,
                )
                if (landlord.isRegisteredCompany) {
                    addRow("landlordDetails.org.companyNumber", landlord.companyNumber)
                }
            }.toList()

    private fun orgTypeMessageKey(orgType: OrgType) =
        when (orgType) {
            OrgType.COMPANY -> "registerAsALandlord.orgType.checkbox.company"
            OrgType.CHARITY -> "registerAsALandlord.orgType.checkbox.charity"
            OrgType.TRUST -> "registerAsALandlord.orgType.checkbox.trust"
            OrgType.NONE -> "commonText.other"
        }

    private fun regulatorMessageKey(regulator: CharityRegulator) =
        when (regulator) {
            CharityRegulator.ENGLAND_AND_WALES -> "forms.orgCharityRegisteredWith.radios.option.englandAndWales"
            CharityRegulator.NORTHERN_IRELAND -> "forms.orgCharityRegisteredWith.radios.option.northernIreland"
            CharityRegulator.SCOTLAND -> "forms.orgCharityRegisteredWith.radios.option.scotland"
            CharityRegulator.NONE -> "commonText.other"
        }

    companion object {
        private const val CHANGE_LINK_MESSAGE_KEY = "forms.links.change"

        private const val UPDATE_ORG_NAME_URL = "$UPDATE_ORG_NAME_ROUTE/${OrgNameStep.ROUTE_SEGMENT}"

        // Non-functional Change link placeholder until the remaining organisation update journeys exist.
        private const val PLACEHOLDER_CHANGE_URL = "#"
    }
}
