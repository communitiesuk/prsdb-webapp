package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.UpdateJourneyDataExtensions.Companion.getOriginalJourneyDataIfPresent

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

        fun JourneyData.getLicensingTypeUpdateIfPresent(): LicensingType? =
            JourneyDataHelper.getFieldEnumValue<LicensingType>(
                this,
                UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment,
                "licensingType",
            )

        fun JourneyData.getLicenceNumberUpdateIfPresent(originalJourneyKey: String): String? {
            val licensingType = this.getLicensingTypeUpdateIfPresent() ?: this.getOriginalLicensingType(originalJourneyKey) ?: return null
            if (licensingType == LicensingType.NO_LICENSING) {
                return null
            } else {
                val licenseNumberUpdateStepId = getLicenceNumberUpdateStepId(licensingType)
                return JourneyDataHelper.getFieldStringValue(this, licenseNumberUpdateStepId!!.urlPathSegment, "licenceNumber")
            }
        }

        fun getLicenceNumberUpdateStepId(licensingType: LicensingType?): UpdatePropertyDetailsStepId? =
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

        fun JourneyData.getLatestNumberOfHouseholds(originalJourneyDataKey: String?): Int {
            val journeyDataValue = this.getNumberOfHouseholdsUpdateIfPresent()
            val originalJourneyData = this.getOriginalJourneyDataIfPresent(originalJourneyDataKey)!!
            val originalJourneyDataValue = originalJourneyData.getNumberOfHouseholdsUpdateIfPresent()
            if (journeyDataValue == null) {
                return originalJourneyDataValue ?: 0
            } else {
                return journeyDataValue
            }
        }

        private fun JourneyData.getOriginalLicensingType(originalJourneyKey: String) =
            JourneyDataHelper.getPageData(this, originalJourneyKey)?.getLicensingTypeUpdateIfPresent()
    }
}
