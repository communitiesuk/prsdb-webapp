package uk.gov.communities.prsdb.webapp.forms.steps

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
    saveAfterSubmit: Boolean = true,
    private val nextStepIfAddressesFound: T,
    private val nextStepIfNoAddressesFound: T,
    private val addressLookupService: AddressLookupService,
    private val journeyDataService: JourneyDataService,
) : Step<T>(
        id = id,
        page = page,
        nextAction = { filteredJourneyData: JourneyData, subPageNumber: Int? ->
            Pair(getNextStep(filteredJourneyData, nextStepIfAddressesFound, nextStepIfNoAddressesFound), subPageNumber)
        },
        saveAfterSubmit = saveAfterSubmit,
        handleSubmitAndRedirect = { filteredJourneyData: JourneyData, subPageNumber: Int?, checkingAnswersFor: T? ->
            performAddressLookupCacheResultsAndGetRedirect(
                filteredJourneyData,
                subPageNumber,
                id,
                checkingAnswersFor,
                nextStepIfAddressesFound,
                nextStepIfNoAddressesFound,
                journeyDataService,
                addressLookupService,
            )
        },
    ) {
    companion object {
        fun <T : StepId> getNextStep(
            filteredJourneyData: JourneyData,
            nextStepIfAddressesFound: T,
            nextStepIfNoAddressesFound: T,
        ): T =
            if (filteredJourneyData.getLookedUpAddresses().isEmpty()) {
                nextStepIfNoAddressesFound
            } else {
                nextStepIfAddressesFound
            }

        fun <T : StepId> performAddressLookupCacheResultsAndGetRedirect(
            filteredJourneyData: JourneyData,
            subPageNumber: Int?,
            id: T,
            checkingAnswersFor: T?,
            nextStepIfAddressesFound: T,
            nextStepIfNoAddressesFound: T,
            journeyDataService: JourneyDataService,
            addressLookupService: AddressLookupService,
        ): String {
            val (houseNameOrNumber, postcode) =
                JourneyDataHelper.getLookupAddressHouseNameOrNumberAndPostcode(
                    filteredJourneyData,
                    id.urlPathSegment,
                )!!
            val addressLookupResults = addressLookupService.search(houseNameOrNumber, postcode)

            val updatedFilteredJourneyData = filteredJourneyData.withUpdatedLookedUpAddresses(addressLookupResults)
            journeyDataService.addToJourneyDataIntoSession(updatedFilteredJourneyData)

            val nextStepId = getNextStep(updatedFilteredJourneyData, nextStepIfAddressesFound, nextStepIfNoAddressesFound)
            return generateUrl(nextStepId, subPageNumber, checkingAnswersFor)
        }
    }
}
