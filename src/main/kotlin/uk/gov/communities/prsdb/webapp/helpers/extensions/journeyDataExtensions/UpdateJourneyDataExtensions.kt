package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap

class UpdateJourneyDataExtensions {
    companion object {
        fun JourneyData.getOriginalJourneyDataIfPresent(originalJourneyDataKey: String?): JourneyData? =
            objectToStringKeyedMap(this[originalJourneyDataKey])
    }
}
