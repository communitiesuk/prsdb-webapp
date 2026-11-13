package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class PropertyManagerSummaryCardHelper {
    fun getSummaryCard(
        details: PropertyManagerDetailsFormModel,
        actions: List<SummaryCardActionViewModel> = emptyList(),
    ): SummaryCardViewModel =
        SummaryCardViewModel(
            title = details.notNullValue(PropertyManagerDetailsFormModel::name),
            summaryList =
                listOf(
                    SummaryListRowViewModel(
                        fieldHeading = "registerProperty.propertyManager.check.emailAddress",
                        fieldValue = details.notNullValue(PropertyManagerDetailsFormModel::emailAddress),
                    ),
                ),
            actions = actions,
        )
}
