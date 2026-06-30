package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class PropertyDetailsLandlordViewModelBuilder {
    companion object {
        fun fromEntity(
            landlord: Landlord,
            landlordDetailsUrl: String = LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE,
        ): List<SummaryListRowViewModel> =
            mutableListOf<SummaryListRowViewModel>()
                .apply {
                    addRow(
                        "landlordDetails.personalDetails.name",
                        landlord.name,
                        landlordDetailsUrl,
                    )
                    addRow(
                        "landlordDetails.personalDetails.dateOfBirth",
                        landlord.dateOfBirth,
                    )
                    addRow(
                        "landlordDetails.personalDetails.oneLoginVerified",
                        MessageKeyConverter.convert(landlord.isVerified),
                    )
                    addRow(
                        "landlordDetails.personalDetails.emailAddress",
                        landlord.email,
                    )
                    addRow(
                        "propertyDetails.landlordDetails.contactNumber",
                        landlord.phoneNumber,
                    )
                    if (landlord.isEnglandOrWalesResident()) {
                        addRow(
                            "landlordDetails.personalDetails.contactAddress",
                            landlord.address.singleLineAddress,
                        )
                    } else {
                        addRow(
                            "propertyDetails.landlordDetails.addressNonEnglandOrWales",
                            landlord.nonEnglandOrWalesAddress,
                        )
                        addRow(
                            "propertyDetails.landlordDetails.contactAddressInEnglandOrWales",
                            landlord.address.singleLineAddress,
                        )
                    }
                }.toList()

        fun buildSummaryCards(
            landlords: Set<Landlord>,
            currentUserId: String,
            propertyOwnershipId: Long,
        ): List<SummaryCardViewModel> =
            landlords
                .sortedWith(compareByDescending<Landlord> { it.baseUser.id == currentUserId }.thenBy { it.name })
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

        private fun buildLandlordCardRows(landlord: Landlord): List<SummaryListRowViewModel> =
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
                .sortedBy { it.name }
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

        private fun buildLocalCouncilCardRows(landlord: Landlord): List<SummaryListRowViewModel> =
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
