package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.forms.journeys.LaUserRegistrationJourney
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@PrsdbWebComponent
class LaUserRegistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val invitationService: LocalAuthorityInvitationService,
    private val localAuthorityDataService: LocalAuthorityDataService,
    private val securityContextService: SecurityContextService,
) {
    fun create(invitation: LocalAuthorityInvitation) =
        LaUserRegistrationJourney(
            validator,
            journeyDataServiceFactory.create(JOURNEY_DATA_KEY),
            invitationService,
            localAuthorityDataService,
            invitation,
            securityContextService,
        )

    companion object {
        const val JOURNEY_DATA_KEY = RegisterLAUserController.LA_USER_REGISTRATION_ROUTE
    }
}
