package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper

class PropertyDetailsUpdateJourneyDataExtensions private constructor(
    private val journeyData: JourneyData,
) {
    companion object {
        val JourneyData.propertyDetailsUpdateJourneyDataExtensions
            get() = PropertyDetailsUpdateJourneyDataExtensions(this)

        fun PropertyDetailsUpdateJourneyDataExtensions.getOwnershipTypeUpdateIfPresent() =
            JourneyDataHelper.getFieldEnumValue<OwnershipType>(
                journeyData,
                UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment,
                "ownershipType",
            )
    }
}
