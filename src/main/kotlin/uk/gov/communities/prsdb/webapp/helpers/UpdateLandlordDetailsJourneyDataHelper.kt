package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.UpdateLandlordDetailsJourney
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.JourneyDataExtensions.Companion.getLookedUpAddress
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.time.LocalDate

class UpdateLandlordDetailsJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getIsIdentityVerified(journeyData: JourneyData): Boolean =
            getStringValueByKey(journeyData, UpdateLandlordDetailsJourney.IS_IDENTITY_VERIFIED_KEY).toBoolean()

        fun getEmailUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateLandlordDetailsStepId.UpdateEmail.urlPathSegment,
                "emailAddress",
            )

        fun getNameUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateLandlordDetailsStepId.UpdateName.urlPathSegment,
                "name",
            )

        fun getPhoneNumberIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateLandlordDetailsStepId.UpdatePhoneNumber.urlPathSegment,
                "phoneNumber",
            )

        fun getAddressIfPresent(journeyData: JourneyData): AddressDataModel? {
            val selectedAddress =
                getFieldStringValue(
                    journeyData,
                    UpdateLandlordDetailsStepId.SelectEnglandAndWalesAddress.urlPathSegment,
                    "address",
                )

            return if (selectedAddress == MANUAL_ADDRESS_CHOSEN) {
                getManualAddress(journeyData, UpdateLandlordDetailsStepId.ManualEnglandAndWalesAddress.urlPathSegment)
            } else if (selectedAddress != null) {
                journeyData.getLookedUpAddress(selectedAddress)
            } else {
                null
            }
        }

        fun getDateOfBirthIfPresent(journeyData: JourneyData): LocalDate? {
            val day =
                getFieldIntegerValue(
                    journeyData,
                    UpdateLandlordDetailsStepId.UpdateDateOfBirth.urlPathSegment,
                    "day",
                ) ?: return null

            val month =
                getFieldIntegerValue(
                    journeyData,
                    UpdateLandlordDetailsStepId.UpdateDateOfBirth.urlPathSegment,
                    "month",
                ) ?: return null

            val year =
                getFieldIntegerValue(
                    journeyData,
                    UpdateLandlordDetailsStepId.UpdateDateOfBirth.urlPathSegment,
                    "year",
                ) ?: return null

            return LocalDate.of(year, month, day)
        }
    }
}
