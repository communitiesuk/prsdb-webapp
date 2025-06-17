package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.UPDATE_LANDLORD_DETAILS_URL
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordDetailsUpdateJourney
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordDetailsUpdateStepId
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@PrsdbWebComponent
class LandlordDetailsUpdateJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val addressLookupService: AddressLookupService,
    private val landlordService: LandlordService,
) {
    fun create(
        landlordBaseUserId: String,
        stepName: String,
    ) = LandlordDetailsUpdateJourney(
        validator,
        journeyDataServiceFactory.create(getJourneyDataKey(stepName)),
        addressLookupService,
        landlordService,
        landlordBaseUserId,
        stepName,
    )

    companion object {
        fun getJourneyDataKey(stepName: String): String {
            val step =
                LandlordDetailsUpdateStepId.fromPathSegment(stepName)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid LandlordDetailsUpdateJourney step name: $stepName")

            return "$UPDATE_LANDLORD_DETAILS_URL-${step.groupIdentifier}"
        }
    }
}
