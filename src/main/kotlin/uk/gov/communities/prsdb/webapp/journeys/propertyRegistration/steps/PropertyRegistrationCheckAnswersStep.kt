package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import jakarta.persistence.EntityExistsException
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.example.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService

@JourneyFrameworkComponent
class PropertyRegistrationCyaStepConfig(
    private val localCouncilService: LocalCouncilService,
    private val propertyRegistrationService: PropertyRegistrationService,
    private val licensingHelper: LicensingDetailsHelper,
) : AbstractGenericRequestableStepConfig<Complete, CheckAnswersFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = CheckAnswersFormModel::class

    private lateinit var childJourneyId: String

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any> {
        if (state.cyaChildJourneyId == null) {
            state.initialiseCyaChildJourney()
        }

        childJourneyId = state.cyaChildJourneyId
            ?: throw UnrecoverableJourneyStateException(state.journeyId, "CYA child journey ID should be initialised")

        return mapOf(
            "title" to "registerProperty.title",
            "submitButtonText" to "forms.buttons.completeRegistration",
            "insetText" to true,
            "propertyName" to state.getAddress().singleLineAddress,
            "propertyDetails" to getPropertyDetailsSummaryList(state),
            "licensingDetails" to licensingHelper.getCheckYourAnswersSummaryList(state, childJourneyId),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )
    }

    override fun enrichSubmittedDataBeforeValidation(
        state: PropertyRegistrationJourneyState,
        formData: PageData,
    ): PageData =
        super.enrichSubmittedDataBeforeValidation(state, formData) +
            (CheckAnswersFormModel::storedJourneyData.name to state.getSubmittedStepData())

    override fun afterStepDataIsAdded(state: PropertyRegistrationJourneyState) {
        try {
            propertyRegistrationService.registerProperty(
                addressModel = state.getAddress(),
                propertyType = state.propertyTypeStep.formModel.notNullValue(PropertyTypeFormModel::propertyType),
                licenseType = state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType),
                licenceNumber = state.getLicenceNumberOrNull() ?: "",
                ownershipType = state.ownershipTypeStep.formModel.notNullValue(OwnershipTypeFormModel::ownershipType),
                numberOfHouseholds =
                    state.households.formModelOrNull
                        ?.notNullValue(NumberOfHouseholdsFormModel::numberOfHouseholds)
                        ?.toInt() ?: 0,
                numberOfPeople =
                    state.tenants.formModelOrNull
                        ?.notNullValue(NewNumberOfPeopleFormModel::numberOfPeople)
                        ?.toInt() ?: 0,
                baseUserId = SecurityContextHolder.getContext().authentication.name,
            )
        } catch (_: EntityExistsException) {
            state.isAddressAlreadyRegistered = true
        }
    }

    private fun getPropertyDetailsSummaryList(state: PropertyRegistrationJourneyState): List<SummaryListRowViewModel> =
        getAddressRows(state) +
            getPropertyTypeRow(state) +
            getOwnershipTypeRow(state) +
            getTenancyRows(state)

    private fun getAddressRows(state: PropertyRegistrationJourneyState) =
        state.getAddress().let { address ->
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.address",
                    address.singleLineAddress,
                    Destination.VisitableStep(state.lookupStep, childJourneyId),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.localCouncil",
                    localCouncilService.retrieveLocalCouncilById(address.localCouncilId!!).name,
                    Destination.VisitableStep(state.localCouncilStep, childJourneyId),
                ),
            )
        }

    private fun getPropertyTypeRow(state: PropertyRegistrationJourneyState): SummaryListRowViewModel {
        val propertyType = state.propertyTypeStep.formModel.propertyType
        val customType = state.propertyTypeStep.formModel.customPropertyType
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.type",
            if (propertyType == PropertyType.OTHER) listOf(propertyType, customType) else propertyType,
            Destination.VisitableStep(state.propertyTypeStep, childJourneyId),
        )
    }

    private fun getOwnershipTypeRow(state: PropertyRegistrationJourneyState) =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.ownership",
            state.ownershipTypeStep.formModel.ownershipType,
            Destination.VisitableStep(state.ownershipTypeStep, childJourneyId),
        )

    private fun getTenancyRows(state: PropertyRegistrationJourneyState): List<SummaryListRowViewModel> =
        if (state.occupied.formModel.occupied == true) {
            val householdsStep = state.households
            val tenantsStep = state.tenants
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.occupied",
                    true,
                    Destination.VisitableStep(state.occupied, childJourneyId),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.households",
                    householdsStep.formModel.numberOfHouseholds,
                    Destination(householdsStep),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.people",
                    tenantsStep.formModel.numberOfPeople,
                    Destination(tenantsStep),
                ),
            )
        } else {
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.occupied",
                    false,
                    Destination.VisitableStep(state.occupied, childJourneyId),
                ),
            )
        }

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination =
        if (state.isAddressAlreadyRegistered == true) {
            Destination.VisitableStep(state.alreadyRegisteredStep, childJourneyId)
        } else {
            state.deleteJourney()
            defaultDestination
        }

    override fun chooseTemplate(state: PropertyRegistrationJourneyState): String = "forms/propertyRegistrationCheckAnswersForm"

    override fun mode(state: PropertyRegistrationJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class PropertyRegistrationCheckAnswersStep(
    stepConfig: PropertyRegistrationCyaStepConfig,
) : RequestableStep<Complete, CheckAnswersFormModel, PropertyRegistrationJourneyState>(stepConfig)
