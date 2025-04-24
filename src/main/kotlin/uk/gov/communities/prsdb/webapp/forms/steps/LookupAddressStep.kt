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
) : Step<T>(
        id = id,
        page = page,
        isSatisfied = isSatisfied,
        nextAction = { journeyData: JourneyData, subPageNumber: Int? ->
            Pair(getNextStep(journeyData, nextStepIfAddressesFound, nextStepIfNoAddressesFound), subPageNumber)
        },
        saveAfterSubmit = saveAfterSubmit,
    ) {
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

        val nextStepId = getNextStep(journeyData, nextStepIfAddressesFound, nextStepIfNoAddressesFound)

        return Step.generateUrl(nextStepId, subPageNumber)
    }

    companion object {
        fun <T : StepId> getNextStep(
            journeyData: JourneyData,
            nextStepIfAddressesFound: T,
            nextStepIfNoAddressesFound: T,
        ): T =
            if (journeyData.getLookedUpAddresses().isEmpty()) {
                nextStepIfNoAddressesFound
            } else {
                nextStepIfAddressesFound
            }
    }
}
