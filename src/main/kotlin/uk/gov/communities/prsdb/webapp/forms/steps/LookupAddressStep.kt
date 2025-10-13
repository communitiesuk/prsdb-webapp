package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.AbstractPage
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getAddressDataPair
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getLookedUpAddresses
import uk.gov.communities.prsdb.webapp.services.AddressService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class LookupAddressStep<T : StepId>(
    id: T,
    page: AbstractPage,
    saveAfterSubmit: Boolean = true,
    restrictToEngland: Boolean = false,
    private val nextStepIfAddressesFound: T,
    private val nextStepIfNoAddressesFound: T,
    private val addressService: AddressService,
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
                addressService,
                restrictToEngland,
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
            addressService: AddressService,
            restrictToEngland: Boolean,
        ): String {
            val (houseNameOrNumber, postcode) =
                JourneyDataHelper.getLookupAddressHouseNameOrNumberAndPostcode(
                    filteredJourneyData,
                    id.urlPathSegment,
                )!!
            val addressLookupResults = addressService.searchForAddresses(houseNameOrNumber, postcode, restrictToEngland)

            val addressDataPair = getAddressDataPair(addressLookupResults)
            journeyDataService.addToJourneyDataIntoSession(mapOf(addressDataPair))

            val nextStepId = getNextStep(filteredJourneyData + addressDataPair, nextStepIfAddressesFound, nextStepIfNoAddressesFound)
            return generateUrl(nextStepId, subPageNumber, checkingAnswersFor)
        }
    }
}
