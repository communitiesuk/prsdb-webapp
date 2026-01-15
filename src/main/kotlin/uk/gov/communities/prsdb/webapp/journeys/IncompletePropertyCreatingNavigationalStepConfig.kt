package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent

@JourneyFrameworkComponent
class IncompletePropertyCreatingNavigationalStepConfig : NavigationalStepConfig()

@JourneyFrameworkComponent
class IncompletePropertyCreatingNavigationalStep(
    config: IncompletePropertyCreatingNavigationalStepConfig,
) : NavigationalStep(config)
