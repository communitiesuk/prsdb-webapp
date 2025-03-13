package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.forms.journeys.LaUserRegistrationJourney
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
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
            journeyDataServiceFactory.create(REGISTER_LA_USER_JOURNEY_URL),
            invitationService,
            localAuthorityDataService,
            invitation,
            securityContextService,
        )
}
