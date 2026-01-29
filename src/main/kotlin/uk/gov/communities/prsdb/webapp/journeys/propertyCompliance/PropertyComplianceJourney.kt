package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService

@JourneyFrameworkComponent
class PropertyComplianceJourney(
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    PropertyComplianceJourneyState
