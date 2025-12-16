package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyRegistrationJourney
import uk.gov.communities.prsdb.webapp.services.AddressService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationMonolithicService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@PrsdbWebComponent
class PropertyRegistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val addressService: AddressService,
    private val propertyRegistrationService: PropertyRegistrationMonolithicService,
    private val localCouncilService: LocalCouncilService,
) {
    fun create() =
        PropertyRegistrationJourney(
            validator,
            journeyDataServiceFactory.create(JOURNEY_DATA_KEY),
            addressService,
            propertyRegistrationService,
            localCouncilService,
        )

    companion object {
        const val JOURNEY_DATA_KEY = RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE
    }
}
