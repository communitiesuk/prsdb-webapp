package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.forms.journeys.LocalCouncilUserRegistrationJourney
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@PrsdbWebComponent
class LocalCouncilUserRegistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val invitationService: LocalCouncilInvitationService,
    private val localCouncilDataService: LocalCouncilDataService,
    private val securityContextService: SecurityContextService,
) {
    fun create(invitation: LocalCouncilInvitation) =
        LocalCouncilUserRegistrationJourney(
            validator,
            journeyDataServiceFactory.create(JOURNEY_DATA_KEY),
            invitationService,
            localCouncilDataService,
            invitation,
            securityContextService,
        )

    companion object {
        const val JOURNEY_DATA_KEY = RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE
    }
}
