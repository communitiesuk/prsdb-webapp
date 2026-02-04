package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import jakarta.persistence.EntityExistsException
import org.springframework.context.MessageSource
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.helpers.RentDataHelper
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
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
    private val messageSource: MessageSource,
) : AbstractCheckYourAnswersStepConfig<PropertyRegistrationJourneyState>() {
    override fun chooseTemplate(state: PropertyRegistrationJourneyState) = "forms/propertyRegistrationCheckAnswersForm"

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState) =
        mapOf(
            "title" to "registerProperty.title",
            "submitButtonText" to "forms.buttons.completeRegistration",
            "insetText" to true,
            "propertyName" to state.getAddress().singleLineAddress,
            "propertyDetails" to getPropertyDetailsSummaryList(state),
            "licensingDetails" to licensingHelper.getCheckYourAnswersSummaryList(state, childJourneyId),
            "tenancyDetails" to getTenancyDetailsSummaryList(state),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )

    override fun afterStepDataIsAdded(state: PropertyRegistrationJourneyState) {
        try {
            val isOccupied = state.occupied.formModel.notNullValue(OccupancyFormModel::occupied)
            val billsIncludedDataModel = state.getBillsIncludedOrNull()
            propertyRegistrationService.registerProperty(
                addressModel = state.getAddress(),
                propertyType = state.propertyTypeStep.formModel.notNullValue(PropertyTypeFormModel::propertyType),
                licenseType = state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType),
                licenceNumber = state.getLicenceNumberOrNull() ?: "",
                ownershipType = state.ownershipTypeStep.formModel.notNullValue(OwnershipTypeFormModel::ownershipType),
                numberOfHouseholds =
                    if (isOccupied) {
                        state.households.formModel
                            .notNullValue(NumberOfHouseholdsFormModel::numberOfHouseholds)
                            .toInt()
                    } else {
                        0
                    },
                numberOfPeople =
                    if (isOccupied) {
                        state.tenants.formModel
                            .notNullValue(NewNumberOfPeopleFormModel::numberOfPeople)
                            .toInt()
                    } else {
                        0
                    },
                numBedrooms =
                    if (isOccupied) {
                        state.bedrooms.formModel
                            .notNullValue(NumberOfBedroomsFormModel::numberOfBedrooms)
                            .toInt()
                    } else {
                        null
                    },
                billsIncludedList = if (isOccupied) billsIncludedDataModel?.standardBillsIncludedString else null,
                customBillsIncluded = if (isOccupied) billsIncludedDataModel?.customBillsIncluded else null,
                furnishedStatus = if (isOccupied) state.furnishedStatus.formModel.furnishedStatus else null,
                rentFrequency = if (isOccupied) state.rentFrequency.formModel.rentFrequency else null,
                customRentFrequency = if (isOccupied) state.getCustomRentFrequencyIfSelected() else null,
                rentAmount =
                    if (isOccupied) {
                        state.rentAmount.formModel.rentAmount
                            .toBigDecimal()
                    } else {
                        null
                    },
                baseUserId = SecurityContextHolder.getContext().authentication.name,
            )
        } catch (_: EntityExistsException) {
            state.isAddressAlreadyRegistered = true
        }
    }

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination =
        if (state.isAddressAlreadyRegistered == true) {
            Destination.VisitableStep(state.alreadyRegisteredStep, childJourneyId)
        } else {
            super.resolveNextDestination(state, defaultDestination)
        }

    private fun getPropertyDetailsSummaryList(state: PropertyRegistrationJourneyState) =
        getAddressRows(state) +
            getPropertyTypeRow(state) +
            getOwnershipTypeRow(state)

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

    private fun getTenancyDetailsSummaryList(state: PropertyRegistrationJourneyState) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val isOccupied = state.occupied.formModel.occupied ?: false
                add(getOccupancyStatusRow(isOccupied, state.occupied))
                if (isOccupied) addAll(getOccupiedTenancyDetailsSummaryList(state))
            }

    private fun getOccupancyStatusRow(
        isOccupied: Boolean,
        occupiedStep: RequestableStep<*, *, *>,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.tenancyDetails.occupied",
            isOccupied,
            Destination.VisitableStep(occupiedStep, childJourneyId),
        )

    private fun getOccupiedTenancyDetailsSummaryList(state: PropertyRegistrationJourneyState) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val householdsStep = state.households
                val tenantsStep = state.tenants
                val bedroomsStep = state.bedrooms
                val rentIncludesBillsStep = state.rentIncludesBills
                val billsIncludedStep = state.billsIncluded
                val furnishedStatusStep = state.furnishedStatus
                val rentFrequencyStep = state.rentFrequency
                val rentAmountStep = state.rentAmount
                val rentIncludesBills = rentIncludesBillsStep.formModel.rentIncludesBills!!
                val rentFrequency = rentFrequencyStep.formModel.rentFrequency!!
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.households",
                        householdsStep.formModel.numberOfHouseholds,
                        Destination(householdsStep),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.people",
                        tenantsStep.formModel.numberOfPeople,
                        Destination(tenantsStep),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.bedrooms",
                        bedroomsStep.formModel.numberOfBedrooms,
                        Destination(bedroomsStep),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.rentIncludesBills",
                        rentIncludesBills,
                        Destination(rentIncludesBillsStep),
                    ),
                )
                if (rentIncludesBills) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkPropertyAnswers.tenancyDetails.billsIncluded",
                            state.getBillsIncluded(messageSource),
                            Destination(billsIncludedStep),
                        ),
                    )
                }
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.furnishedStatus",
                        furnishedStatusStep.formModel.furnishedStatus,
                        Destination(furnishedStatusStep),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.rentFrequency",
                        RentDataHelper.getRentFrequency(rentFrequency, rentFrequencyStep.formModel.customRentFrequency),
                        Destination(rentFrequencyStep),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.rentAmount",
                        state.getRentAmount(messageSource),
                        Destination(rentAmountStep),
                    ),
                )
            }
}

@JourneyFrameworkComponent
final class PropertyRegistrationCyaStep(
    stepConfig: PropertyRegistrationCyaStepConfig,
) : AbstractCheckYourAnswersStep<PropertyRegistrationJourneyState>(stepConfig)
