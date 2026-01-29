package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService

@PrsdbWebService
class NewPropertyComplianceJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyComplianceJourneyState>,
)
