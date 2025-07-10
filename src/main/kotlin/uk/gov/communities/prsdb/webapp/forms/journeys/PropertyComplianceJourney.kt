package uk.gov.communities.prsdb.webapp.forms.journeys

import kotlinx.datetime.toJavaLocalDate
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONTACT_EPC_ASSESSOR_URL
import uk.gov.communities.prsdb.webapp.constants.EPC_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.EPC_IMPROVEMENT_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.FIND_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.GOVERNMENT_APPROVED_DEPOSIT_PROTECTION_SCHEME_URL
import uk.gov.communities.prsdb.webapp.constants.HOMES_ACT_2018_URL
import uk.gov.communities.prsdb.webapp.constants.HOUSES_IN_MULTIPLE_OCCUPATION_URL
import uk.gov.communities.prsdb.webapp.constants.HOUSING_HEALTH_AND_SAFETY_RATING_SYSTEM_URL
import uk.gov.communities.prsdb.webapp.constants.HOW_TO_RENT_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.MEES_EXEMPTION_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.PRIVATE_RENTING_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_INFO_URL
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_REGISTER_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PRS_EXEMPTION_URL
import uk.gov.communities.prsdb.webapp.constants.RIGHT_TO_RENT_CHECKS_URL
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.FileUploadPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PageWithContentProvider
import uk.gov.communities.prsdb.webapp.forms.pages.PropertyComplianceCheckAnswersPage
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceSharedSteps
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.helpers.JourneyContextHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.GroupedJourneyExtensions.Companion.withBackUrlIfNotNullAndNotCheckingAnswers
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAcceptedEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAutoMatchedEpcIsCorrect
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
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEicrExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasFireSafetyDeclaration
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrExemptionReasonOther
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getLatestEpcCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMatchedEpcIsCorrect
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMeesExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getPropertyHasMeesExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.withEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.withResetCheckMatchedEpc
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
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.KeepPropertySafeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ResponsibilityToTenantsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
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

    private val isCheckingAnswers = JourneyContextHelper.isCheckingAnswers(checkingAnswersForStep)
    private val checkingAnswersFor = PropertyComplianceStepId.entries.find { it.urlPathSegment == checkingAnswersForStep }

    override val stepRouter = GroupedStepRouter(this)
    override val checkYourAnswersStepId = PropertyComplianceStepId.CheckAndSubmit
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
                    PropertyComplianceSharedSteps.gasSafetyIssueDateStep,
                    PropertyComplianceSharedSteps.gasSafetyEngineerNumStep,
                    PropertyComplianceSharedSteps.gasSafetyUploadStep,
                    PropertyComplianceSharedSteps.gasSafetyUploadConfirmationStep(eicrTask.startingStepId, isCheckingAnswers),
                    PropertyComplianceSharedSteps.gasSafetyOutdatedStep(eicrTask.startingStepId, isCheckingAnswers),
                    PropertyComplianceSharedSteps.gasSafetyExemptionStep,
                    PropertyComplianceSharedSteps.gasSafetyExemptionReasonStep,
                    PropertyComplianceSharedSteps.gasSafetyExemptionOtherReasonStep,
                    PropertyComplianceSharedSteps.gasSafetyExemptionConfirmationStep(eicrTask.startingStepId, isCheckingAnswers),
                    PropertyComplianceSharedSteps.gasSafetyExemptionMissingStep(eicrTask.startingStepId, isCheckingAnswers),
                ),
                "propertyCompliance.taskList.upload.gasSafety",
            )

    private val eicrTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.EICR,
                setOf(
                    eicrStep,
                    eicrIssueDateStep,
                    eicrUploadStep,
                    eicrUploadConfirmationStep,
                    eicrOutdatedStep,
                    eicrExemptionStep,
                    eicrExemptionReasonStep,
                    eicrExemptionOtherReasonStep,
                    eicrExemptionConfirmationStep,
                    eicrExemptionMissingStep,
                ),
                "propertyCompliance.taskList.upload.eicr",
            )

    private val epcTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.EPC,
                setOf(
                    epcStep,
                    epcNotAutomatchedStep,
                    checkAutoMatchedEpcStep,
                    epcLookupStep,
                    checkMatchedEpcStep,
                    epcNotFoundStep,
                    epcSupersededStep,
                    epcExpiryCheckStep,
                    epcExpiredStep,
                    epcMissingStep,
                    epcExemptionReasonStep,
                    epcExemptionConfirmationStep,
                    meesExemptionCheckStep,
                    meesExemptionReasonStep,
                    meesExemptionConfirmationStep,
                    lowEnergyRatingStep,
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

    private val eicrIssueDateStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrIssueDate,
                page =
                    Page(
                        formModel = TodayOrPastDateFormModel::class,
                        templateName = "forms/dateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.todayOrPastDate.eicr.fieldSetHeading",
                                "fieldSetHint" to "forms.todayOrPastDate.eicr.fieldSetHint",
                                "submitButtonText" to "forms.buttons.saveAndContinue",
                            ),
                    ),
                nextAction = { filteredJourneyData, _ -> eicrIssueDateStepNextAction(filteredJourneyData) },
            )

    private val eicrUploadStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrUpload,
                page =
                    FileUploadPage(
                        formModel = EicrUploadCertificateFormModel::class,
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.uploadCertificate.eicr.fieldSetHeading",
                                "fieldSetHint" to "forms.uploadCertificate.fieldSetHint",
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.EicrUploadConfirmation, null) },
            )

    private val eicrUploadConfirmationStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrUploadConfirmation,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/uploadCertificateConfirmationForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "submitButtonText" to getSubmitButtonTextOrDefaultIfCheckingAnswers("forms.buttons.saveAndContinueToEPC"),
                            ),
                    ),
                nextAction = { _, _ -> Pair(epcTask.startingStepId, null) },
            )

    private val eicrOutdatedStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrOutdated,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/eicrOutdatedForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "rcpElectricalInfoUrl" to RCP_ELECTRICAL_INFO_URL,
                                "rcpElectricalRegisterUrl" to RCP_ELECTRICAL_REGISTER_URL,
                                "submitButtonText" to getSubmitButtonTextOrDefaultIfCheckingAnswers("forms.buttons.saveAndContinueToEPC"),
                            ),
                    ),
                nextAction = { _, _ -> Pair(epcTask.startingStepId, null) },
            )

    private val eicrExemptionStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrExemption,
                page =
                    Page(
                        formModel = EicrExemptionFormModel::class,
                        templateName = "forms/exemptionForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.eicrExemption.fieldSetHeading",
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
                            ),
                    ),
                nextAction = { filteredJourneyData, _ -> eicrExemptionStepNextAction(filteredJourneyData) },
            )

    private val eicrExemptionReasonStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrExemptionReason,
                page =
                    Page(
                        formModel = EicrExemptionReasonFormModel::class,
                        templateName = "forms/exemptionReasonForm.html",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.eicrExemptionReason.fieldSetHeading",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = GasSafetyExemptionReason.LONG_LEASE,
                                            labelMsgKey = "forms.eicrExemptionReason.radios.longLease.label",
                                            hintMsgKey = "forms.eicrExemptionReason.radios.longLease.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EicrExemptionReason.STUDENT_ACCOMMODATION,
                                            labelMsgKey = "forms.eicrExemptionReason.radios.studentAccommodation.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EicrExemptionReason.LIVE_IN_LANDLORD,
                                            labelMsgKey = "forms.eicrExemptionReason.radios.liveInLandlord.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = GasSafetyExemptionReason.OTHER,
                                            labelMsgKey = "forms.eicrExemptionReason.radios.other.label",
                                            hintMsgKey = "forms.eicrExemptionReason.radios.other.hint",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { filteredJourneyData, _ -> eicrExemptionReasonStepNextAction(filteredJourneyData) },
            )

    private val eicrExemptionOtherReasonStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrExemptionOtherReason,
                page =
                    Page(
                        formModel = EicrExemptionOtherReasonFormModel::class,
                        templateName = "forms/exemptionOtherReasonForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.eicrExemptionOtherReason.fieldSetHeading",
                                "fieldSetHint" to "forms.eicrExemptionOtherReason.fieldSetHint",
                                "limit" to EXEMPTION_OTHER_REASON_MAX_LENGTH,
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.EicrExemptionConfirmation, null) },
            )

    private val eicrExemptionConfirmationStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrExemptionConfirmation,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/eicrExemptionConfirmationForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "submitButtonText" to getSubmitButtonTextOrDefaultIfCheckingAnswers("forms.buttons.saveAndContinueToEPC"),
                            ),
                    ),
                nextAction = { _, _ -> Pair(epcTask.startingStepId, null) },
            )

    private val eicrExemptionMissingStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrExemptionMissing,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/eicrExemptionMissingForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "rcpElectricalInfoUrl" to RCP_ELECTRICAL_INFO_URL,
                                "rcpElectricalRegisterUrl" to RCP_ELECTRICAL_REGISTER_URL,
                                "submitButtonText" to getSubmitButtonTextOrDefaultIfCheckingAnswers("forms.buttons.saveAndContinueToEPC"),
                            ),
                    ),
                nextAction = { _, _ -> Pair(epcTask.startingStepId, null) },
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

    private val epcMissingStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcMissing,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/epcMissingForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "findEpcUrl" to FIND_EPC_URL,
                                "getNewEpcUrl" to GET_NEW_EPC_URL,
                                "submitButtonText" to
                                    getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                        "forms.buttons.saveAndContinueToLandlordResponsibilities",
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(landlordResponsibilities.first().startingStepId, null) },
            )

    private val epcNotAutomatchedStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcNotAutoMatched,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/epcNotAutoMatchedForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.EpcLookup, null) },
            )

    private val checkAutoMatchedEpcStep
        get() =
            Step(
                id = PropertyComplianceStepId.CheckAutoMatchedEpc,
                page = getCheckMatchedEpcPage(autoMatchedEpc = true),
                nextAction = { filteredJourneyData, _ -> checkAutoMatchedEpcStepNextAction(filteredJourneyData) },
            )

    private val checkMatchedEpcStep
        get() =
            Step(
                id = PropertyComplianceStepId.CheckMatchedEpc,
                page = getCheckMatchedEpcPage(autoMatchedEpc = false),
                nextAction = { filteredJourneyData, _ -> checkMatchedEpcStepNextAction(filteredJourneyData) },
                handleSubmitAndRedirect = { filteredJourneyData, _, _ ->
                    checkMatchedEpcStepHandleSubmitAndRedirect(filteredJourneyData)
                },
            )

    private fun getCheckMatchedEpcPage(autoMatchedEpc: Boolean): Page {
        val epcDetails = getEpcDetailsFromSession(autoMatched = autoMatchedEpc)
        return Page(
            formModel = CheckMatchedEpcFormModel::class,
            templateName = "forms/checkMatchedEpcForm",
            content =
                mapOf(
                    "title" to "propertyCompliance.title",
                    "fieldSetHeading" to "forms.checkMatchedEpc.fieldSetHeading",
                    "epcDetails" to (epcDetails ?: ""),
                    "epcCertificateUrl" to epcCertificateUrlProvider.getEpcCertificateUrl(epcDetails?.certificateNumber ?: ""),
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
                                labelMsgKey = "forms.checkMatchedEpc.radios.no.label",
                            ),
                        ),
                ),
        )
    }

    private val epcSupersededStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcSuperseded,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/epcSupersededForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "certificateNumber" to getLatestEpcCertificateNumberFromSession(),
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.CheckMatchedEpc, null) },
                handleSubmitAndRedirect = { filteredJourneyData, _, _ -> epcSupersededHandleSubmitAndRedirect(filteredJourneyData) },
            )

    private val epcExemptionReasonStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcExemptionReason,
                page =
                    Page(
                        formModel = EpcExemptionReasonFormModel::class,
                        templateName = "forms/epcExemptionReasonForm.html",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.epcExemptionReason.fieldSetHeading",
                                "epcGuideUrl" to EPC_GUIDE_URL,
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.LISTED_BUILDING,
                                            labelMsgKey = "forms.epcExemptionReason.radios.listedBuilding.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.ANNUAL_USE_LESS_THAN_4_MONTHS,
                                            labelMsgKey = "forms.epcExemptionReason.radios.annualUseLessThan4Months.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.ANNUAL_ENERGY_CONSUMPTION_LESS_THAN_25_PERCENT,
                                            labelMsgKey = "forms.epcExemptionReason.radios.annualEnergyConsumptionLessThan25Percent.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.TEMPORARY_BUILDING,
                                            labelMsgKey = "forms.epcExemptionReason.radios.temporaryBuilding.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.STANDALONE_SMALL_BUILDING,
                                            labelMsgKey = "forms.epcExemptionReason.radios.standaloneSmallBuilding.label",
                                            hintMsgKey = "forms.epcExemptionReason.radios.standaloneSmallBuilding.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.DUE_FOR_DEMOLITION,
                                            labelMsgKey = "forms.epcExemptionReason.radios.dueForDemolition.label",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.EpcExemptionConfirmation, null) },
            )

    private val epcExemptionConfirmationStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcExemptionConfirmation,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/epcExemptionConfirmationForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "submitButtonText" to
                                    getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                        "forms.buttons.saveAndContinueToLandlordResponsibilities",
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(landlordResponsibilities.first().startingStepId, null) },
            )

    private val epcLookupStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcLookup,
                page =
                    Page(
                        formModel = EpcLookupFormModel::class,
                        templateName = "forms/epcLookupForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.epcLookup.fieldSetHeading",
                                "fieldSetHint" to "forms.epcLookup.fieldSetHint",
                                "findEpcUrl" to FIND_EPC_URL,
                                "getNewEpcUrl" to GET_NEW_EPC_URL,
                            ),
                    ),
                nextAction = { filteredJourneyData, _ -> epcLookupStepNextAction(filteredJourneyData) },
                handleSubmitAndRedirect = { filteredJourneyData, _, _ -> epcLookupStepHandleSubmitAndRedirect(filteredJourneyData) },
            )

    private val epcNotFoundStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcNotFound,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/epcNotFoundForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "contactAssessorUrl" to CONTACT_EPC_ASSESSOR_URL,
                                "getNewEpcUrl" to GET_NEW_EPC_URL,
                                "searchAgainUrl" to PropertyComplianceStepId.EpcLookup.urlPathSegment,
                                "certificateNumber" to getEpcLookupCertificateNumberFromSession(),
                            ),
                    ),
                nextAction = { _, _ -> Pair(fireSafetyDeclarationStep.id, null) },
            )

    private val epcExpiryCheckStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcExpiryCheck,
                page =
                    Page(
                        formModel = EpcExpiryCheckFormModel::class,
                        templateName = "forms/epcExpiryCheckForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.epcExpiryCheck.fieldSetHeading",
                                "expiryDate" to (getAcceptedEpcDetailsFromSession()?.expiryDateAsJavaLocalDate ?: ""),
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
                            ),
                    ),
                nextAction = { filteredJourneyData, _ -> epcExpiryCheckStepNextAction(filteredJourneyData) },
            )

    private val epcExpiredStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcExpired,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = getEpcExpiredTemplate(),
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "getNewEpcUrl" to GET_NEW_EPC_URL,
                                "meesExemptionGuideUrl" to MEES_EXEMPTION_GUIDE_URL,
                                "registerMeesExemptionUrl" to REGISTER_PRS_EXEMPTION_URL,
                                "epcImprovementGuideUrl" to EPC_IMPROVEMENT_GUIDE_URL,
                                "expiryDateAsJavaLocalDate" to (getAcceptedEpcDetailsFromSession()?.expiryDateAsJavaLocalDate ?: ""),
                                "submitButtonText" to
                                    getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                        "forms.buttons.saveAndContinueToLandlordResponsibilities",
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(landlordResponsibilities.first().startingStepId, null) },
            )

    private val meesExemptionCheckStep
        get() =
            Step(
                id = PropertyComplianceStepId.MeesExemptionCheck,
                page =
                    Page(
                        formModel = MeesExemptionCheckFormModel::class,
                        templateName = "forms/meesExemptionCheckForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
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
                                "meesExemptionGuideUrl" to MEES_EXEMPTION_GUIDE_URL,
                                "singleLineAddress" to (getAcceptedEpcDetailsFromSession()?.singleLineAddress ?: ""),
                            ),
                    ),
                nextAction = { filteredJourneyData, _ -> meesExemptionCheckStepNextAction(filteredJourneyData) },
            )

    private val meesExemptionReasonStep
        get() =
            Step(
                id = PropertyComplianceStepId.MeesExemptionReason,
                page =
                    Page(
                        formModel = MeesExemptionReasonFormModel::class,
                        templateName = "forms/exemptionReasonForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.meesExemptionReason.fieldSetHeading",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = MeesExemptionReason.LISTED_BUILDING,
                                            labelMsgKey = "forms.meesExemptionReason.radios.listedBuilding.label",
                                            hintMsgKey = "forms.meesExemptionReason.radios.listedBuilding.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = MeesExemptionReason.SMALL_DETACHED_BUILDING,
                                            labelMsgKey = "forms.meesExemptionReason.radios.smallDetachedBuilding.label",
                                            hintMsgKey = "forms.meesExemptionReason.radios.smallDetachedBuilding.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = MeesExemptionReason.HIGH_COST,
                                            labelMsgKey = "forms.meesExemptionReason.radios.highCost.label",
                                            hintMsgKey = "forms.meesExemptionReason.radios.highCost.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = MeesExemptionReason.ALL_IMPROVEMENTS_MADE,
                                            labelMsgKey = "forms.meesExemptionReason.radios.allImprovementsMade.label",
                                            hintMsgKey = "forms.meesExemptionReason.radios.allImprovementsMade.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = MeesExemptionReason.WALL_INSULATION,
                                            labelMsgKey = "forms.meesExemptionReason.radios.wallInsulation.label",
                                            hintMsgKey = "forms.meesExemptionReason.radios.wallInsulation.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = MeesExemptionReason.THIRD_PARTY_CONSENT,
                                            labelMsgKey = "forms.meesExemptionReason.radios.thirdPartyConsent.label",
                                            hintMsgKey = "forms.meesExemptionReason.radios.thirdPartyConsent.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = MeesExemptionReason.PROPERTY_DEVALUATION,
                                            labelMsgKey = "forms.meesExemptionReason.radios.propertyDevaluation.label",
                                            hintMsgKey = "forms.meesExemptionReason.radios.propertyDevaluation.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = MeesExemptionReason.NEW_LANDLORD,
                                            labelMsgKey = "forms.meesExemptionReason.radios.newLandlord.label",
                                            hintMsgKey = "forms.meesExemptionReason.radios.newLandlord.hint",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.MeesExemptionConfirmation, null) },
            )

    private val meesExemptionConfirmationStep
        get() =
            Step(
                id = PropertyComplianceStepId.MeesExemptionConfirmation,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/meesExemptionConfirmationForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "submitButtonText" to
                                    getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                        "forms.buttons.saveAndContinueToLandlordResponsibilities",
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(landlordResponsibilities.first().startingStepId, null) },
            )

    private val lowEnergyRatingStep
        get() =
            Step(
                id = PropertyComplianceStepId.LowEnergyRating,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/lowEnergyRatingForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "epcImprovementGuideUrl" to EPC_IMPROVEMENT_GUIDE_URL,
                                "registerPrsExemptionUrl" to REGISTER_PRS_EXEMPTION_URL,
                                "submitButtonText" to
                                    getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                        "forms.buttons.saveAndContinueToLandlordResponsibilities",
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(landlordResponsibilities.first().startingStepId, null) },
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
                page = PropertyComplianceCheckAnswersPage(journeyDataService, epcCertificateUrlProvider) { getPropertyAddress() },
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

    private fun eicrIssueDateStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getIsEicrOutdated()!!) {
            Pair(PropertyComplianceStepId.EicrOutdated, null)
        } else {
            Pair(PropertyComplianceStepId.EicrUpload, null)
        }

    private fun eicrExemptionStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getHasEicrExemption()!!) {
            Pair(PropertyComplianceStepId.EicrExemptionReason, null)
        } else {
            Pair(PropertyComplianceStepId.EicrExemptionMissing, null)
        }

    private fun eicrExemptionReasonStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getIsEicrExemptionReasonOther()!!) {
            Pair(PropertyComplianceStepId.EicrExemptionOtherReason, null)
        } else {
            Pair(PropertyComplianceStepId.EicrExemptionConfirmation, null)
        }

    private fun epcStepHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        if (filteredJourneyData.getHasEPC() == HasEpc.YES) {
            val uprn =
                propertyOwnershipService
                    .getPropertyOwnership(propertyOwnershipId)
                    .property.address.uprn

            val epcDetails = uprn?.let { epcLookupService.getEpcByUprn(it) }

            return updateEpcDetailsInSessionAndRedirectToNextStep(epcStep, filteredJourneyData, epcDetails, autoMatchedEpc = true)
        }

        return getRedirectForNextStep(epcStep, filteredJourneyData, null, checkingAnswersFor)
    }

    private fun updateEpcDetailsInSessionAndRedirectToNextStep(
        currentStep: Step<PropertyComplianceStepId>,
        filteredJourneyData: JourneyData,
        epcDetails: EpcDataModel?,
        autoMatchedEpc: Boolean,
    ): String {
        val newFilteredJourneyData = filteredJourneyData.withEpcDetails(epcDetails, autoMatchedEpc)
        journeyDataService.addToJourneyDataIntoSession(newFilteredJourneyData)
        return getRedirectForNextStep(currentStep, newFilteredJourneyData, null, checkingAnswersFor)
    }

    private fun resetCheckMatchedEpcInSessionIfChangedEpcDetails(newEpcDetails: EpcDataModel?) {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        if (newEpcDetails != journeyData.getEpcDetails(autoMatched = false)) {
            val newJourneyData = journeyData.withResetCheckMatchedEpc()
            journeyDataService.setJourneyDataInSession(newJourneyData)
        }
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

    private fun checkAutoMatchedEpcStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId?, Int?> =
        if (filteredJourneyData.getAutoMatchedEpcIsCorrect()!!) {
            matchedEpcIsCorrectNextAction(filteredJourneyData, autoMatched = true)
        } else {
            Pair(PropertyComplianceStepId.EpcLookup, null)
        }

    private fun checkMatchedEpcStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId?, Int?> =
        if (filteredJourneyData.getMatchedEpcIsCorrect()!!) {
            matchedEpcIsCorrectNextAction(filteredJourneyData, autoMatched = false)
        } else {
            // The user will be redirected to the lookup step in handleSubmitAndRedirect
            // When they are redirected, the nextAction of lookupStep is this step (checkMatchedEpc)
            // Here we set checkMatchedEpc's nextAction to null to avoid an infinite loop of previous steps when checking if a step is reachable
            Pair(null, null)
        }

    private fun matchedEpcIsCorrectNextAction(
        filteredJourneyData: JourneyData,
        autoMatched: Boolean,
    ): Pair<PropertyComplianceStepId?, Int?> {
        val epcDetails = filteredJourneyData.getEpcDetails(autoMatched)!!
        if (epcDetails.isPastExpiryDate()) {
            return Pair(PropertyComplianceStepId.EpcExpiryCheck, null)
        }
        if (!epcDetails.isEnergyRatingEOrBetter()) {
            return Pair(PropertyComplianceStepId.MeesExemptionCheck, null)
        }
        return Pair(landlordResponsibilities.first().startingStepId, null)
    }

    private fun checkMatchedEpcStepHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val nextAction = checkMatchedEpcStepNextAction(filteredJourneyData)
        if (nextAction.first == null) {
            return getRedirectForNextStep(
                checkMatchedEpcStep,
                filteredJourneyData,
                null,
                checkingAnswersFor,
                PropertyComplianceStepId.EpcLookup,
            )
        }
        return getRedirectForNextStep(checkMatchedEpcStep, filteredJourneyData, null, checkingAnswersFor)
    }

    private fun epcLookupStepHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val certificateNumber = filteredJourneyData.getEpcLookupCertificateNumber()!!
        val lookedUpEpc = epcLookupService.getEpcByCertificateNumber(certificateNumber)
        resetCheckMatchedEpcInSessionIfChangedEpcDetails(lookedUpEpc)
        return updateEpcDetailsInSessionAndRedirectToNextStep(epcLookupStep, filteredJourneyData, lookedUpEpc, autoMatchedEpc = false)
    }

    private fun epcLookupStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId?, Int?> {
        val lookedUpEpcDetails =
            filteredJourneyData.getEpcDetails(autoMatched = false)
                ?: return Pair(PropertyComplianceStepId.EpcNotFound, null)
        return if (lookedUpEpcDetails.isLatestCertificateForThisProperty()) {
            Pair(PropertyComplianceStepId.CheckMatchedEpc, null)
        } else {
            Pair(PropertyComplianceStepId.EpcSuperseded, null)
        }
    }

    private fun epcSupersededHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val certificateNumber = filteredJourneyData.getLatestEpcCertificateNumber()!!
        val latestEpc = epcLookupService.getEpcByCertificateNumber(certificateNumber)
        resetCheckMatchedEpcInSessionIfChangedEpcDetails(latestEpc)
        return updateEpcDetailsInSessionAndRedirectToNextStep(epcLookupStep, filteredJourneyData, latestEpc, autoMatchedEpc = false)
    }

    private fun epcExpiryCheckStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId?, Int?> =
        if (filteredJourneyData.getDidTenancyStartBeforeEpcExpiry() == true) {
            if (filteredJourneyData.getAcceptedEpcDetails()?.isEnergyRatingEOrBetter() == true) {
                Pair(landlordResponsibilities.first().startingStepId, null)
            } else {
                Pair(PropertyComplianceStepId.MeesExemptionCheck, null)
            }
        } else {
            Pair(PropertyComplianceStepId.EpcExpired, null)
        }

    private fun meesExemptionCheckStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId?, Int?> =
        if (filteredJourneyData.getPropertyHasMeesExemption()!!) {
            Pair(PropertyComplianceStepId.MeesExemptionReason, null)
        } else {
            Pair(PropertyComplianceStepId.LowEnergyRating, null)
        }

    private fun getEpcExpiredTemplate(): String {
        val epcDetails = getAcceptedEpcDetailsFromSession() ?: return ""
        return if (epcDetails.isEnergyRatingEOrBetter()) {
            "forms/epcExpiredForm"
        } else {
            "forms/epcExpiredLowRatingForm"
        }
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

    private fun getEpcLookupCertificateNumberFromSession(): String {
        val submittedCertificateNumber =
            journeyDataService
                .getJourneyDataFromSession()
                .getEpcLookupCertificateNumber()
                ?: return ""
        return EpcDataModel.parseCertificateNumberOrNull(submittedCertificateNumber)!! // Only valid EPC numbers will be in journeyData
    }

    private fun getEpcDetailsFromSession(autoMatched: Boolean): EpcDataModel? =
        journeyDataService
            .getJourneyDataFromSession()
            .getEpcDetails(autoMatched)

    private fun getLatestEpcCertificateNumberFromSession(): String {
        return journeyDataService.getJourneyDataFromSession().getLatestEpcCertificateNumber()
            ?: return ""
    }

    private fun getAcceptedEpcDetailsFromSession(): EpcDataModel? =
        journeyDataService
            .getJourneyDataFromSession()
            .getAcceptedEpcDetails()

    private fun getPropertyAddress() =
        propertyOwnershipService
            .getPropertyOwnership(propertyOwnershipId)
            .property.address.singleLineAddress

    private fun getSubmitButtonTextOrDefaultIfCheckingAnswers(submitButtonText: String) =
        if (isCheckingAnswers) {
            "forms.buttons.saveAndContinue"
        } else {
            submitButtonText
        }

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
    }
}
