package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.helpers.extensions.addAction
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesDataModel

class IncompletePropertyViewModelBuilder {
    companion object {
        fun fromDataModel(
            index: Int,
            dataModel: IncompletePropertiesDataModel,
            currentUrlKey: Int? = null,
        ): SummaryCardViewModel =
            SummaryCardViewModel(
                cardNumber = (index + 1).toString(),
                title = "landlord.incompleteProperties.summaryCardTitle",
                summaryList = getSummaryList(dataModel),
                actions = getActions(dataModel.contextId, currentUrlKey),
            )

        private fun getSummaryList(dataModel: IncompletePropertiesDataModel): List<SummaryListRowViewModel> =
            mutableListOf<SummaryListRowViewModel>()
                .apply {
                    addRow(
                        "landlord.incompleteProperties.summaryRow.propertyAddress",
                        dataModel.singleLineAddress,
                    )
                    addRow(
                        "landlord.incompleteProperties.summaryRow.localAuthority",
                        dataModel.localAuthorityName,
                    )
                    addRow(
                        "landlord.incompleteProperties.summaryRow.completeBy",
                        dataModel.completeByDate,
                    )
                }.toList()

        private fun getActions(
            contextId: Long,
            currentUrlKey: Int?,
        ): List<SummaryCardActionViewModel> =
            mutableListOf<SummaryCardActionViewModel>()
                .apply {
                    addAction(
                        "landlord.incompleteProperties.action.continue",
                        RegisterPropertyController.getResumePropertyRegistrationPath(contextId).overrideBackLinkForUrl(currentUrlKey),
                    )
                    addAction(
                        "landlord.incompleteProperties.action.delete",
                        LandlordController.deleteIncompletePropertyPath(contextId),
                    )
                }.toList()
    }
}
