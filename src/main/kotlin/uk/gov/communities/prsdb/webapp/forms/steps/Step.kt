package uk.gov.communities.prsdb.webapp.forms.steps

import org.springframework.validation.BindingResult
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.constants.CHECKING_ANSWERS_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.pages.AbstractPage
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import java.util.Optional

open class Step<T : StepId>(
    val id: T,
    val page: AbstractPage,
    val handleSubmitAndRedirect: ((filteredJourneyData: JourneyData, subPageNumber: Int?, checkingAnswersFor: T?) -> String)? = null,
    val isSatisfied: (bindingResult: BindingResult) -> Boolean = { bindingResult -> page.isSatisfied(bindingResult) },
    val nextAction: (filteredJourneyData: JourneyData, subPageNumber: Int?) -> Pair<T?, Int?> = { _, _ -> Pair(null, null) },
    val saveAfterSubmit: Boolean = true,
) {
    val name: String = id.urlPathSegment

    fun stepDataPair(
        journeyData: JourneyData,
        formModel: FormModel,
        subPageNumber: Int?,
    ): Pair<String, PageData> =
        if (subPageNumber != null) {
            val newStepData = updatedStepData(journeyData, subPageNumber, formModel.toPageData())
            (name to newStepData)
        } else {
            (name to formModel.toPageData())
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

    companion object {
        fun generateUrl(
            stepId: StepId,
            subPageNumber: Int?,
            checkingAnswersFor: StepId? = null,
        ): String =
            UriComponentsBuilder
                .newInstance()
                .path(stepId.urlPathSegment)
                .queryParamIfPresent("subpage", Optional.ofNullable(subPageNumber))
                .queryParamIfPresent(CHECKING_ANSWERS_FOR_PARAMETER_NAME, Optional.ofNullable(checkingAnswersFor?.urlPathSegment))
                .build(true)
                .toUriString()
    }
}
