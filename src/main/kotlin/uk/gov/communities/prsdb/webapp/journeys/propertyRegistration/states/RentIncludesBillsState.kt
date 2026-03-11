package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.helpers.BillsIncludedHelper
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.models.dataModels.BillsIncludedDataModel

interface RentIncludesBillsState : JourneyState {
    val rentIncludesBills: RentIncludesBillsStep
    val billsIncluded: BillsIncludedStep

    fun getBillsIncludedOrNull(): BillsIncludedDataModel? =
        if (doesRentIncludeBills()) {
            billsIncluded.formModelOrNull?.let { billsIncludedFormModel ->
                BillsIncludedDataModel.fromFormData(
                    formModel = billsIncludedFormModel,
                )
            }
        } else {
            null
        }

    fun getBillsIncluded(messageSource: MessageSource): String =
        BillsIncludedHelper.getBillsIncludedForCYAStep(
            getBillsIncludedOrNull()!!,
            messageSource,
        )

    fun doesRentIncludeBills() = rentIncludesBills.formModelOrNull?.rentIncludesBills == true
}
