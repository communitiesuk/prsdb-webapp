package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.forms.journeys.LaUserRegistrationJourney
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService

@Component
class LaUserRegistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataService: JourneyDataService,
    private val invitationService: LocalAuthorityInvitationService,
    private val localAuthorityDataService: LocalAuthorityDataService,
) {
    fun create(invitation: LocalAuthorityInvitation) =
        LaUserRegistrationJourney(validator, journeyDataService, invitationService, localAuthorityDataService, invitation)
}
