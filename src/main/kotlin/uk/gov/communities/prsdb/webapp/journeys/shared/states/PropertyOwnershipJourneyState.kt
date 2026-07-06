package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState

interface PropertyOwnershipJourneyState : JourneyState {
    var propertyOwnershipId: Long
    var isStateInitialized: Boolean
}

fun <T : PropertyOwnershipJourneyState> T.initialiseFromPropertyOwnershipId(propertyOwnershipId: Long): T {
    if (!isStateInitialized) {
        this.propertyOwnershipId = propertyOwnershipId
        isStateInitialized = true
    }

    if (this.propertyOwnershipId != propertyOwnershipId) {
        throw PropertyOwnershipMismatchException(
            "Journey was initialized for property ownership ${this.propertyOwnershipId} " +
                "but request is for property ownership $propertyOwnershipId",
        )
    }

    return this
}
