package uk.gov.communities.prsdb.webapp.forms.steps

import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.AbstractPage
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getLookedUpAddresses
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.withUpdatedLookedUpAddresses
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class LookupAddressStep<T : StepId>(
    id: T,
    page: AbstractPage,
    isSatisfied: (bindingResult: BindingResult) -> Boolean = { bindingResult -> page.isSatisfied(bindingResult) },
    saveAfterSubmit: Boolean = true,
    private val nextStepIfAddressesFound: T,
    private val nextStepIfNoAddressesFound: T,
    private val addressLookupService: AddressLookupService,
    private val journeyDataService: JourneyDataService,
) : Step<T>(id, page, isSatisfied, saveAfterSubmit) {
    override var nextAction: (JourneyData, Int?) -> Pair<T?, Int?> =
        { _: JourneyData, subPageNumber: Int? -> Pair(getNextStep(), subPageNumber) }

    override var handleSubmitAndRedirect: ((JourneyData, Int?) -> String)? = { journeyData: JourneyData, subPageNumber: Int? ->
        performAddressLookupCacheResultsAndGetRedirect(
            journeyData,
            subPageNumber,
        )
    }

    private fun performAddressLookupCacheResultsAndGetRedirect(
        journeyData: JourneyData,
        subPageNumber: Int?,
    ): String {
        val (houseNameOrNumber, postcode) =
            JourneyDataHelper.getLookupAddressHouseNameOrNumberAndPostcode(
                journeyData,
                id.urlPathSegment,
            )!!
        val addressLookupResults = addressLookupService.search(houseNameOrNumber, postcode)

        val updatedJourneyData = journeyData.withUpdatedLookedUpAddresses(addressLookupResults)
        journeyDataService.setJourneyDataInSession(updatedJourneyData)

        val nextStepId = getNextStep()

        return Step.generateUrl(nextStepId, subPageNumber)
    }

    private fun getNextStep(): T {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        return if (journeyData.getLookedUpAddresses().isEmpty()) {
            nextStepIfNoAddressesFound
        } else {
            nextStepIfAddressesFound
        }
    }
}
