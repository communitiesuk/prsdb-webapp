package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException

class UnrecoverableJourneyStateException(
    val journeyId: String,
    message: String,
) : PrsdbWebException(message)
