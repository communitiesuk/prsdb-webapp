package uk.gov.communities.prsdb.webapp.forms.journeys

import jakarta.persistence.EntityExistsException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.forms.pages.AlreadyRegisteredPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PropertyRegistrationCheckAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.SelectAddressPage
import uk.gov.communities.prsdb.webapp.forms.pages.SelectLocalAuthorityPage
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DeclarationFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoAdditionalLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoMandatoryLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectiveLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService

@Component
class PropertyRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val addressLookupService: AddressLookupService,
    private val addressDataService: AddressDataService,
    private val propertyRegistrationService: PropertyRegistrationService,
    private val localAuthorityService: LocalAuthorityService,
    private val landlordService: LandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>,
) : JourneyWithTaskList<RegisterPropertyStepId>(
        journeyType = JourneyType.PROPERTY_REGISTRATION,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    override val initialStepId = RegisterPropertyStepId.LookupAddress

    override val taskListUrlSegment: String = "task-list"

    override val sections =
        listOf(
            JourneySection(registerPropertyTasks(), "registerProperty.taskList.register.heading"),
            JourneySection(checkAndSubmitPropertiesTasks(), "registerProperty.taskList.checkAndSubmit.heading"),
        )

    override val journeyPathSegment = REGISTER_PROPERTY_JOURNEY_URL

    override val taskListFactory =
        getTaskListViewModelFactory(
            "registerProperty.title",
            "registerProperty.taskList.heading",
            "registerProperty.taskList.subtitle",
            "register-property-task",
        )

    private fun registerPropertyTasks(): List<JourneyTask<RegisterPropertyStepId>> =
        listOf(
            addressTask(),
            JourneyTask.withOneStep(
                propertyTypeStep(),
                "registerProperty.taskList.register.selectType",
            ),
            JourneyTask.withOneStep(
                ownershipTypeStep(),
                "registerProperty.taskList.register.selectOwnership",
            ),
            licensingTask(),
            occupancyTask(),
        )

    private fun checkAndSubmitPropertiesTasks(): List<JourneyTask<RegisterPropertyStepId>> =
        listOf(
            JourneyTask.withOneStep(
                checkAnswersStep(),
                "registerProperty.taskList.checkAndSubmit.checkAnswers",
            ),
            JourneyTask.withOneStep(
                declarationStep(),
            ),
        )

    private fun addressTask() =
        JourneyTask(
            RegisterPropertyStepId.LookupAddress,
            setOf(
                lookupAddressStep(),
                selectAddressStep(),
                alreadyRegisteredStep(),
                manualAddressStep(),
                localAuthorityStep(),
            ),
            "registerProperty.taskList.register.addAddress",
        )

    private fun licensingTask() =
        JourneyTask(
            RegisterPropertyStepId.LicensingType,
            setOf(
                licensingTypeStep(),
                selectiveLicenceStep(),
                hmoMandatoryLicenceStep(),
                hmoAdditionalLicenceStep(),
            ),
            "registerProperty.taskList.register.addLicensing",
        )

    private fun occupancyTask() =
        JourneyTask(
            RegisterPropertyStepId.Occupancy,
            setOf(
                occupancyStep(),
                numberOfHouseholdsStep(),
                numberOfPeopleStep(),
            ),
            "registerProperty.taskList.register.addTenancyInfo",
        )

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
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(RegisterPropertyStepId.SelectAddress, null) },
        )

    private fun selectAddressStep() =
        Step(
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
                    lookupAddressPathSegment = RegisterPropertyStepId.LookupAddress.urlPathSegment,
                    addressLookupService = addressLookupService,
                    addressDataService = addressDataService,
                    displaySectionHeader = true,
                ),
            nextAction = { journeyData, _ ->
                selectAddressNextAction(
                    journeyData,
                    addressDataService,
                    propertyRegistrationService,
                )
            },
        )

    private fun alreadyRegisteredStep() =
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
                    selectedAddressPathSegment = RegisterPropertyStepId.SelectAddress.urlPathSegment,
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
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(RegisterPropertyStepId.LocalAuthority, null) },
        )

    private fun localAuthorityStep() =
        Step(
            id = RegisterPropertyStepId.LocalAuthority,
            page =
                SelectLocalAuthorityPage(
                    content =
                        mapOf(
                            "title" to "registerProperty.title",
                            "fieldSetHeading" to "forms.selectLocalAuthority.fieldSetHeading",
                            "fieldSetHint" to "forms.selectLocalAuthority.fieldSetHint",
                            "selectLabel" to "forms.selectLocalAuthority.select.label",
                        ),
                    localAuthorityService = localAuthorityService,
                    displaySectionHeader = true,
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
                    shouldDisplaySectionHeader = true,
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
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(RegisterPropertyStepId.LicensingType, null) },
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
                    shouldDisplaySectionHeader = true,
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
                    shouldDisplaySectionHeader = true,
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
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(RegisterPropertyStepId.CheckAnswers, null) },
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
                    shouldDisplaySectionHeader = true,
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
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(RegisterPropertyStepId.Occupancy, null) },
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
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(RegisterPropertyStepId.Occupancy, null) },
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
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(RegisterPropertyStepId.Occupancy, null) },
        )

    private fun checkAnswersStep() =
        Step(
            id = RegisterPropertyStepId.CheckAnswers,
            page =
                PropertyRegistrationCheckAnswersPage(
                    addressDataService,
                    localAuthorityService,
                    displaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(RegisterPropertyStepId.Declaration, null) },
        )

    private fun declarationStep() =
        Step(
            id = RegisterPropertyStepId.Declaration,
            page =
                Page(
                    formModel = DeclarationFormModel::class,
                    templateName = "forms/declarationForm",
                    content =
                        mapOf(
                            "title" to "registerProperty.title",
                            "bulletOneFineAmount" to "forms.declaration.fines.propertyRegistrationJourneyAmount",
                            "bulletTwoFineAmount" to "forms.declaration.fines.propertyRegistrationJourneyAmount",
                            "options" to
                                listOf(
                                    CheckboxViewModel(
                                        value = "true",
                                        labelMsgKey = "forms.declaration.checkbox.label",
                                    ),
                                ),
                            "submitButtonText" to "forms.buttons.confirmAndCompleteRegistration",
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            handleSubmitAndRedirect = { _, _ -> checkAnswersSubmitAndRedirect() },
        )

    private fun occupancyNextAction(journeyData: JourneyData): Pair<RegisterPropertyStepId, Int?> =
        if (PropertyRegistrationJourneyDataHelper.getIsOccupied(journeyData)!!) {
            Pair(RegisterPropertyStepId.NumberOfHouseholds, null)
        } else {
            Pair(RegisterPropertyStepId.CheckAnswers, null)
        }

    private fun selectAddressNextAction(
        journeyData: JourneyData,
        addressDataService: AddressDataService,
        propertyRegistrationService: PropertyRegistrationService,
    ): Pair<RegisterPropertyStepId, Int?> =
        if (PropertyRegistrationJourneyDataHelper.isManualAddressChosen(journeyData)) {
            Pair(RegisterPropertyStepId.ManualAddress, null)
        } else {
            val selectedAddress =
                PropertyRegistrationJourneyDataHelper.getAddress(journeyData, addressDataService)!!
            val selectedAddressData = addressDataService.getAddressData(selectedAddress.singleLineAddress)!!
            if (selectedAddressData.uprn != null &&
                propertyRegistrationService.getIsAddressRegistered(selectedAddressData.uprn)
            ) {
                Pair(RegisterPropertyStepId.AlreadyRegistered, null)
            } else {
                Pair(RegisterPropertyStepId.PropertyType, null)
            }
        }

    private fun licensingTypeNextAction(journeyData: JourneyData): Pair<RegisterPropertyStepId, Int?> =
        when (PropertyRegistrationJourneyDataHelper.getLicensingType(journeyData)!!) {
            LicensingType.SELECTIVE_LICENCE -> Pair(RegisterPropertyStepId.SelectiveLicence, null)
            LicensingType.HMO_MANDATORY_LICENCE -> Pair(RegisterPropertyStepId.HmoMandatoryLicence, null)
            LicensingType.HMO_ADDITIONAL_LICENCE -> Pair(RegisterPropertyStepId.HmoAdditionalLicence, null)
            LicensingType.NO_LICENSING -> Pair(RegisterPropertyStepId.Occupancy, null)
        }

    private fun checkAnswersSubmitAndRedirect(): String {
        try {
            val journeyData = last().filteredJourneyData
            val address = PropertyRegistrationJourneyDataHelper.getAddress(journeyData, addressDataService)!!
            val baseUserId = SecurityContextHolder.getContext().authentication.name
            val propertyRegistrationNumber =
                propertyRegistrationService.registerPropertyAndReturnPropertyRegistrationNumber(
                    address = address,
                    propertyType = PropertyRegistrationJourneyDataHelper.getPropertyType(journeyData)!!,
                    licenseType = PropertyRegistrationJourneyDataHelper.getLicensingType(journeyData)!!,
                    licenceNumber = PropertyRegistrationJourneyDataHelper.getLicenseNumber(journeyData)!!,
                    ownershipType = PropertyRegistrationJourneyDataHelper.getOwnershipType(journeyData)!!,
                    numberOfHouseholds = PropertyRegistrationJourneyDataHelper.getNumberOfHouseholds(journeyData),
                    numberOfPeople = PropertyRegistrationJourneyDataHelper.getNumberOfTenants(journeyData),
                    baseUserId = baseUserId,
                )

            propertyRegistrationService.setLastPrnRegisteredThisSession(propertyRegistrationNumber.number)

            confirmationEmailSender.sendEmail(
                landlordService.retrieveLandlordByBaseUserId(baseUserId)!!.email,
                PropertyRegistrationConfirmationEmail(
                    RegistrationNumberDataModel.fromRegistrationNumber(propertyRegistrationNumber).toString(),
                    address.singleLineAddress,
                    absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                ),
            )

            journeyDataService.deleteJourneyData()

            return CONFIRMATION_PAGE_PATH_SEGMENT
        } catch (exception: EntityExistsException) {
            return RegisterPropertyStepId.AlreadyRegistered.urlPathSegment
        }
    }
}
