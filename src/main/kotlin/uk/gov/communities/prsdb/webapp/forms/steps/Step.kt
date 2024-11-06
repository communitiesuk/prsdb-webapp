package uk.gov.communities.prsdb.webapp.forms.steps

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.pages.Page

class Step<T : StepId>(
    val id: T,
    val page: Page,
    val handleSubmitAndRedirect: ((journeyData: JourneyData, subPageNumber: Int?) -> String)? = null,
    val isSatisfied: (validator: Validator, journeyData: JourneyData) -> Boolean = { validator, journeyData ->
        page.isSatisfied(
            validator,
            journeyData,
        )
    },
    val nextAction: (journeyData: JourneyData, subPageNumber: Int?) -> Pair<T?, Int?> = { _, _ ->
        Pair(
            null,
            null,
        )
    },
    val saveAfterSubmit: Boolean = true,
) {
    val name: String = id.urlPathSegment

    fun updateJourneyData(
        journeyData: JourneyData,
        pageData: PageData,
        subPageNumber: Int?,
    ) {
        if (subPageNumber != null) {
            var stepData = objectToStringKeyedMap(journeyData[name])
            if (stepData == null) {
                stepData = mutableMapOf<String, Any?>()
            }
            stepData[subPageNumber.toString()] = pageData
            journeyData[name] = stepData
        } else {
            journeyData[name] = pageData
        }
    }
}
