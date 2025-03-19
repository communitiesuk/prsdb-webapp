package uk.gov.communities.prsdb.webapp.forms.steps

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.pages.AbstractPage

class Step<T : StepId>(
    val id: T,
    val page: AbstractPage,
    val handleSubmitAndRedirect: ((journeyData: JourneyData, subPageNumber: Int?) -> String)? = null,
    val isSatisfied: (validator: Validator, pageData: PageData) -> Boolean = { validator, pageData ->
        page.isSatisfied(validator, pageData)
    },
    val nextAction: (journeyData: JourneyData, subPageNumber: Int?) -> Pair<T?, Int?> = { _, _ ->
        Pair(null, null)
    },
    val reachableActions: (journeyData: JourneyData, subPageNumber: Int?) -> List<Pair<T, Int?>> = { journeyData, subPageNumber ->
        listOf(nextAction(journeyData, subPageNumber)).filterIsInstance<Pair<T, Int?>>()
    },
    val saveAfterSubmit: Boolean = true,
) {
    val name: String = id.urlPathSegment

    fun updatedJourneyData(
        journeyData: JourneyData,
        pageData: PageData,
        subPageNumber: Int?,
    ): JourneyData =
        if (subPageNumber != null) {
            val newStepData = updatedStepData(journeyData, subPageNumber, pageData)
            journeyData + (name to newStepData)
        } else {
            journeyData + (name to pageData)
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
