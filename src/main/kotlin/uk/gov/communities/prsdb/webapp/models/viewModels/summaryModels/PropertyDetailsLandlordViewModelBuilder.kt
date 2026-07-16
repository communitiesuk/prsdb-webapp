package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class PropertyDetailsLandlordViewModelBuilder {
    companion object {
        fun buildSummaryCards(
            landlords: Set<Landlord>,
            currentUserId: String,
            propertyOwnershipId: Long,
        ): List<SummaryCardViewModel> =
            landlords
                // TODO: PDJB-1276: Update landlord tab landlord view for org landlords
                .map { landlord ->
                    check(landlord is IndividualLandlord)
                    landlord
                }.sortedWith(compareByDescending<IndividualLandlord> { it.baseUser.id == currentUserId }.thenBy { it.name })
                .map { landlord ->
                    val isCurrentUser = landlord.baseUser.id == currentUserId
                    if (isCurrentUser) {
                        val removeMeAction =
                            SummaryCardActionViewModel(
                                "propertyDetails.landlordDetails.registeredLandlords.removeMe",
                                LeavePropertyController.getLeavePropertyPath(propertyOwnershipId),
                            )

                        SummaryCardViewModel(
                            title =
                                "propertyDetails.landlordDetails.registeredLandlords.currentUserCardTitle",
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

        private fun buildLandlordCardRows(landlord: IndividualLandlord): List<SummaryListRowViewModel> =
            listOf(
                SummaryListRowViewModel(
                    fieldHeading = "landlordDetails.personalDetails.lrn",
                    fieldValue = RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber),
                ),
                SummaryListRowViewModel(
                    fieldHeading = "landlordDetails.personalDetails.emailAddress",
                    fieldValue = landlord.email,
                ),
            )

        fun buildLocalCouncilSummaryCards(
            landlords: Set<Landlord>,
            landlordDetailsUrlProvider: (Landlord) -> String,
        ): List<SummaryCardViewModel> =
            landlords
                // TODO: PDJB-1277: Update landlord tab local authority view for org landlords
                .map { landlord ->
                    check(landlord is IndividualLandlord)
                    landlord
                }.sortedBy { it.name }
                .map { landlord ->
                    SummaryCardViewModel(
                        title = landlord.name,
                        summaryList = buildLocalCouncilCardRows(landlord),
                        actions =
                            listOf(
                                SummaryCardActionViewModel(
                                    text = "propertyDetails.landlordDetails.registeredLandlords.viewLandlordRecord",
                                    url = landlordDetailsUrlProvider(landlord),
                                    opensInNewTab = true,
                                ),
                            ),
                    )
                }

        private fun buildLocalCouncilCardRows(landlord: IndividualLandlord): List<SummaryListRowViewModel> =
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
