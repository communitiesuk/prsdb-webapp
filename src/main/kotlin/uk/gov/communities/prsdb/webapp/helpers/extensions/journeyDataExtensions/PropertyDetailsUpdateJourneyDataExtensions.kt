package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper.Companion.getFieldEnumValue
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper.Companion.getFieldStringValue

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

        fun JourneyData.getLicensingTypeIfPresent(): LicensingType? =
            getFieldEnumValue<LicensingType>(
                this,
                UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment,
                "licensingType",
            )

        fun JourneyData.getLicenceNumberIfPresent(originalJourneyKey: String): String? {
            val licensingType = getLicensingTypeIfPresent() ?: this.getOriginalLicensingType(originalJourneyKey)

            val licenseNumberPathSegment = getUpdateStepUrlPathSegmentForLicensingType(licensingType)

            return licenseNumberPathSegment?.let { getFieldStringValue(this, it.urlPathSegment, "licenceNumber") }
        }

        fun getUpdateStepUrlPathSegmentForLicensingType(licensingType: LicensingType?): UpdatePropertyDetailsStepId? =
            when (licensingType) {
                LicensingType.SELECTIVE_LICENCE -> UpdatePropertyDetailsStepId.UpdateSelectiveLicence
                LicensingType.HMO_MANDATORY_LICENCE -> UpdatePropertyDetailsStepId.UpdateHmoMandatoryLicence
                LicensingType.HMO_ADDITIONAL_LICENCE -> UpdatePropertyDetailsStepId.UpdateHmoAdditionalLicence
                else -> null
            }

        private fun JourneyData.getIsOccupied() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment,
                "occupied",
            )

        private fun JourneyData.getOriginalLicensingType(originalJourneyKey: String) =
            JourneyDataHelper.getPageData(this, originalJourneyKey)?.getLicensingTypeIfPresent()
    }
}
