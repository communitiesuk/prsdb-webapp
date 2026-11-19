package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.PropertyManager
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
        buildSummaryCard(
            name = details.notNullValue(PropertyManagerDetailsFormModel::name),
            email = details.notNullValue(PropertyManagerDetailsFormModel::emailAddress),
            actions = actions,
        )

    fun getSummaryCard(propertyManager: PropertyManager): SummaryCardViewModel =
        buildSummaryCard(name = propertyManager.name, email = propertyManager.email, actions = emptyList())

    private fun buildSummaryCard(
        name: String,
        email: String,
        actions: List<SummaryCardActionViewModel>,
    ): SummaryCardViewModel =
        SummaryCardViewModel(
            title = name,
            summaryList =
                listOf(
                    SummaryListRowViewModel(
                        fieldHeading = "registerProperty.propertyManager.check.emailAddress",
                        fieldValue = email,
                    ),
                ),
            actions = actions,
        )
}
