package uk.gov.communities.prsdb.webapp.forms.journeys

import kotlinx.datetime.toJavaLocalDate
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.GOVERNMENT_APPROVED_DEPOSIT_PROTECTION_SCHEME_URL
import uk.gov.communities.prsdb.webapp.constants.HOMES_ACT_2018_URL
import uk.gov.communities.prsdb.webapp.constants.HOUSES_IN_MULTIPLE_OCCUPATION_URL
import uk.gov.communities.prsdb.webapp.constants.HOUSING_HEALTH_AND_SAFETY_RATING_SYSTEM_URL
import uk.gov.communities.prsdb.webapp.constants.HOW_TO_RENT_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.PRIVATE_RENTING_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.RIGHT_TO_RENT_CHECKS_URL
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PageWithContentProvider
import uk.gov.communities.prsdb.webapp.forms.pages.PropertyComplianceCheckAnswersPage
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.factories.PropertyComplianceSharedStepFactory
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.GroupedJourneyExtensions.Companion.withBackUrlIfNotNullAndNotCheckingAnswers
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAcceptedEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getDidTenancyStartBeforeEpcExpiry
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrOriginalName
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcLookupCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertEngineerNum
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertOriginalName
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEICR
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEPC
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasFireSafetyDeclaration
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getLatestEpcCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMeesExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.withEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.withResetCheckMatchedEpc
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FireSafetyDeclarationFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.KeepPropertySafeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ResponsibilityToTenantsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyComplianceConfirmationMessageKeys
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailBulletPointList
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.FullPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PartialPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

class PropertyComplianceJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val epcLookupService: EpcLookupService,
    private val propertyComplianceService: PropertyComplianceService,
    private val propertyOwnershipId: Long,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val messageSource: MessageSource,
    private val fullPropertyComplianceConfirmationEmailService: EmailNotificationService<FullPropertyComplianceConfirmationEmail>,
    private val partialPropertyComplianceConfirmationEmailService: EmailNotificationService<PartialPropertyComplianceConfirmationEmail>,
    private val urlProvider: AbsoluteUrlProvider,
    checkingAnswersForStep: String?,
    stepName: String,
) : JourneyWithTaskList<PropertyComplianceStepId>(
        journeyType = JourneyType.PROPERTY_COMPLIANCE,
        initialStepId = initialStepId,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    init {
        loadJourneyDataIfNotLoaded()
    }

    private fun loadJourneyDataIfNotLoaded() {
        if (journeyDataService.getJourneyDataFromSession().isEmpty()) {
            val formContext =
                propertyOwnershipService.getPropertyOwnership(propertyOwnershipId).incompleteComplianceForm
                    ?: throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Property ownership $propertyOwnershipId does not have an incomplete compliance form",
                    )

            journeyDataService.loadJourneyDataIntoSession(formContext)
        }
    }

    private val isCheckingAnswers = checkingAnswersForStep != null
    private val checkingAnswersFor = PropertyComplianceStepId.entries.find { it.urlPathSegment == checkingAnswersForStep }

    override val stepRouter = GroupedStepRouter(this)
    override val checkYourAnswersStepId = PropertyComplianceStepId.CheckAndSubmit

    private val propertyComplianceSharedStepFactory =
        PropertyComplianceSharedStepFactory(
            defaultSaveAfterSubmit = true,
            isUpdateJourney = false,
            isCheckingOrUpdatingAnswers = isCheckingAnswers,
            journeyDataService = journeyDataService,
            epcCertificateUrlProvider = epcCertificateUrlProvider,
            stepName = stepName,
        )

    override val sections =
        listOf(
            JourneySection(
                uploadTasks,
                "propertyCompliance.taskList.upload.heading",
                "upload-certificates",
            ),
            JourneySection(
                landlordResponsibilities,
                "propertyCompliance.taskList.landlordResponsibilities.heading",
                "landlord-responsibilities",
            ),
            JourneySection.withOneTask(
                JourneyTask.withOneStep(checkAndSubmitStep, "propertyCompliance.taskList.checkAndSubmit.check"),
                "propertyCompliance.taskList.checkAndSubmit.heading",
                "check-and-submit",
            ),
        )

    override val taskListFactory =
        getTaskListViewModelFactory(
            "propertyCompliance.title",
            "propertyCompliance.taskList.heading",
            listOf(
                "propertyCompliance.taskList.subtitle.one",
                "propertyCompliance.taskList.subtitle.two",
                "propertyCompliance.taskList.subtitle.three",
            ),
            numberSections = false,
            backUrl = LANDLORD_DASHBOARD_URL,
        )

    private val uploadTasks
        get() =
            listOf(
                gasSafetyTask,
                eicrTask,
                epcTask,
            )

    private val landlordResponsibilities
        get() =
            listOf(
                fireSafetyTask,
                JourneyTask.withOneStep(
                    keepPropertySafeStep,
                    "propertyCompliance.taskList.landlordResponsibilities.keepPropertySafe",
                ),
                JourneyTask.withOneStep(
                    responsibilityToTenantsStep,
                    "propertyCompliance.taskList.landlordResponsibilities.tenants",
                ),
            )

    private val gasSafetyTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.GasSafety,
                setOf(
                    gasSafetyStep,
                    propertyComplianceSharedStepFactory.createGasSafetyIssueDateStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyEngineerNumStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyUploadStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyUploadConfirmationStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyOutdatedStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyExemptionStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyExemptionReasonStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyExemptionOtherReasonStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyExemptionConfirmationStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyExemptionMissingStep(),
                ),
                "propertyCompliance.taskList.upload.gasSafety",
            )

    private val eicrTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.EICR,
                setOf(
                    eicrStep,
                    propertyComplianceSharedStepFactory.createEicrIssueDateStep(),
                    propertyComplianceSharedStepFactory.createEicrUploadStep(),
                    propertyComplianceSharedStepFactory.createEicrUploadConfirmationStep(),
                    propertyComplianceSharedStepFactory.createEicrOutdatedStep(),
                    propertyComplianceSharedStepFactory.createEicrExemptionStep(),
                    propertyComplianceSharedStepFactory.createEicrExemptionReasonStep(),
                    propertyComplianceSharedStepFactory.createEicrExemptionOtherReasonStep(),
                    propertyComplianceSharedStepFactory.createEicrExemptionConfirmationStep(),
                    propertyComplianceSharedStepFactory.createEicrExemptionMissingStep(),
                ),
                "propertyCompliance.taskList.upload.eicr",
            )

    private val epcLookupStep
        get() =
            propertyComplianceSharedStepFactory.createEpcLookupStep(
                handleSubmitAndRedirect = { filteredJourneyData ->
                    epcLookupStepHandleSubmitAndRedirect(filteredJourneyData)
                },
            )

    private val checkMatchedEpcStep
        get() =
            propertyComplianceSharedStepFactory.createCheckMatchedEpcStep(
                handleSubmitAndRedirect = { filteredJourneyData ->
                    checkMatchedEpcStepHandleSubmitAndRedirect(filteredJourneyData)
                },
            )

    private val epcTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.EPC,
                setOf(
                    epcStep,
                    propertyComplianceSharedStepFactory.createEpcNotAutoMatchedStep(),
                    propertyComplianceSharedStepFactory.createCheckAutoMatchedEpcStep(),
                    epcLookupStep,
                    checkMatchedEpcStep,
                    propertyComplianceSharedStepFactory.createEpcNotFoundStep(),
                    propertyComplianceSharedStepFactory.createEpcSupersededStep(
                        handleSubmitAndRedirect = { filteredJourneyData ->
                            epcSupersededHandleSubmitAndRedirect(filteredJourneyData)
                        },
                    ),
                    propertyComplianceSharedStepFactory.createEpcExpiryCheckStep(),
                    propertyComplianceSharedStepFactory.createEpcExpiredStep(),
                    propertyComplianceSharedStepFactory.createEpcMissingStep(),
                    propertyComplianceSharedStepFactory.createEpcExemptionReasonStep(),
                    propertyComplianceSharedStepFactory.createEpcExemptionConfirmationStep(),
                    propertyComplianceSharedStepFactory.createMeesExemptionCheckStep(),
                    propertyComplianceSharedStepFactory.createMeesExemptionReasonStep(),
                    propertyComplianceSharedStepFactory.createMeesExemptionConfirmationStep(),
                    propertyComplianceSharedStepFactory.createLowEnergyRatingStep(),
                ),
                "propertyCompliance.taskList.upload.epc",
                "propertyCompliance.taskList.upload.epc.hint",
            )

    private val fireSafetyTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.FireSafetyDeclaration,
                setOf(
                    fireSafetyDeclarationStep,
                    fireSafetyRiskStep,
                ),
                "propertyCompliance.taskList.landlordResponsibilities.fireSafety",
            )

    private val gasSafetyStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafety,
                page =
                    PageWithContentProvider(
                        formModel = GasSafetyFormModel::class,
                        templateName = "forms/certificateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.gasSafety.fieldSetHeading",
                                "fieldSetHint" to "forms.gasSafety.fieldSetHint",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = true,
                                            valueStr = "yes",
                                            labelMsgKey = "forms.radios.option.yes.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = false,
                                            valueStr = "no",
                                            labelMsgKey = "forms.radios.option.no.label",
                                        ),
                                    ),
                            ).withBackUrlIfNotNullAndNotCheckingAnswers(taskListUrlSegment, isCheckingAnswers),
                    ) { mapOf("address" to getPropertyAddress()) },
                nextAction = { filteredJourneyData, _ -> gasSafetyStepNextAction(filteredJourneyData) },
            )

    private val eicrStep
        get() =
            Step(
                id = PropertyComplianceStepId.EICR,
                page =
                    PageWithContentProvider(
                        formModel = EicrFormModel::class,
                        templateName = "forms/certificateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.eicr.fieldSetHeading",
                                "fieldSetHint" to "forms.eicr.fieldSetHint",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = true,
                                            valueStr = "yes",
                                            labelMsgKey = "forms.radios.option.yes.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = false,
                                            valueStr = "no",
                                            labelMsgKey = "forms.radios.option.no.label",
                                        ),
                                    ),
                            ).withBackUrlIfNotNullAndNotCheckingAnswers(taskListUrlSegment, isCheckingAnswers),
                    ) { mapOf("address" to getPropertyAddress()) },
                nextAction = { filteredJourneyData, _ -> eicrStepNextAction(filteredJourneyData) },
            )

    private val epcStep
        get() =
            Step(
                id = PropertyComplianceStepId.EPC,
                page =
                    PageWithContentProvider(
                        formModel = EpcFormModel::class,
                        templateName = "forms/certificateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.epc.fieldSetHeading",
                                "fieldSetHint" to "forms.epc.fieldSetHint",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = HasEpc.YES,
                                            labelMsgKey = "forms.radios.option.yes.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = HasEpc.NO,
                                            labelMsgKey = "forms.radios.option.no.label",
                                        ),
                                        RadiosDividerViewModel(
                                            "forms.radios.dividerText",
                                        ),
                                        RadiosButtonViewModel(
                                            value = HasEpc.NOT_REQUIRED,
                                            labelMsgKey = "forms.epc.radios.option.notRequired.label",
                                        ),
                                    ),
                            ).withBackUrlIfNotNullAndNotCheckingAnswers(taskListUrlSegment, isCheckingAnswers),
                    ) { mapOf("address" to getPropertyAddress()) },
                handleSubmitAndRedirect = { filteredJourneyData, _, _ -> epcStepHandleSubmitAndRedirect(filteredJourneyData) },
                nextAction = { filteredJourneyData, _ -> epcStepNextAction(filteredJourneyData) },
            )

    private val fireSafetyDeclarationStep
        get() =
            Step(
                id = PropertyComplianceStepId.FireSafetyDeclaration,
                page =
                    Page(
                        formModel = FireSafetyDeclarationFormModel::class,
                        templateName = "forms/fireSafetyDeclarationForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "housesInMultipleOccupationUrl" to HOUSES_IN_MULTIPLE_OCCUPATION_URL,
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = true,
                                            valueStr = "yes",
                                            labelMsgKey = "forms.radios.option.yes.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = false,
                                            valueStr = "no",
                                            labelMsgKey = "forms.radios.option.no.label",
                                        ),
                                    ),
                            ).withBackUrlIfNotNullAndNotCheckingAnswers(taskListUrlSegment, isCheckingAnswers),
                    ),
                nextAction = { filteredJourneyData, _ -> fireSafetyDeclarationStepNextAction(filteredJourneyData) },
            )

    private val fireSafetyRiskStep
        get() =
            Step(
                id = PropertyComplianceStepId.FireSafetyRisk,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/fireSafetyRiskForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.KeepPropertySafe, null) },
            )

    private val keepPropertySafeStep
        get() =
            Step(
                id = PropertyComplianceStepId.KeepPropertySafe,
                page =
                    Page(
                        formModel = KeepPropertySafeFormModel::class,
                        templateName = "forms/keepPropertySafeForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "housingHealthAndSafetyRatingSystemUrl" to
                                    HOUSING_HEALTH_AND_SAFETY_RATING_SYSTEM_URL,
                                "homesAct2018Url" to HOMES_ACT_2018_URL,
                                "options" to
                                    listOf(
                                        CheckboxViewModel(
                                            value = "true",
                                            labelMsgKey = "forms.landlordResponsibilities.keepPropertySafe.checkbox.label",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.ResponsibilityToTenants, null) },
            )

    private val responsibilityToTenantsStep
        get() =
            Step(
                id = PropertyComplianceStepId.ResponsibilityToTenants,
                page =
                    Page(
                        formModel = ResponsibilityToTenantsFormModel::class,
                        templateName = "forms/responsibilityToTenantsForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "privateRentingGuideUrl" to
                                    PRIVATE_RENTING_GUIDE_URL,
                                "rightToRentChecksUrl" to RIGHT_TO_RENT_CHECKS_URL,
                                "governmentApprovedDepositProtectionSchemeUrl" to
                                    GOVERNMENT_APPROVED_DEPOSIT_PROTECTION_SCHEME_URL,
                                "howToRentGuideUrl" to HOW_TO_RENT_GUIDE_URL,
                                "options" to
                                    listOf(
                                        CheckboxViewModel(
                                            value = "true",
                                            labelMsgKey = "forms.landlordResponsibilities.responsibilityToTenants.checkbox.label",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.CheckAndSubmit, null) },
            )

    private val checkAndSubmitStep
        get() =
            Step(
                id = PropertyComplianceStepId.CheckAndSubmit,
                page =
                    PropertyComplianceCheckAnswersPage(
                        journeyDataService,
                        epcCertificateUrlProvider,
                        unreachableStepRedirect,
                    ) { getPropertyAddress() },
                handleSubmitAndRedirect = { filteredJourneyData, _, _ -> checkAndSubmitHandleSubmitAndRedirect(filteredJourneyData) },
            )

    private fun gasSafetyStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getHasGasSafetyCert()!!) {
            Pair(PropertyComplianceStepId.GasSafetyIssueDate, null)
        } else {
            Pair(PropertyComplianceStepId.GasSafetyExemption, null)
        }

    private fun eicrStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getHasEICR()!!) {
            Pair(PropertyComplianceStepId.EicrIssueDate, null)
        } else {
            Pair(PropertyComplianceStepId.EicrExemption, null)
        }

    private fun epcStepHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        if (filteredJourneyData.getHasEPC() == HasEpc.YES) {
            val epcDetails = getAutomatchedEpc(propertyOwnershipId, epcLookupService, propertyOwnershipService)

            val newFilteredJourneyData =
                updateEpcDetailsInSessionAndReturnUpdatedJourneyData(
                    journeyDataService,
                    filteredJourneyData,
                    epcDetails,
                    autoMatchedEpc = true,
                )

            return getRedirectForNextStep(epcStep, newFilteredJourneyData, null, checkingAnswersFor)
        }

        return getRedirectForNextStep(epcStep, filteredJourneyData, null, checkingAnswersFor)
    }

    private fun epcStepNextAction(filteredJourneyData: JourneyData) =
        when (filteredJourneyData.getHasEPC()!!) {
            HasEpc.YES -> {
                if (filteredJourneyData.getEpcDetails(autoMatched = true) != null) {
                    Pair(PropertyComplianceStepId.CheckAutoMatchedEpc, null)
                } else {
                    Pair(PropertyComplianceStepId.EpcNotAutoMatched, null)
                }
            }
            HasEpc.NO -> Pair(PropertyComplianceStepId.EpcMissing, null)
            HasEpc.NOT_REQUIRED -> Pair(PropertyComplianceStepId.EpcExemptionReason, null)
        }

    private fun checkMatchedEpcStepHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val nextAction = propertyComplianceSharedStepFactory.checkMatchedEpcStepNextAction(filteredJourneyData)
        val overriddenRedirectStepId = getRedirectStepOverrideForCheckMatchedEpcStepHandleSubmitAndRedirect(nextAction)

        return getRedirectForNextStep(checkMatchedEpcStep, filteredJourneyData, null, checkingAnswersFor, overriddenRedirectStepId)
    }

    private fun epcLookupStepHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val newFilteredJourneyData =
            epcLookupStepHandleSubmit(
                filteredJourneyData,
                journeyDataService,
                epcLookupService,
            )
        return getRedirectForNextStep(epcLookupStep, newFilteredJourneyData, null, checkingAnswersFor)
    }

    private fun epcSupersededHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val newFilteredJourneyData =
            epcSupersededStepHandleSubmit(
                filteredJourneyData,
                journeyDataService,
                epcLookupService,
            )

        return getRedirectForNextStep(epcLookupStep, newFilteredJourneyData, null, checkingAnswersFor)
    }

    private fun fireSafetyDeclarationStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getHasFireSafetyDeclaration()!!) {
            Pair(PropertyComplianceStepId.KeepPropertySafe, null)
        } else {
            Pair(PropertyComplianceStepId.FireSafetyRisk, null)
        }

    private fun checkAndSubmitHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val gasSafetyCertFilename =
            filteredJourneyData.getGasSafetyCertOriginalName()?.let {
                PropertyComplianceJourneyHelper.getCertFilename(
                    propertyOwnershipId,
                    PropertyComplianceStepId.GasSafetyUpload.urlPathSegment,
                    it,
                )
            }

        val eicrFilename =
            filteredJourneyData.getEicrOriginalName()?.let {
                PropertyComplianceJourneyHelper.getCertFilename(
                    propertyOwnershipId,
                    PropertyComplianceStepId.EicrUpload.urlPathSegment,
                    it,
                )
            }

        val epcDetails = filteredJourneyData.getAcceptedEpcDetails()

        val propertyCompliance =
            propertyComplianceService.createPropertyCompliance(
                propertyOwnershipId = propertyOwnershipId,
                gasSafetyCertS3Key = gasSafetyCertFilename,
                gasSafetyCertIssueDate = filteredJourneyData.getGasSafetyCertIssueDate()?.toJavaLocalDate(),
                gasSafetyCertEngineerNum = filteredJourneyData.getGasSafetyCertEngineerNum(),
                gasSafetyCertExemptionReason = filteredJourneyData.getGasSafetyCertExemptionReason(),
                gasSafetyCertExemptionOtherReason = filteredJourneyData.getGasSafetyCertExemptionOtherReason(),
                eicrS3Key = eicrFilename,
                eicrIssueDate = filteredJourneyData.getEicrIssueDate()?.toJavaLocalDate(),
                eicrExemptionReason = filteredJourneyData.getEicrExemptionReason(),
                eicrExemptionOtherReason = filteredJourneyData.getEicrExemptionOtherReason(),
                epcUrl = epcDetails?.let { epcCertificateUrlProvider.getEpcCertificateUrl(it.certificateNumber) },
                epcExpiryDate = epcDetails?.expiryDate?.toJavaLocalDate(),
                tenancyStartedBeforeEpcExpiry = filteredJourneyData.getDidTenancyStartBeforeEpcExpiry(),
                epcEnergyRating = epcDetails?.energyRating,
                epcExemptionReason = filteredJourneyData.getEpcExemptionReason(),
                epcMeesExemptionReason = filteredJourneyData.getMeesExemptionReason(),
                hasFireSafetyDeclaration = filteredJourneyData.getHasFireSafetyDeclaration()!!,
            )

        sendConfirmationEmail(propertyCompliance)

        propertyComplianceService.addToPropertiesWithComplianceAddedThisSession(propertyOwnershipId)

        propertyOwnershipService.deleteIncompleteComplianceForm(propertyOwnershipId)

        return CONFIRMATION_PATH_SEGMENT
    }

    private fun getPropertyAddress() =
        propertyOwnershipService
            .getPropertyOwnership(propertyOwnershipId)
            .property.address.singleLineAddress

    private fun sendConfirmationEmail(propertyCompliance: PropertyCompliance) {
        val landlordEmail = propertyCompliance.propertyOwnership.primaryLandlord.email
        val propertyAddress = propertyCompliance.propertyOwnership.property.address.singleLineAddress

        val confirmationMsgKeys = PropertyComplianceConfirmationMessageKeys(propertyCompliance)
        val compliantMsgs = confirmationMsgKeys.compliantMsgKeys.map { messageSource.getMessageForKey(it) }
        val nonCompliantMsgs = confirmationMsgKeys.nonCompliantMsgKeys.map { messageSource.getMessageForKey(it) }

        if (nonCompliantMsgs.isEmpty()) {
            fullPropertyComplianceConfirmationEmailService.sendEmail(
                landlordEmail,
                FullPropertyComplianceConfirmationEmail(
                    propertyAddress,
                    EmailBulletPointList(compliantMsgs),
                    urlProvider.buildLandlordDashboardUri().toString(),
                ),
            )
        } else {
            partialPropertyComplianceConfirmationEmailService.sendEmail(
                landlordEmail,
                PartialPropertyComplianceConfirmationEmail(
                    propertyAddress,
                    EmailBulletPointList(compliantMsgs),
                    EmailBulletPointList(nonCompliantMsgs),
                    urlProvider.buildComplianceInformationUri(propertyOwnershipId).toString(),
                ),
            )
        }
    }

    companion object {
        val initialStepId = PropertyComplianceStepId.GasSafety

        fun getAutomatchedEpc(
            propertyOwnershipId: Long,
            epcLookupService: EpcLookupService,
            propertyOwnershipService: PropertyOwnershipService,
        ): EpcDataModel? {
            val uprn =
                propertyOwnershipService
                    .getPropertyOwnership(propertyOwnershipId)
                    .property.address.uprn
            return uprn?.let { epcLookupService.getEpcByUprn(it) }
        }

        fun getRedirectStepOverrideForCheckMatchedEpcStepHandleSubmitAndRedirect(nextAction: Pair<PropertyComplianceStepId?, Int?>) =
            if (nextAction.first == null) {
                PropertyComplianceStepId.EpcLookup
            } else {
                null
            }

        fun resetCheckMatchedEpcInSessionIfChangedEpcDetails(
            newEpcDetails: EpcDataModel?,
            journeyDataService: JourneyDataService,
        ) {
            val journeyData = journeyDataService.getJourneyDataFromSession()
            if (newEpcDetails != journeyData.getEpcDetails(autoMatched = false)) {
                val newJourneyData = journeyData.withResetCheckMatchedEpc()
                journeyDataService.setJourneyDataInSession(newJourneyData)
            }
        }

        fun updateEpcDetailsInSessionAndReturnUpdatedJourneyData(
            journeyDataService: JourneyDataService,
            filteredJourneyData: JourneyData,
            epcDetails: EpcDataModel?,
            autoMatchedEpc: Boolean,
        ): JourneyData {
            val newFilteredJourneyData = filteredJourneyData.withEpcDetails(epcDetails, autoMatchedEpc)
            journeyDataService.addToJourneyDataIntoSession(newFilteredJourneyData)
            return newFilteredJourneyData
        }

        fun epcLookupStepHandleSubmit(
            filteredJourneyData: JourneyData,
            journeyDataService: JourneyDataService,
            epcLookupService: EpcLookupService,
        ): JourneyData {
            val lookedUpEpc = getLookedUpEpc(filteredJourneyData, journeyDataService, epcLookupService)
            return updateEpcDetailsInSessionAndReturnUpdatedJourneyData(
                journeyDataService,
                filteredJourneyData,
                lookedUpEpc,
                autoMatchedEpc = false,
            )
        }

        private fun getLookedUpEpc(
            filteredJourneyData: JourneyData,
            journeyDataService: JourneyDataService,
            epcLookupService: EpcLookupService,
        ): EpcDataModel? {
            val certificateNumber = filteredJourneyData.getEpcLookupCertificateNumber()!!
            val lookedUpEpc = epcLookupService.getEpcByCertificateNumber(certificateNumber)
            resetCheckMatchedEpcInSessionIfChangedEpcDetails(lookedUpEpc, journeyDataService)
            return lookedUpEpc
        }

        fun epcSupersededStepHandleSubmit(
            filteredJourneyData: JourneyData,
            journeyDataService: JourneyDataService,
            epcLookupService: EpcLookupService,
        ): JourneyData {
            val certificateNumber = filteredJourneyData.getLatestEpcCertificateNumber()!!
            val latestEpc = epcLookupService.getEpcByCertificateNumber(certificateNumber)
            resetCheckMatchedEpcInSessionIfChangedEpcDetails(latestEpc, journeyDataService)
            return updateEpcDetailsInSessionAndReturnUpdatedJourneyData(
                journeyDataService,
                filteredJourneyData,
                latestEpc,
                autoMatchedEpc = false,
            )
        }
    }
}
