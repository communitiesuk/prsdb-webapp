package uk.gov.communities.prsdb.webapp.forms.steps

import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.pages.AbstractPage
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel

class Step<T : StepId>(
    val id: T,
    val page: AbstractPage,
    val autocompleteAndRedirect: (() -> String)? = null,
    val handleSubmitAndRedirect: ((journeyData: JourneyData, subPageNumber: Int?) -> String)? = null,
    val isSatisfied: (bindingResult: BindingResult) -> Boolean = { bindingResult -> page.isSatisfied(bindingResult) },
    val nextAction: (journeyData: JourneyData, subPageNumber: Int?) -> Pair<T?, Int?> = { _, _ ->
        Pair(
            null,
            null,
        )
    },
    val saveAfterSubmit: Boolean = true,
) {
    val name: String = id.urlPathSegment

    fun updatedJourneyData(
        journeyData: JourneyData,
        formModel: FormModel,
        subPageNumber: Int?,
    ): JourneyData =
        if (subPageNumber != null) {
            val newStepData = updatedStepData(journeyData, subPageNumber, formModel.toPageData())
            journeyData + (name to newStepData)
        } else {
            journeyData + (name to formModel.toPageData())
        }

    private fun updatedStepData(
        journeyData: JourneyData,
        subPageNumber: Int,
        pageData: PageData,
    ): PageData {
        val stepData = objectToStringKeyedMap(journeyData[name])
        return if (stepData == null) {
            mapOf(subPageNumber.toString() to pageData)
        } else {
            stepData + (subPageNumber.toString() to pageData)
        }
    }

    override fun toString() = id.toString()
}
