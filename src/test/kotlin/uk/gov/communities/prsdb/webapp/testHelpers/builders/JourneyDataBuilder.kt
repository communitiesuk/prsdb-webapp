package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordDetailsUpdateStepId
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.ORIGINALLY_NOT_INCLUDED_KEY
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FireSafetyDeclarationFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.KeepPropertySafeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ResponsibilityToTenantsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateEicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateGasSafetyCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import java.time.LocalDate

class JourneyDataBuilder(
    private val mockLocalAuthorityService: LocalAuthorityService = mock(),
    initialJourneyData: JourneyData? = null,
) {
    private val journeyData = initialJourneyData?.toMutableMap() ?: mutableMapOf()

    fun build() = journeyData

    companion object {
        const val DEFAULT_ADDRESS = "4, Example Road, EG"

        private val defaultPropertyJourneyData: JourneyData =
            mapOf(
                "lookup-address" to
                    mapOf(
                        "postcode" to "EG",
                        "houseNameOrNumber" to "4",
                    ),
                "select-address" to
                    mapOf(
                        "address" to DEFAULT_ADDRESS,
                    ),
                "property-type" to
                    mapOf(
                        "propertyType" to "OTHER",
                        "customPropertyType" to "Bungalow",
                    ),
                "ownership-type" to
                    mapOf(
                        "ownershipType" to "FREEHOLD",
                    ),
                "occupancy" to
                    mapOf(
                        "occupied" to "true",
                    ),
                "number-of-households" to
                    mapOf(
                        "numberOfHouseholds" to "2",
                    ),
                "number-of-people" to
                    mapOf(
                        "numberOfPeople" to "4",
                    ),
                "licensing-type" to
                    mapOf(
                        "licensingType" to "HMO_MANDATORY_LICENCE",
                    ),
                "hmo-mandatory-licence" to
                    mapOf(
                        "licenceNumber" to "test1234",
                    ),
                RegisterPropertyStepId.CheckAnswers.urlPathSegment to emptyMap(),
            )

        fun propertyDefault(localAuthorityService: LocalAuthorityService) =
            JourneyDataBuilder(localAuthorityService, defaultPropertyJourneyData).withSelectedAddress(
                DEFAULT_ADDRESS,
                709902,
                createLocalAuthority(),
            )

        private val defaultLandlordJourneyData: JourneyData =
            mapOf(
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment to mapOf(),
                LandlordRegistrationStepId.Name.urlPathSegment to mapOf("name" to "Arthur Dent"),
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment to
                    mapOf(
                        "day" to 6,
                        "month" to 8,
                        "year" to 2000,
                    ),
                LandlordRegistrationStepId.Email.urlPathSegment to mapOf("emailAddress" to "test@example.com"),
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment to mapOf("phoneNumber" to "07123456789"),
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment to mapOf("livesInEnglandOrWales" to true),
                LandlordRegistrationStepId.LookupAddress.urlPathSegment to mapOf("houseNameOrNumber" to "44", "postcode" to "EG1 1GE"),
                LandlordRegistrationStepId.SelectAddress.urlPathSegment to mapOf("address" to DEFAULT_ADDRESS),
                LandlordRegistrationStepId.CheckAnswers.urlPathSegment to emptyMap(),
            )

        fun landlordDefault(localAuthorityService: LocalAuthorityService) =
            JourneyDataBuilder(localAuthorityService, defaultLandlordJourneyData).withSelectedAddress(
                DEFAULT_ADDRESS,
                709902,
                createLocalAuthority(),
            )

        fun forLaUser(
            name: String,
            email: String,
        ) = JourneyDataBuilder().withLandingPageReached().withName(name).withEmailAddress(email)
    }

    fun withLookupAddress(
        houseNameOrNumber: String = "4",
        postcode: String = "EG1 2AB",
        isContactAddress: Boolean = false,
    ): JourneyDataBuilder {
        val lookupAddressKey = if (isContactAddress) "lookup-contact-address" else "lookup-address"
        journeyData[lookupAddressKey] = mapOf("houseNameOrNumber" to houseNameOrNumber, "postcode" to postcode)
        return this
    }

    fun withEmptyLookedUpAddresses(): JourneyDataBuilder {
        journeyData[NonStepJourneyDataKey.LookedUpAddresses.key] = "[]"
        return this
    }

    fun withLookedUpAddresses(customLookedUpAddresses: List<AddressDataModel>? = null): JourneyDataBuilder {
        val defaultLookedUpAddresses = listOf(AddressDataModel("1 Street Address, City, AB1 2CD"))
        journeyData[NonStepJourneyDataKey.LookedUpAddresses.key] = Json.encodeToString(customLookedUpAddresses ?: defaultLookedUpAddresses)
        return this
    }

    fun withManualAddressSelected(isContactAddress: Boolean = false): JourneyDataBuilder {
        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        journeyData[selectAddressKey] = mapOf("address" to MANUAL_ADDRESS_CHOSEN)
        return this
    }

    fun withSelectedAddress(
        singleLineAddress: String = "1 Street Address, City, AB1 2CD",
        uprn: Long? = null,
        localAuthority: LocalAuthority? = createLocalAuthority(),
        isContactAddress: Boolean = false,
    ): JourneyDataBuilder {
        localAuthority?.let {
            whenever(mockLocalAuthorityService.retrieveLocalAuthorityById(localAuthority.id)).thenReturn(localAuthority)
        }

        if (!isContactAddress) {
            withEnglandOrWalesResidence()
        }

        journeyData[NonStepJourneyDataKey.LookedUpAddresses.key] =
            Json.encodeToString(listOf(AddressDataModel(singleLineAddress, localAuthorityId = localAuthority?.id, uprn = uprn)))

        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        journeyData[selectAddressKey] = mapOf("address" to singleLineAddress)
        return this
    }

    fun withManualAddress(
        addressLineOne: String = "1 Street Address",
        townOrCity: String = "City",
        postcode: String = "AB1 2CD",
        localAuthority: LocalAuthority? = null,
        isContactAddress: Boolean = false,
    ): JourneyDataBuilder {
        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        journeyData[selectAddressKey] = mapOf("address" to MANUAL_ADDRESS_CHOSEN)

        val manualAddressKey = if (isContactAddress) "manual-contact-address" else "manual-address"
        journeyData[manualAddressKey] =
            mapOf(
                "addressLineOne" to addressLineOne,
                "townOrCity" to townOrCity,
                "postcode" to postcode,
            )

        if (!isContactAddress) {
            withEnglandOrWalesResidence()
        }

        if (localAuthority != null) {
            whenever(mockLocalAuthorityService.retrieveLocalAuthorityById(localAuthority.id)).thenReturn(localAuthority)
        }

        journeyData["local-authority"] =
            mapOf("localAuthorityId" to localAuthority?.id)

        return this
    }

    fun withEnglandOrWalesResidence(): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.CountryOfResidence.urlPathSegment] =
            mapOf(
                "livesInEnglandOrWales" to true,
            )
        return this
    }

    fun withCheckedAnswers(): JourneyDataBuilder {
        journeyData["check-answers"] = emptyMap<String, Any?>()
        return this
    }

    fun withPropertyType(
        type: PropertyType = PropertyType.DETACHED_HOUSE,
        customType: String = "type",
    ): JourneyDataBuilder {
        journeyData["property-type"] =
            if (type == PropertyType.OTHER) {
                mapOf("propertyType" to type.name, "customPropertyType" to customType)
            } else {
                mapOf("propertyType" to type.name)
            }
        return this
    }

    fun withOwnershipType(ownershipType: OwnershipType = OwnershipType.FREEHOLD): JourneyDataBuilder {
        journeyData["ownership-type"] = mapOf("ownershipType" to ownershipType.name)
        return this
    }

    fun withLicensingType(licensingType: LicensingType): JourneyDataBuilder {
        journeyData["licensing-type"] = mapOf("licensingType" to licensingType.name)
        return this
    }

    fun withLicensing(
        licensingType: LicensingType,
        licenseNumber: String? = null,
    ): JourneyDataBuilder {
        withLicensingType(licensingType)
        when (licensingType) {
            LicensingType.SELECTIVE_LICENCE -> withLicenceNumber("selective-licence", licenseNumber)
            LicensingType.HMO_MANDATORY_LICENCE -> withLicenceNumber("hmo-mandatory-licence", licenseNumber)
            LicensingType.HMO_ADDITIONAL_LICENCE -> withLicenceNumber("hmo-additional-licence", licenseNumber)
            LicensingType.NO_LICENSING -> {}
        }
        return this
    }

    private fun withLicenceNumber(
        urlPathSegment: String,
        licenceNumber: String?,
    ): JourneyDataBuilder {
        journeyData[urlPathSegment] = mapOf("licenceNumber" to licenceNumber)
        return this
    }

    fun withNoTenants(): JourneyDataBuilder {
        journeyData.remove("number-of-households")
        journeyData.remove("number-of-people")
        return withOccupiedSetToFalse()
    }

    fun withOccupiedSetToFalse(): JourneyDataBuilder {
        journeyData["occupancy"] =
            mapOf(
                "occupied" to "false",
            )
        return this
    }

    fun withOccupancyStatus(occupied: Boolean): JourneyDataBuilder {
        journeyData[RegisterPropertyStepId.Occupancy.urlPathSegment] = mapOf(OccupancyFormModel::occupied.name to occupied)
        return this
    }

    fun withHouseholds(households: Int = 2): JourneyDataBuilder {
        journeyData[RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment] =
            mapOf(NumberOfHouseholdsFormModel::numberOfHouseholds.name to households.toString())
        return this
    }

    fun withTenants(
        households: Int = 2,
        people: Int = 4,
    ): JourneyDataBuilder {
        withOccupancyStatus(true)
        withHouseholds(households)
        journeyData[RegisterPropertyStepId.NumberOfPeople.urlPathSegment] =
            mapOf(NumberOfPeopleFormModel::numberOfPeople.name to people.toString())

        return this
    }

    fun withVerifiedUser(
        name: String = "Arthur Dent",
        dob: LocalDate = LocalDate.of(2000, 6, 8),
    ): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.VerifyIdentity.urlPathSegment] =
            mapOf(
                "name" to name,
                "birthDate" to dob,
            )
        journeyData[LandlordRegistrationStepId.ConfirmIdentity.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withUnverifiedUser(
        name: String? = "Arthur Dent",
        dob: LocalDate? = LocalDate.of(2000, 6, 8),
    ): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.VerifyIdentity.urlPathSegment] = emptyMap<String, Any?>()
        name?.let { journeyData[LandlordRegistrationStepId.Name.urlPathSegment] = mapOf("name" to name) }
        dob?.let {
            journeyData[LandlordRegistrationStepId.DateOfBirth.urlPathSegment] =
                mapOf("day" to dob.dayOfMonth, "month" to dob.monthValue, "year" to dob.year)
        }
        return this
    }

    fun withEmailAddress(emailAddress: String = "email@test.com"): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.Email.urlPathSegment] = mapOf("emailAddress" to emailAddress)
        return this
    }

    fun withPhoneNumber(phoneNumber: String = "07456097576"): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.PhoneNumber.urlPathSegment] = mapOf("phoneNumber" to phoneNumber)
        return this
    }

    fun withNonEnglandOrWalesAndSelectedContactAddress(
        countryOfResidence: String,
        nonEnglandOrWalesAddress: String,
        selectedAddress: String,
    ): JourneyDataBuilder =
        this
            .withNonEnglandOrWalesAddress(countryOfResidence, nonEnglandOrWalesAddress)
            .withSelectedAddress(selectedAddress, localAuthority = null, isContactAddress = true)

    fun withNonEnglandOrWalesAndManualContactAddress(
        countryOfResidence: String,
        nonEnglandOrWalesAddress: String,
        addressLineOne: String,
        townOrCity: String,
        postcode: String,
    ): JourneyDataBuilder =
        this
            .withNonEnglandOrWalesAddress(countryOfResidence, nonEnglandOrWalesAddress)
            .withManualAddress(addressLineOne, townOrCity, postcode, isContactAddress = true)

    fun withNonEnglandOrWalesAddress(
        countryOfResidence: String = "Zimbabwe",
        nonEnglandOrWalesAddress: String? = "123 Example Road, Harare",
    ): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.CountryOfResidence.urlPathSegment] =
            mapOf(
                "livesInEnglandOrWales" to false,
                "countryOfResidence" to countryOfResidence,
            )
        nonEnglandOrWalesAddress?.let {
            journeyData[LandlordRegistrationStepId.NonEnglandOrWalesAddress.urlPathSegment] =
                mapOf("nonEnglandOrWalesAddress" to nonEnglandOrWalesAddress)
        }
        return this
    }

    fun withEmailAddressUpdate(newEmail: String): JourneyDataBuilder {
        journeyData[LandlordDetailsUpdateStepId.UpdateEmail.urlPathSegment] = mapOf("emailAddress" to newEmail)
        return this
    }

    fun withNameUpdate(newName: String): JourneyDataBuilder {
        journeyData[LandlordDetailsUpdateStepId.UpdateName.urlPathSegment] = mapOf("name" to newName)
        return this
    }

    fun withDateOfBirthUpdate(dateOfBirth: LocalDate): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.DateOfBirth.urlPathSegment] =
            mapOf("day" to dateOfBirth.dayOfMonth, "month" to dateOfBirth.monthValue, "year" to dateOfBirth.year)
        return this
    }

    fun withOwnershipTypeUpdate(ownershipType: OwnershipType): JourneyDataBuilder {
        journeyData[UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment] =
            mutableMapOf("ownershipType" to ownershipType.name)
        return this
    }

    fun withIsOccupiedUpdate(isOccupied: Boolean): JourneyDataBuilder {
        journeyData[UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment] =
            mutableMapOf("occupied" to isOccupied)
        return this
    }

    fun withNumberOfHouseholdsUpdate(numberOfHouseholds: Int): JourneyDataBuilder {
        journeyData[UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment] =
            mutableMapOf("numberOfHouseholds" to numberOfHouseholds)
        return this
    }

    fun withNumberOfPeopleUpdate(numberOfPeople: Int): JourneyDataBuilder {
        journeyData[UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment] =
            mutableMapOf("numberOfPeople" to numberOfPeople)
        return this
    }

    fun withLicensingTypeUpdate(licensingType: LicensingType): JourneyDataBuilder {
        journeyData[UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment] = mutableMapOf("licensingType" to licensingType.name)
        return this
    }

    fun withLicenceNumberUpdate(
        licenceNumber: String,
        licensingType: LicensingType,
    ): JourneyDataBuilder {
        val licenseNumberUpdateStepIdUrlPathSegment =
            when (licensingType) {
                LicensingType.SELECTIVE_LICENCE -> UpdatePropertyDetailsStepId.UpdateSelectiveLicence.urlPathSegment
                LicensingType.HMO_MANDATORY_LICENCE -> UpdatePropertyDetailsStepId.UpdateHmoMandatoryLicence.urlPathSegment
                LicensingType.HMO_ADDITIONAL_LICENCE -> UpdatePropertyDetailsStepId.UpdateHmoAdditionalLicence.urlPathSegment
                LicensingType.NO_LICENSING -> ""
            }
        journeyData[licenseNumberUpdateStepIdUrlPathSegment] = mapOf("licenceNumber" to licenceNumber)
        return this
    }

    fun withLicenceUpdate(
        licensingType: LicensingType,
        licenceNumber: String,
    ): JourneyDataBuilder =
        this
            .withLicensingTypeUpdate(licensingType)
            .withLicenceNumberUpdate(licenceNumber, licensingType)

    fun withOriginalData(
        originalDataKey: String,
        originalData: JourneyData,
    ): JourneyDataBuilder {
        journeyData[originalDataKey] = originalData
        return this
    }

    fun withOriginalNumberOfHouseholdsData(
        originalDataKey: String,
        originalNumberOfHouseholds: Int,
    ): JourneyDataBuilder {
        journeyData[originalDataKey] =
            mapOf(
                UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment to
                    mapOf("numberOfHouseholds" to originalNumberOfHouseholds),
            )
        return this
    }

    fun withGasSafetyCertStatus(hasGasSafetyCert: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafety.urlPathSegment] =
            mapOf(GasSafetyFormModel::hasCert.name to hasGasSafetyCert)
        return this
    }

    fun withNewGasSafetyCertStatus(hasNewGasSafetyCert: Boolean?): JourneyDataBuilder {
        if (hasNewGasSafetyCert != null) {
            journeyData[PropertyComplianceStepId.UpdateGasSafety.urlPathSegment] =
                mapOf(UpdateGasSafetyCertificateFormModel::hasNewCertificate.name to hasNewGasSafetyCert)
            return this
        } else {
            journeyData[PropertyComplianceStepId.UpdateGasSafety.urlPathSegment] =
                mapOf(
                    UpdateGasSafetyCertificateFormModel::hasNewCertificate.name to false,
                    ORIGINALLY_NOT_INCLUDED_KEY to true,
                )
            return this
        }
    }

    fun withGasSafetyIssueDate(issueDate: LocalDate = LocalDate.now()): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment] =
            mapOf(
                TodayOrPastDateFormModel::day.name to issueDate.dayOfMonth,
                TodayOrPastDateFormModel::month.name to issueDate.monthValue,
                TodayOrPastDateFormModel::year.name to issueDate.year,
            )
        return this
    }

    fun withGasSafeEngineerNum(engineerNum: String = "1234567"): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafetyEngineerNum.urlPathSegment] =
            mapOf(GasSafeEngineerNumFormModel::engineerNumber.name to engineerNum)
        return this
    }

    fun withOriginalGasSafetyCertName(
        originalName: String,
        metadataOnly: Boolean = false,
    ): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafetyUpload.urlPathSegment] =
            mapOf(
                GasSafetyUploadCertificateFormModel::name.name to originalName,
                GasSafetyUploadCertificateFormModel::isMetadataOnly.name to metadataOnly,
            )
        return this
    }

    fun withGasSafetyCertUploadConfirmation(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafetyUploadConfirmation.urlPathSegment] = emptyMap<String, Any>()
        return this
    }

    fun withGasSafetyCertExemptionStatus(hasGasSafetyCertExemption: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafetyExemption.urlPathSegment] =
            mapOf(GasSafetyExemptionFormModel::hasExemption.name to hasGasSafetyCertExemption)
        return this
    }

    fun withGasSafetyCertExemptionReason(gasSafetyCertExemptionReason: GasSafetyExemptionReason): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafetyExemptionReason.urlPathSegment] =
            mapOf(GasSafetyExemptionReasonFormModel::exemptionReason.name to gasSafetyCertExemptionReason)
        return this
    }

    fun withGasSafetyCertExemptionOtherReason(otherReason: String): JourneyDataBuilder {
        withGasSafetyCertExemptionReason(GasSafetyExemptionReason.OTHER)
        journeyData[PropertyComplianceStepId.GasSafetyExemptionOtherReason.urlPathSegment] =
            mapOf(GasSafetyExemptionOtherReasonFormModel::otherReason.name to otherReason)
        return this
    }

    fun withGasSafetyCertExemptionConfirmation(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafetyExemptionConfirmation.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withMissingGasSafetyExemption(): JourneyDataBuilder {
        withGasSafetyCertStatus(false)
        withGasSafetyCertExemptionStatus(false)
        journeyData[PropertyComplianceStepId.GasSafetyExemptionMissing.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withGasSafetyUpdateCheckAnswers(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withEicrStatus(hasEICR: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EICR.urlPathSegment] = mapOf(EicrFormModel::hasCert.name to hasEICR)
        return this
    }

    fun withNewEicrStatus(hasNewEICR: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.UpdateEICR.urlPathSegment] =
            mapOf(UpdateEicrFormModel::hasNewCertificate.name to hasNewEICR)
        return this
    }

    fun withEicrIssueDate(issueDate: LocalDate = LocalDate.now()): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EicrIssueDate.urlPathSegment] =
            mapOf(
                TodayOrPastDateFormModel::day.name to issueDate.dayOfMonth,
                TodayOrPastDateFormModel::month.name to issueDate.monthValue,
                TodayOrPastDateFormModel::year.name to issueDate.year,
            )
        return this
    }

    fun withOriginalEicrName(originalName: String): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EicrUpload.urlPathSegment] =
            mapOf(EicrUploadCertificateFormModel::name.name to originalName)
        return this
    }

    fun withEicrUploadConfirmation(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EicrUploadConfirmation.urlPathSegment] = emptyMap<String, Any>()
        return this
    }

    fun withEicrExemptionStatus(hasEicrExemption: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EicrExemption.urlPathSegment] =
            mapOf(EicrExemptionFormModel::hasExemption.name to hasEicrExemption)
        return this
    }

    fun withEicrExemptionReason(eicrExemptionReason: EicrExemptionReason): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EicrExemptionReason.urlPathSegment] =
            mapOf(EicrExemptionReasonFormModel::exemptionReason.name to eicrExemptionReason)
        return this
    }

    fun withEicrExemptionOtherReason(otherReason: String): JourneyDataBuilder {
        withEicrExemptionReason(EicrExemptionReason.OTHER)
        journeyData[PropertyComplianceStepId.EicrExemptionOtherReason.urlPathSegment] =
            mapOf(EicrExemptionOtherReasonFormModel::otherReason.name to otherReason)
        return this
    }

    fun withEicrExemptionConfirmation(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EicrExemptionConfirmation.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withMissingEicrExemption(): JourneyDataBuilder {
        withEicrStatus(false)
        withEicrExemptionStatus(false)
        journeyData[PropertyComplianceStepId.EicrExemptionMissing.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withEicrUpdateCheckAnswers(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.UpdateEicrCheckYourAnswers.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withEpcStatus(hasEpc: HasEpc): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EPC.urlPathSegment] =
            mapOf(EpcFormModel::hasCert.name to hasEpc)
        return this
    }

    fun withNewEpcStatus(hasNewEpc: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.UpdateEpc.urlPathSegment] =
            mapOf(UpdateEpcFormModel::hasNewCertificate.name to hasNewEpc)
        return this
    }

    fun withAutoMatchedEpcDetails(epcDetails: EpcDataModel?): JourneyDataBuilder {
        journeyData[NonStepJourneyDataKey.AutoMatchedEpc.key] = Json.encodeToString(epcDetails)
        return this
    }

    fun withCheckAutoMatchedEpcResult(matchedEpcIsCorrect: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.CheckAutoMatchedEpc.urlPathSegment] =
            mapOf(CheckMatchedEpcFormModel::matchedEpcIsCorrect.name to matchedEpcIsCorrect)
        return this
    }

    fun withCheckMatchedEpcResult(matchedEpcIsCorrect: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment] =
            mapOf(CheckMatchedEpcFormModel::matchedEpcIsCorrect.name to matchedEpcIsCorrect)
        return this
    }

    fun withResetCheckMatchedEpcResult(): JourneyDataBuilder {
        journeyData.remove(PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment)
        return this
    }

    fun withEpcLookupCertificateNumber(certificateNumber: String = CURRENT_EPC_CERTIFICATE_NUMBER): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EpcLookup.urlPathSegment] =
            mapOf(EpcLookupFormModel::certificateNumber.name to certificateNumber)
        return this
    }

    fun withLookedUpEpcDetails(epcDetails: EpcDataModel): JourneyDataBuilder {
        journeyData[NonStepJourneyDataKey.LookedUpEpc.key] = Json.encodeToString(epcDetails)
        return this
    }

    fun withNullLookedUpEpcDetails(): JourneyDataBuilder {
        journeyData[NonStepJourneyDataKey.LookedUpEpc.key] = null
        return this
    }

    fun withEpcSuperseded(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EpcSuperseded.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withEpcExemptionReason(epcExemptionReason: EpcExemptionReason): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EpcExemptionReason.urlPathSegment] =
            mapOf(EpcExemptionReasonFormModel::exemptionReason.name to epcExemptionReason)
        return this
    }

    fun withEpcExemptionConfirmationStep(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EpcExemptionConfirmation.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withEpcMissingStep(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EpcMissing.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withEpcExpiryCheckStep(tenancyStartedBeforeExpiry: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EpcExpiryCheck.urlPathSegment] =
            mapOf(EpcExpiryCheckFormModel::tenancyStartedBeforeExpiry.name to tenancyStartedBeforeExpiry)
        return this
    }

    fun withEpcExpiredStep(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EpcExpired.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withEpcNotFoundStep(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.EpcNotFound.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withLowEnergyRatingStep(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.LowEnergyRating.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withMeesExemptionCheckStep(hasExemption: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.MeesExemptionCheck.urlPathSegment] =
            mapOf(MeesExemptionCheckFormModel::propertyHasExemption.name to hasExemption)
        return this
    }

    fun withMeesExemptionReasonStep(exemptionReason: MeesExemptionReason): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.MeesExemptionReason.urlPathSegment] =
            mapOf(MeesExemptionReasonFormModel::exemptionReason.name to exemptionReason)
        return this
    }

    fun withMeesExemptionConfirmationStep(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.MeesExemptionConfirmation.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withMissingEpcExemption(): JourneyDataBuilder {
        withEpcStatus(HasEpc.NO)
        journeyData[PropertyComplianceStepId.EpcMissing.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withFireSafetyDeclaration(hasDeclared: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment] =
            mapOf(FireSafetyDeclarationFormModel::hasDeclared.name to hasDeclared)
        return this
    }

    fun withKeepPropertySafeDeclaration(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.KeepPropertySafe.urlPathSegment] =
            mapOf(KeepPropertySafeFormModel::agreesToResponsibility.name to true)
        return this
    }

    fun withResponsibilityToTenantsDeclaration(): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.ResponsibilityToTenants.urlPathSegment] =
            mapOf(ResponsibilityToTenantsFormModel::agreesToResponsibility.name to true)
        return this
    }

    fun withLandingPageReached(): JourneyDataBuilder {
        journeyData[RegisterLaUserStepId.LandingPage.urlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withName(name: String = "Mary Margaret"): JourneyDataBuilder {
        journeyData[RegisterLaUserStepId.Name.urlPathSegment] = mapOf(NameFormModel::name.name to name)
        return this
    }

    fun withWantsToProceed(): JourneyDataBuilder {
        journeyData[DeregisterPropertyStepId.AreYouSure.urlPathSegment] =
            mapOf(PropertyDeregistrationAreYouSureFormModel::wantsToProceed.name to true)
        return this
    }
}
