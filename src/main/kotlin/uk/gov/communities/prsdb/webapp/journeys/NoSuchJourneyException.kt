package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException

class NoSuchJourneyException(
    journeyId: String,
) : PrsdbWebException("No such journey with ID: $journeyId") {
    constructor() : this("No journey ID provided")
}
