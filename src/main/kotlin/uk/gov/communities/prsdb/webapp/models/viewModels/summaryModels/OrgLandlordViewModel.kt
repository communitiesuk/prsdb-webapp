package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordNameController.Companion.UPDATE_ORG_NAME_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class OrgLandlordViewModel(
    landlord: OrganisationalLandlord,
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
                    // TODO: PDJB-1235: Add update journey
                    null,
                )
                addRow(
                    "landlordDetails.org.phone",
                    landlord.phoneNumber,
                    CHANGE_LINK_MESSAGE_KEY,
                    // TODO: PDJB-1236: Add update journey
                    null,
                )
                addRow(
                    "landlordDetails.org.organisationType",
                    landlord.organisationTypes,
                    CHANGE_LINK_MESSAGE_KEY,
                    // TODO: PDJB-1237: Add update journey
                    null,
                )
                addRow(
                    "landlordDetails.org.registeredCharity",
                    MessageKeyConverter.convert(landlord.isRegisteredCharity),
                    CHANGE_LINK_MESSAGE_KEY,
                    // TODO: PDJB-1239: Add update journey
                    null,
                )
                if (landlord.isRegisteredCharity) {
                    addRow("landlordDetails.org.charityCommission", landlord.charityRegisteredWith)
                }
                if (landlord.hasCharityNumber) {
                    addRow("landlordDetails.org.charityNumber", landlord.charityNumber)
                }
                addRow(
                    "landlordDetails.org.registeredWithCompaniesHouse",
                    MessageKeyConverter.convert(landlord.isRegisteredCompany),
                    CHANGE_LINK_MESSAGE_KEY,
                    // TODO: PDJB-1238: Add update journey
                    null,
                )
                if (landlord.isRegisteredCompany) {
                    addRow("landlordDetails.org.companyNumber", landlord.companyNumber)
                }
            }.toList()

    companion object {
        private const val CHANGE_LINK_MESSAGE_KEY = "forms.links.change"

        private const val UPDATE_ORG_NAME_URL = "$UPDATE_ORG_NAME_ROUTE/${OrgNameStep.ROUTE_SEGMENT}"
    }
}
