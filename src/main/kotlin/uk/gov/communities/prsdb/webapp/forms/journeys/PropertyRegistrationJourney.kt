package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITIES
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.pages.AlreadyRegisteredPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.SelectAddressPage
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.dataModels.FormSummaryDataModel
import uk.gov.communities.prsdb.webapp.models.formModels.HmoAdditionalLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.HmoMandatoryLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.SelectLocalAuthorityFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.SelectiveLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService

@Component
class PropertyRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    addressLookupService: AddressLookupService,
    addressDataService: AddressDataService,
    propertyRegistrationService: PropertyRegistrationService,
) : Journey<RegisterPropertyStepId>(
        journeyType = JourneyType.PROPERTY_REGISTRATION,
        initialStepId = RegisterPropertyStepId.LookupAddress,
        validator = validator,
        journeyDataService = journeyDataService,
        steps =
            setOf(
                lookupAddressStep(),
                selectAddressStep(journeyDataService, addressLookupService, addressDataService, propertyRegistrationService),
                alreadyRegisteredStep(journeyDataService),
                manualAddressStep(),
                localAuthorityStep(),
                propertyTypeStep(),
                ownershipTypeStep(),
                occupancyStep(),
                numberOfHouseholdsStep(),
                numberOfPeopleStep(),
                licensingTypeStep(),
                selectiveLicenceStep(),
                hmoMandatoryLicenceStep(),
                hmoAdditionalLicenceStep(),
                Step(
                    id = RegisterPropertyStepId.CheckAnswers,
                    page =
                        Page(
                            formModel = NoInputFormModel::class,
                            templateName = "forms/propertyRegistrationCheckAnswersForm",
                            content =
                                mapOf(
                                    "title" to "registerProperty.title",
                                    "propertyName" to "1 example road EX4 PL3",
                                    "submitButtonText" to "forms.buttons.saveAndContinue",
                                    "propertyDetails" to
                                        listOf(
                                            FormSummaryDataModel(
                                                "forms.checkPropertyAnswers.propertyDetails.address",
                                                "1 example road EX4 PL3",
                                                null,
                                            ),
                                            FormSummaryDataModel(
                                                "forms.checkPropertyAnswers.propertyDetails.uprn",
                                                "100023584755",
                                                null,
                                            ),
                                            FormSummaryDataModel(
                                                "forms.checkPropertyAnswers.propertyDetails.type",
                                                "Flat",
                                                null,
                                            ),
                                            FormSummaryDataModel(
                                                "forms.checkPropertyAnswers.propertyDetails.ownership",
                                                "Freehold",
                                                null,
                                            ),
                                            FormSummaryDataModel(
                                                "forms.checkPropertyAnswers.propertyDetails.landlordType",
                                                "Individual",
                                                null,
                                            ),
                                        ),
                                ),
                        ),
                    nextAction = { _, _ -> Pair(RegisterPropertyStepId.PlaceholderPage, null) },
                ),
                Step(
                    id = RegisterPropertyStepId.PlaceholderPage,
                    page =
                        Page(
                            formModel = NoInputFormModel::class,
                            templateName = "placeholder",
                            content =
                                mapOf(
                                    "title" to "registerProperty.title",
                                ),
                        ),
                ),
            ),
    ) {
    companion object {
        private fun lookupAddressStep() =
            Step(
                id = RegisterPropertyStepId.LookupAddress,
                page =
                    Page(
                        formModel = LookupAddressFormModel::class,
                        templateName = "forms/lookupAddressForm",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.lookupAddress.propertyRegistration.fieldSetHeading",
                                "fieldSetHint" to "forms.lookupAddress.fieldSetHint",
                                "postcodeLabel" to "forms.lookupAddress.postcode.label",
                                "postcodeHint" to "forms.lookupAddress.postcode.hint",
                                "houseNameOrNumberLabel" to "forms.lookupAddress.houseNameOrNumber.label",
                                "houseNameOrNumberHint" to "forms.lookupAddress.houseNameOrNumber.hint",
                                "submitButtonText" to "forms.buttons.saveAndContinue",
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterPropertyStepId.SelectAddress, null) },
            )

        private fun selectAddressStep(
            journeyDataService: JourneyDataService,
            addressLookupService: AddressLookupService,
            addressDataService: AddressDataService,
            propertyRegistrationService: PropertyRegistrationService,
        ) = Step(
            id = RegisterPropertyStepId.SelectAddress,
            page =
                SelectAddressPage(
                    formModel = SelectAddressFormModel::class,
                    templateName = "forms/selectAddressForm",
                    content =
                        mapOf(
                            "title" to "registerProperty.title",
                            "fieldSetHeading" to "forms.selectAddress.fieldSetHeading",
                            "submitButtonText" to "forms.buttons.useThisAddress",
                            "searchAgainUrl" to
                                "/$REGISTER_PROPERTY_JOURNEY_URL/" +
                                RegisterPropertyStepId.LookupAddress.urlPathSegment,
                        ),
                    urlPathSegment = RegisterPropertyStepId.LookupAddress.urlPathSegment,
                    journeyDataService = journeyDataService,
                    addressLookupService = addressLookupService,
                    addressDataService = addressDataService,
                ),
            nextAction = { journeyData, _ ->
                selectAddressNextAction(
                    journeyData,
                    journeyDataService,
                    addressDataService,
                    propertyRegistrationService,
                )
            },
        )

        private fun alreadyRegisteredStep(journeyDataService: JourneyDataService) =
            Step(
                id = RegisterPropertyStepId.AlreadyRegistered,
                page =
                    AlreadyRegisteredPage(
                        formModel = NoInputFormModel::class,
                        templateName = "alreadyRegisteredPropertyPage",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "searchAgainUrl" to
                                    "/$REGISTER_PROPERTY_JOURNEY_URL/" +
                                    RegisterPropertyStepId.LookupAddress.urlPathSegment,
                            ),
                        journeyDataService = journeyDataService,
                        urlPathSegment = RegisterPropertyStepId.SelectAddress.urlPathSegment,
                    ),
            )

        private fun manualAddressStep() =
            Step(
                id = RegisterPropertyStepId.ManualAddress,
                page =
                    Page(
                        formModel = ManualAddressFormModel::class,
                        templateName = "forms/manualAddressForm",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.manualAddress.propertyRegistration.fieldSetHeading",
                                "fieldSetHint" to "forms.manualAddress.fieldSetHint",
                                "addressLineOneLabel" to "forms.manualAddress.addressLineOne.label",
                                "addressLineTwoLabel" to "forms.manualAddress.addressLineTwo.label",
                                "townOrCityLabel" to "forms.manualAddress.townOrCity.label",
                                "countyLabel" to "forms.manualAddress.county.label",
                                "postcodeLabel" to "forms.manualAddress.postcode.label",
                                "submitButtonText" to "forms.buttons.saveAndContinue",
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterPropertyStepId.LocalAuthority, null) },
            )

        private fun localAuthorityStep() =
            Step(
                id = RegisterPropertyStepId.LocalAuthority,
                page =
                    Page(
                        formModel = SelectLocalAuthorityFormModel::class,
                        templateName = "forms/selectLocalAuthorityForm",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.selectLocalAuthority.fieldSetHeading",
                                "fieldSetHint" to "forms.selectLocalAuthority.fieldSetHint",
                                "selectLabel" to "forms.selectLocalAuthority.select.label",
                                "selectOptions" to
                                    LOCAL_AUTHORITIES.map {
                                        SelectViewModel(
                                            value = it.custodianCode,
                                            label = it.displayName,
                                        )
                                    },
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterPropertyStepId.PropertyType, null) },
            )

        private fun propertyTypeStep() =
            Step(
                id = RegisterPropertyStepId.PropertyType,
                page =
                    Page(
                        formModel = PropertyTypeFormModel::class,
                        templateName = "forms/propertyTypeForm.html",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.propertyType.fieldSetHeading",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = PropertyType.DETACHED_HOUSE,
                                            labelMsgKey = "forms.propertyType.radios.option.detachedHouse.label",
                                            hintMsgKey = "forms.propertyType.radios.option.detachedHouse.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = PropertyType.SEMI_DETACHED_HOUSE,
                                            labelMsgKey = "forms.propertyType.radios.option.semiDetachedHouse.label",
                                            hintMsgKey = "forms.propertyType.radios.option.semiDetachedHouse.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = PropertyType.TERRACED_HOUSE,
                                            labelMsgKey = "forms.propertyType.radios.option.terracedHouse.label",
                                            hintMsgKey = "forms.propertyType.radios.option.terracedHouse.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = PropertyType.FLAT,
                                            labelMsgKey = "forms.propertyType.radios.option.flat.label",
                                            hintMsgKey = "forms.propertyType.radios.option.flat.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = PropertyType.OTHER,
                                            labelMsgKey = "forms.propertyType.radios.option.other.label",
                                            hintMsgKey = "forms.propertyType.radios.option.other.hint",
                                            conditionalFragment = "customPropertyTypeInput",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterPropertyStepId.OwnershipType, null) },
            )

        private fun ownershipTypeStep() =
            Step(
                id = RegisterPropertyStepId.OwnershipType,
                page =
                    Page(
                        formModel = OwnershipTypeFormModel::class,
                        templateName = "forms/ownershipTypeForm.html",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.ownershipType.fieldSetHeading",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = OwnershipType.FREEHOLD,
                                            labelMsgKey = "forms.ownershipType.radios.option.freehold.label",
                                            hintMsgKey = "forms.ownershipType.radios.option.freehold.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = OwnershipType.LEASEHOLD,
                                            labelMsgKey = "forms.ownershipType.radios.option.leasehold.label",
                                            hintMsgKey = "forms.ownershipType.radios.option.leasehold.hint",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterPropertyStepId.Occupancy, null) },
            )

        private fun occupancyStep() =
            Step(
                id = RegisterPropertyStepId.Occupancy,
                page =
                    Page(
                        formModel = OccupancyFormModel::class,
                        templateName = "forms/propertyOccupancyForm",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.occupancy.fieldSetHeading",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = true,
                                            labelMsgKey = "forms.occupancy.radios.option.yes.label",
                                            hintMsgKey = "forms.occupancy.radios.option.yes.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = false,
                                            labelMsgKey = "forms.occupancy.radios.option.no.label",
                                            hintMsgKey = "forms.occupancy.radios.option.no.hint",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { journeyData, _ -> occupancyNextAction(journeyData) },
            )

        private fun numberOfHouseholdsStep() =
            Step(
                id = RegisterPropertyStepId.NumberOfHouseholds,
                page =
                    Page(
                        formModel = NumberOfHouseholdsFormModel::class,
                        templateName = "forms/numberOfHouseholdsForm",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.numberOfHouseholds.fieldSetHeading",
                                "label" to "forms.numberOfHouseholds.label",
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterPropertyStepId.NumberOfPeople, null) },
            )

        private fun numberOfPeopleStep() =
            Step(
                id = RegisterPropertyStepId.NumberOfPeople,
                page =
                    Page(
                        formModel = NumberOfPeopleFormModel::class,
                        templateName = "forms/numberOfPeopleForm",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.numberOfPeople.fieldSetHeading",
                                "fieldSetHint" to "forms.numberOfPeople.fieldSetHint",
                                "label" to "forms.numberOfPeople.label",
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterPropertyStepId.LicensingType, null) },
            )

        private fun licensingTypeStep() =
            Step(
                id = RegisterPropertyStepId.LicensingType,
                page =
                    Page(
                        formModel = LicensingTypeFormModel::class,
                        templateName = "forms/licensingTypeForm",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.licensingType.fieldSetHeading",
                                "fieldSetHint" to "forms.licensingType.fieldSetHint",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = LicensingType.SELECTIVE_LICENCE,
                                            labelMsgKey = "forms.licensingType.radios.option.selectiveLicence.label",
                                            hintMsgKey = "forms.licensingType.radios.option.selectiveLicence.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = LicensingType.HMO_MANDATORY_LICENCE,
                                            labelMsgKey = "forms.licensingType.radios.option.hmoMandatory.label",
                                            hintMsgKey = "forms.licensingType.radios.option.hmoMandatory.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = LicensingType.HMO_ADDITIONAL_LICENCE,
                                            labelMsgKey = "forms.licensingType.radios.option.hmoAdditional.label",
                                            hintMsgKey = "forms.licensingType.radios.option.hmoAdditional.hint",
                                        ),
                                        RadiosDividerViewModel("forms.radios.dividerText"),
                                        RadiosButtonViewModel(
                                            value = LicensingType.NO_LICENSING,
                                            labelMsgKey = "forms.licensingType.radios.option.noLicensing.label",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { journeyData, _ -> licensingTypeNextAction(journeyData) },
            )

        private fun selectiveLicenceStep() =
            Step(
                id = RegisterPropertyStepId.SelectiveLicence,
                page =
                    Page(
                        formModel = SelectiveLicenceFormModel::class,
                        templateName = "forms/licenceNumberForm",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.selectiveLicence.fieldSetHeading",
                                "label" to "forms.selectiveLicence.label",
                                "detailSummary" to "forms.selectiveLicence.detail.summary",
                                "detailMainText" to "forms.selectiveLicence.detail.text",
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterPropertyStepId.PlaceholderPage, null) },
            )

        private fun hmoMandatoryLicenceStep() =
            Step(
                id = RegisterPropertyStepId.HmoMandatoryLicence,
                page =
                    Page(
                        formModel = HmoMandatoryLicenceFormModel::class,
                        templateName = "forms/licenceNumberForm",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.hmoMandatoryLicence.fieldSetHeading",
                                "label" to "forms.hmoMandatoryLicence.label",
                                "detailSummary" to "forms.hmoMandatoryLicence.detail.summary",
                                "detailMainText" to "forms.hmoMandatoryLicence.detail.paragraph.one",
                                "detailAdditionalContent" to
                                    mapOf(
                                        "bulletOne" to "forms.hmoMandatoryLicence.detail.bullet.one",
                                        "bulletTwo" to "forms.hmoMandatoryLicence.detail.bullet.two",
                                        "text" to "forms.hmoMandatoryLicence.detail.paragraph.two",
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterPropertyStepId.PlaceholderPage, null) },
            )

        private fun hmoAdditionalLicenceStep() =
            Step(
                id = RegisterPropertyStepId.HmoAdditionalLicence,
                page =
                    Page(
                        formModel = HmoAdditionalLicenceFormModel::class,
                        templateName = "forms/licenceNumberForm",
                        content =
                            mapOf(
                                "title" to "registerProperty.title",
                                "fieldSetHeading" to "forms.hmoAdditionalLicence.fieldSetHeading",
                                "label" to "forms.hmoAdditionalLicence.label",
                                "detailSummary" to "forms.hmoAdditionalLicence.detail.summary",
                                "detailMainText" to "forms.hmoAdditionalLicence.detail.text",
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterPropertyStepId.PlaceholderPage, null) },
            )

        private fun occupancyNextAction(journeyData: JourneyData): Pair<RegisterPropertyStepId, Int?> =
            when (
                val propertyIsOccupied =
                    objectToStringKeyedMap(journeyData[RegisterPropertyStepId.Occupancy.urlPathSegment])
                        ?.get("occupied")
                        .toString()
            ) {
                "true" -> Pair(RegisterPropertyStepId.NumberOfHouseholds, null)
                "false" -> Pair(RegisterPropertyStepId.PlaceholderPage, null)
                else -> throw IllegalArgumentException(
                    "Invalid value for journeyData[\"${RegisterPropertyStepId.Occupancy.urlPathSegment}\"][\"occupied\"]:" +
                        propertyIsOccupied,
                )
            }

        private fun selectAddressNextAction(
            journeyData: JourneyData,
            journeyDataService: JourneyDataService,
            addressDataService: AddressDataService,
            propertyRegistrationService: PropertyRegistrationService,
        ): Pair<RegisterPropertyStepId, Int?> {
            val singleLineAddress =
                journeyDataService
                    .getFieldStringValue(journeyData, RegisterPropertyStepId.SelectAddress.urlPathSegment, "address")
            if (singleLineAddress == MANUAL_ADDRESS_CHOSEN || singleLineAddress == null) {
                return Pair(RegisterPropertyStepId.ManualAddress, null)
            } else {
                val addressData = addressDataService.getAddressData(singleLineAddress)
                if (addressData?.uprn != null && propertyRegistrationService.getIsAddressRegistered(addressData.uprn)) {
                    return Pair(RegisterPropertyStepId.AlreadyRegistered, null)
                }
                return Pair(RegisterPropertyStepId.PropertyType, null)
            }
        }

        private fun licensingTypeNextAction(journeyData: JourneyData): Pair<RegisterPropertyStepId, Int?> {
            val licensingTypePageData = objectToStringKeyedMap(journeyData[RegisterPropertyStepId.LicensingType.urlPathSegment])
            val licensingType = LicensingType.valueOf(licensingTypePageData?.get("licensingType") as String)

            return when (licensingType) {
                LicensingType.SELECTIVE_LICENCE -> Pair(RegisterPropertyStepId.SelectiveLicence, null)
                LicensingType.HMO_MANDATORY_LICENCE -> Pair(RegisterPropertyStepId.HmoMandatoryLicence, null)
                LicensingType.HMO_ADDITIONAL_LICENCE -> Pair(RegisterPropertyStepId.HmoAdditionalLicence, null)
                LicensingType.NO_LICENSING -> Pair(RegisterPropertyStepId.PlaceholderPage, null)
            }
        }
    }
}
