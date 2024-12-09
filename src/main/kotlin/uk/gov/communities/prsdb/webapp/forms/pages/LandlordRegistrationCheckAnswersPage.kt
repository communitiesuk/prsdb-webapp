package uk.gov.communities.prsdb.webapp.forms.pages

import kotlinx.datetime.LocalDate
import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.FormSummaryDataModel
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import kotlin.reflect.KClass

class LandlordRegistrationCheckAnswersPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
) : Page(formModel, templateName, content) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): String {
        val livesInUK = getLivesInUk(journeyData!!)
        val formData = mutableListOf<FormSummaryDataModel>()

        formData.addAll(getIdFormData(journeyData))
        formData.addAll(getEmailAndPhoneFormData(journeyData))
        formData.add(getUKResidentRow(livesInUK))
        formData.addAll(getAddressFormData(journeyData, livesInUK))

        model.addAttribute("formData", formData)
        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl, journeyData)
    }

    private fun getIdFormData(journeyData: JourneyData): List<FormSummaryDataModel> =
        if (LandlordRegistrationJourney.doesJourneyDataContainVerifiedIdentity(journeyData)) {
            getVerifiedIdentifyRows(journeyData)
        } else {
            getManuallyEnteredIdentifyRows(journeyData)
        }

    private fun getVerifiedIdentifyRows(journeyData: JourneyData): List<FormSummaryDataModel> =
        listOf(
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                objectToStringKeyedMap(journeyData[LandlordRegistrationStepId.VerifyIdentity.urlPathSegment])?.get("name"),
                null,
            ),
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                objectToStringKeyedMap(journeyData[LandlordRegistrationStepId.VerifyIdentity.urlPathSegment])?.get("birthDate"),
                null,
            ),
        )

    private fun getManuallyEnteredIdentifyRows(journeyData: JourneyData): List<FormSummaryDataModel> {
        val formattedDate = getFormattedDate(journeyData)
        return listOf(
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                objectToStringKeyedMap(journeyData[LandlordRegistrationStepId.Name.urlPathSegment])?.get("name"),
                "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.Name.urlPathSegment}",
            ),
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                formattedDate,
                "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.DateOfBirth.urlPathSegment}",
            ),
        )
    }

    private fun getFormattedDate(journeyData: JourneyData): String {
        val formData = objectToStringKeyedMap(journeyData[LandlordRegistrationStepId.DateOfBirth.urlPathSegment])!!
        val year = formData["year"].toString().toInt()
        val month = formData["month"].toString().toInt()
        val day = formData["day"].toString().toInt()
        return LocalDate(year, month, day).toString()
    }

    private fun getEmailAndPhoneFormData(journeyData: JourneyData): List<FormSummaryDataModel> =
        listOf(
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                objectToStringKeyedMap(journeyData?.get(LandlordRegistrationStepId.Email.urlPathSegment))?.get("emailAddress"),
                "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.Email.urlPathSegment}",
            ),
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                objectToStringKeyedMap(journeyData?.get(LandlordRegistrationStepId.PhoneNumber.urlPathSegment))?.get("phoneNumber"),
                "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.PhoneNumber.urlPathSegment}",
            ),
        )

    private fun getUKResidentRow(livesInUK: Boolean): FormSummaryDataModel =
        FormSummaryDataModel(
            "registerAsALandlord.checkAnswers.rowHeading.ukResident",
            if (livesInUK) "Yes" else "No",
            "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}",
        )

    private fun getAddressFormData(
        journeyData: JourneyData,
        livesInUK: Boolean,
    ): List<FormSummaryDataModel> {
        val addressFormData = mutableListOf<FormSummaryDataModel>()
        if (!livesInUK) {
            addressFormData.add(getCountryOfResidenceRow(journeyData))
            addressFormData.add(getContactAddressOutsideUKRow(journeyData))
            addressFormData.add(getUKContactAddressOutsideUK(journeyData))
        } else {
            addressFormData.add(getContactAddressRow(journeyData))
        }

        return addressFormData
    }

    private fun getCountryOfResidenceRow(journeyData: JourneyData): FormSummaryDataModel =
        FormSummaryDataModel(
            "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence",
            objectToStringKeyedMap(journeyData[LandlordRegistrationStepId.CountryOfResidence.urlPathSegment])?.get("countryOfResidence"),
            "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}",
        )

    private fun getContactAddressOutsideUKRow(journeyData: JourneyData): FormSummaryDataModel =
        FormSummaryDataModel(
            "registerAsALandlord.checkAnswers.rowHeading.contactAddressOutsideUK",
            objectToStringKeyedMap(
                journeyData[LandlordRegistrationStepId.InternationalAddress.urlPathSegment],
            )?.get("internationalAddress"),
            "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.InternationalAddress.urlPathSegment}",
        )

    private fun getUKContactAddressOutsideUK(journeyData: JourneyData): FormSummaryDataModel {
        var addressValue =
            objectToStringKeyedMap(
                journeyData[LandlordRegistrationStepId.SelectContactAddress.urlPathSegment],
            )?.get("address")
        if (addressValue == MANUAL_ADDRESS_CHOSEN) {
            addressValue = getManualAddressValue(journeyData[LandlordRegistrationStepId.ManualContactAddress.urlPathSegment])
        }

        return FormSummaryDataModel(
            "registerAsALandlord.checkAnswers.rowHeading.ukContactAddress",
            addressValue,
            "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.LookupContactAddress.urlPathSegment}",
        )
    }

    private fun getContactAddressRow(journeyData: JourneyData): FormSummaryDataModel {
        var addressValue =
            objectToStringKeyedMap(
                journeyData[LandlordRegistrationStepId.SelectAddress.urlPathSegment],
            )?.get("address")
        if (addressValue == MANUAL_ADDRESS_CHOSEN) {
            addressValue = getManualAddressValue(journeyData[LandlordRegistrationStepId.ManualAddress.urlPathSegment])
        }
        return FormSummaryDataModel(
            "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
            addressValue,
            "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.LookupAddress.urlPathSegment}",
        )
    }

    private fun getManualAddressValue(key: Any?): String {
        val addressLineOne = objectToStringKeyedMap(key)?.get("addressLineOne")
        val addressLineTwo = objectToStringKeyedMap(key)?.get("addressLineTwo")
        val townOrCity = objectToStringKeyedMap(key)?.get("townOrCity")
        val county = objectToStringKeyedMap(key)?.get("county")
        val postcode = objectToStringKeyedMap(key)?.get("postcode")
        return listOfNotNull(addressLineOne, addressLineTwo, townOrCity, county, postcode)
            .joinToString(", ")
    }

    private fun getLivesInUk(journeyData: JourneyData): Boolean =
        objectToStringKeyedMap(
            journeyData[LandlordRegistrationStepId.CountryOfResidence.urlPathSegment],
        )?.get("livesInUK").toString().toBoolean()
}
