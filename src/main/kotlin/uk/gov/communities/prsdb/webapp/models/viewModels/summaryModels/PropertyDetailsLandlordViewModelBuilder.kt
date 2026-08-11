package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class PropertyDetailsLandlordViewModelBuilder {
    companion object {
        private fun landlordEmail(landlord: Landlord): String =
            when (landlord) {
                is IndividualLandlord -> landlord.email
                is OrganisationalLandlord -> landlord.wholeOrgEmail
                else -> throw IllegalArgumentException("Unknown landlord type")
            }

        fun buildSummaryCards(
            landlords: Set<Landlord>,
            currentLandlord: Landlord,
            propertyOwnershipId: Long,
        ): List<SummaryCardViewModel> =
            landlords
                .sortedWith(compareByDescending<Landlord> { it.id == currentLandlord.id }.thenBy { it.name })
                .map { landlord ->
                    val isCurrentUser = landlord.id == currentLandlord.id
                    if (isCurrentUser) {
                        val removeMeAction =
                            SummaryCardActionViewModel(
                                "propertyDetails.landlordDetails.registeredLandlords.removeMe",
                                LeavePropertyController.getLeavePropertyPath(propertyOwnershipId),
                            )

                        val titleKey =
                            if (landlord.landlordType == LandlordType.ORGANISATION) {
                                "propertyDetails.landlordDetails.registeredLandlords.currentOrgCardTitle"
                            } else {
                                "propertyDetails.landlordDetails.registeredLandlords.currentUserCardTitle"
                            }

                        SummaryCardViewModel(
                            title = titleKey,
                            cardNumber = landlord.name,
                            summaryList = buildLandlordCardRows(landlord),
                            actions = if (landlords.size > 1) listOf(removeMeAction) else null,
                        )
                    } else {
                        SummaryCardViewModel(
                            title = landlord.name,
                            summaryList = buildLandlordCardRows(landlord),
                        )
                    }
                }

        private fun buildLandlordCardRows(landlord: Landlord): List<SummaryListRowViewModel> =
            listOf(
                SummaryListRowViewModel(
                    fieldHeading = "landlordDetails.personalDetails.lrn",
                    fieldValue = RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber),
                ),
                SummaryListRowViewModel(
                    fieldHeading = "landlordDetails.personalDetails.emailAddress",
                    fieldValue = landlordEmail(landlord),
                ),
            )

        fun buildLocalCouncilSummaryCards(
            landlords: Set<Landlord>,
            landlordDetailsUrlProvider: (Landlord) -> String,
        ): List<SummaryCardViewModel> =
            landlords
                .sortedBy { it.name }
                .map { landlord ->
                    when (landlord) {
                        is IndividualLandlord ->
                            SummaryCardViewModel(
                                title = landlord.name,
                                summaryList = buildLocalCouncilIndividualCardRows(landlord),
                                actions =
                                    listOf(
                                        SummaryCardActionViewModel(
                                            text = "propertyDetails.landlordDetails.registeredLandlords.viewLandlordRecord",
                                            url = landlordDetailsUrlProvider(landlord),
                                            opensInNewTab = true,
                                        ),
                                    ),
                            )

                        is OrganisationalLandlord ->
                            SummaryCardViewModel(
                                title = landlord.name,
                                summaryList = buildLocalCouncilOrgCardRows(landlord),
                                actions =
                                    listOf(
                                        // TODO: PDJB-1432: Link this to the landlord record for an org landlord
                                        SummaryCardActionViewModel(
                                            text = "propertyDetails.landlordDetails.registeredLandlords.viewLandlordRecord",
                                            url = landlordDetailsUrlProvider(landlord),
                                            opensInNewTab = true,
                                        ),
                                    ),
                            )

                        else -> throw IllegalArgumentException("Unknown landlord type")
                    }
                }

        private fun buildLocalCouncilOrgCardRows(landlord: OrganisationalLandlord): List<SummaryListRowViewModel> =
            listOf(
                SummaryListRowViewModel(
                    fieldHeading = "landlordDetails.personalDetails.lrn",
                    fieldValue = RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber),
                ),
                SummaryListRowViewModel(
                    fieldHeading = "landlordDetails.personalDetails.emailAddress",
                    fieldValue = landlord.wholeOrgEmail,
                ),
            )

        private fun buildLocalCouncilIndividualCardRows(landlord: IndividualLandlord): List<SummaryListRowViewModel> =
            listOf(
                SummaryListRowViewModel(
                    fieldHeading = "landlordDetails.personalDetails.lrn",
                    fieldValue = RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber),
                ),
                SummaryListRowViewModel(
                    fieldHeading = "landlordDetails.personalDetails.emailAddress",
                    fieldValue = landlord.email,
                ),
                SummaryListRowViewModel(
                    fieldHeading = "propertyDetails.landlordDetails.contactNumber",
                    fieldValue = landlord.phoneNumber,
                ),
                SummaryListRowViewModel(
                    fieldHeading = "landlordDetails.personalDetails.contactAddress",
                    fieldValue = landlord.address.toMultiLineAddress().split("\n"),
                ),
            )
    }
}
