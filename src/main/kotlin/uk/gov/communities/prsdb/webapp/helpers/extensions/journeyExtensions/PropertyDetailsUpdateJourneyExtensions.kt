package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsGroupIdentifier
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.forms.steps.factories.PropertyDetailsUpdateJourneyStepFactory
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoAdditionalLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoMandatoryLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectiveLicenceFormModel

class PropertyDetailsUpdateJourneyExtensions {
    companion object {
        fun JourneyData.getOwnershipTypeUpdateIfPresent() =
            JourneyDataHelper.getFieldEnumValue<OwnershipType>(
                this,
                UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment,
                OwnershipTypeFormModel::ownershipType.name,
            )

        fun JourneyData.getOriginalIsOccupied(
            stepGroupId: UpdatePropertyDetailsGroupIdentifier,
            originalJourneyKey: String,
        ): Boolean? {
            val occupancyStepId = PropertyDetailsUpdateJourneyStepFactory.getOccupancyStepIdFor(stepGroupId)
            return JourneyDataHelper.getPageData(this, originalJourneyKey)?.getIsOccupied(occupancyStepId)
        }

        fun JourneyData.getIsOccupiedUpdateIfPresent(stepGroupId: UpdatePropertyDetailsGroupIdentifier): Boolean? {
            val occupancyStepId = PropertyDetailsUpdateJourneyStepFactory.getOccupancyStepIdFor(stepGroupId)
            return this.getIsOccupied(occupancyStepId)
        }

        fun JourneyData.getNumberOfHouseholdsUpdateIfPresent(stepGroupId: UpdatePropertyDetailsGroupIdentifier): Int? =
            if (this.getIsOccupiedUpdateIfPresent(stepGroupId) == false) {
                0
            } else {
                val numberOfHouseholdsStepId = PropertyDetailsUpdateJourneyStepFactory.getNumberOfHouseholdsStepIdFor(stepGroupId)
                JourneyDataHelper.getFieldIntegerValue(
                    this,
                    numberOfHouseholdsStepId.urlPathSegment,
                    NumberOfHouseholdsFormModel::numberOfHouseholds.name,
                )
            }

        fun JourneyData.getNumberOfPeopleUpdateIfPresent(stepGroupId: UpdatePropertyDetailsGroupIdentifier): Int? =
            if (this.getIsOccupiedUpdateIfPresent(stepGroupId) == false) {
                0
            } else {
                val numberOfPeopleStepId = PropertyDetailsUpdateJourneyStepFactory.getNumberOfPeopleStepIdFor(stepGroupId)
                JourneyDataHelper.getFieldIntegerValue(
                    this,
                    numberOfPeopleStepId.urlPathSegment,
                    NumberOfPeopleFormModel::numberOfPeople.name,
                )
            }

        fun JourneyData.getLicensingTypeUpdateIfPresent(): LicensingType? =
            JourneyDataHelper.getFieldEnumValue<LicensingType>(
                this,
                UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment,
                LicensingTypeFormModel::licensingType.name,
            )

        fun JourneyData.getLicenceNumberUpdateIfPresent(): String? {
            val licensingType = this.getLicensingTypeUpdateIfPresent() ?: return null
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

        private fun JourneyData.getIsOccupied(occupancyStepId: UpdatePropertyDetailsStepId) =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                occupancyStepId.urlPathSegment,
                OccupancyFormModel::occupied.name,
            )

        fun JourneyData.getLatestNumberOfHouseholds(
            stepGroupId: UpdatePropertyDetailsGroupIdentifier,
            originalJourneyKey: String,
        ): Int {
            val journeyDataValue = this.getNumberOfHouseholdsUpdateIfPresent(stepGroupId)
            if (journeyDataValue != null) return journeyDataValue

            val originalJourneyDataValue =
                JourneyDataHelper.getPageData(this, originalJourneyKey)?.getNumberOfHouseholdsUpdateIfPresent(stepGroupId)
            return originalJourneyDataValue ?: 0
        }

        fun PropertyOwnership.getLicenceNumberStepIdAndFormModel(): Pair<UpdatePropertyDetailsStepId, FormModel>? =
            when (this.license?.licenseType) {
                LicensingType.SELECTIVE_LICENCE ->
                    Pair(
                        UpdatePropertyDetailsStepId.UpdateSelectiveLicence,
                        SelectiveLicenceFormModel.fromPropertyOwnership(this),
                    )
                LicensingType.HMO_MANDATORY_LICENCE ->
                    Pair(
                        UpdatePropertyDetailsStepId.UpdateHmoMandatoryLicence,
                        HmoMandatoryLicenceFormModel.fromPropertyOwnership(this),
                    )
                LicensingType.HMO_ADDITIONAL_LICENCE ->
                    Pair(
                        UpdatePropertyDetailsStepId.UpdateHmoAdditionalLicence,
                        HmoAdditionalLicenceFormModel.fromPropertyOwnership(this),
                    )
                else -> null
            }
    }
}
