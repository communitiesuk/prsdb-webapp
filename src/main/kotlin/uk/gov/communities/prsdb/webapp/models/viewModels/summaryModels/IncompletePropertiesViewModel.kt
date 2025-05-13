package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.LocalDate
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.helpers.extensions.addAction
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesDataModel

class IncompletePropertiesViewModel(
    private val incompletePropertiesData: List<IncompletePropertiesDataModel>,
) {
    val incompleteProperties: List<SummaryCardViewModel> =
        incompletePropertiesData.mapIndexed { index, property ->

            SummaryCardViewModel(
                cardNumber = (index + 1).toString(),
                title = "landlord.incompleteProperties.summaryCardTitlePrefix",
                summaryList = getSummaryList(property.singleLineAddress, property.localAuthorityName, property.completeByDate),
                actions = getActions(property.contextId),
            )
        }

    private fun getSummaryList(
        singleLineAddress: String,
        localAuthorityName: String,
        completeByDate: LocalDate,
    ): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "landlord.incompleteProperties.summaryRow.propertyAddress",
                    singleLineAddress,
                )
                addRow(
                    "landlord.incompleteProperties.summaryRow.localAuthority",
                    localAuthorityName,
                )
                addRow(
                    "landlord.incompleteProperties.summaryRow.completeBy",
                    completeByDate,
                )
            }.toList()

    private fun getActions(contextId: Long): List<SummaryCardActionViewModel> =
        mutableListOf<SummaryCardActionViewModel>()
            .apply {
                addAction(
                    "landlord.incompleteProperties.action.continue",
                    RegisterPropertyController.getResumePropertyRegistrationPath(contextId),
                )
                addAction(
                    "landlord.incompleteProperties.action.delete",
                    LandlordController.deleteIncompletePropertyPath(contextId),
                )
            }.toList()
}
