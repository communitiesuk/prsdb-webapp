package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper

class PropertyDetailsUpdateJourneyDataExtensions {
    companion object {
        fun JourneyData.getOwnershipTypeUpdateIfPresent() =
            JourneyDataHelper.getFieldEnumValue<OwnershipType>(
                this,
                UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment,
                "ownershipType",
            )

        fun JourneyData.getOriginalIsOccupied(originalJourneyKey: String) =
            JourneyDataHelper.getPageData(this, originalJourneyKey)?.getIsOccupied()

        fun JourneyData.getIsOccupiedUpdateIfPresent() = this.getIsOccupied()

        fun JourneyData.getNumberOfHouseholdsUpdateIfPresent() =
            if (this.getIsOccupiedUpdateIfPresent() == false) {
                0
            } else {
                JourneyDataHelper.getFieldIntegerValue(
                    this,
                    UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment,
                    "numberOfHouseholds",
                )
            }

        fun JourneyData.getNumberOfPeopleUpdateIfPresent() =
            if (this.getIsOccupiedUpdateIfPresent() == false) {
                0
            } else {
                JourneyDataHelper.getFieldIntegerValue(
                    this,
                    UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment,
                    "numberOfPeople",
                )
            }

        private fun JourneyData.getIsOccupied() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment,
                "occupied",
            )
    }
}
