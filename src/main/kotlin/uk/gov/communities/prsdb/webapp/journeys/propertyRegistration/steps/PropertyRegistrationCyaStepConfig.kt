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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.CheckableElements
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig2
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasJointLandlordsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentIncludesBillsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService

@JourneyFrameworkComponent
class PropertyRegistrationCyaStepConfig(
    private val localCouncilService: LocalCouncilService,
    private val propertyRegistrationService: PropertyRegistrationService,
    private val licensingHelper: LicensingDetailsHelper,
    private val messageSource: MessageSource,
) : AbstractCheckYourAnswersStepConfig2<CheckableElements, PropertyRegistrationJourneyState>() {
    override fun chooseTemplate(state: PropertyRegistrationJourneyState) = "forms/propertyRegistrationCheckAnswersForm"

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any?> {
        CheckableElements.entries.forEach { checkableElement ->
            state.initialiseCyaChildJourney(childJourneyId(checkableElement), checkableElement)
        }

        return mapOf(
            "title" to "registerProperty.title",
            "submitButtonText" to "forms.buttons.completeRegistration",
            "insetText" to true,
            "propertyName" to state.getAddress().singleLineAddress,
            "propertyDetails" to getPropertyDetailsSummaryList(state),
            "licensingDetails" to
                licensingHelper.getCheckYourAnswersSummaryList(state, state.getCyaJourneyId(CheckableElements.LICENSING)),
            "tenancyDetails" to getTenancyDetailsSummaryList(state),
            "jointLandlordsDetails" to getJointLandLordsSummaryRow(state),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )
    }

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
                jointLandlordEmails = state.invitedJointLandlordEmailsMap?.values?.toList(),
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
            Destination(state.alreadyRegisteredStep)
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
                    Destination.VisitableStep(state.lookupAddressStep, state.getCyaJourneyId(CheckableElements.ADDRESS)),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.localCouncil",
                    localCouncilService.retrieveLocalCouncilById(address.localCouncilId!!).name,
                    Destination.VisitableStep(state.localCouncilStep, state.getCyaJourneyId(CheckableElements.COUNCIL)),
                ),
            )
        }

    private fun getPropertyTypeRow(state: PropertyRegistrationJourneyState): SummaryListRowViewModel {
        val propertyType = state.propertyTypeStep.formModel.propertyType
        val customType = state.propertyTypeStep.formModel.customPropertyType
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.type",
            if (propertyType == PropertyType.OTHER) listOf(propertyType, customType) else propertyType,
            Destination.VisitableStep(state.propertyTypeStep, state.getCyaJourneyId(CheckableElements.PROPERTY_TYPE)),
        )
    }

    private fun getOwnershipTypeRow(state: PropertyRegistrationJourneyState) =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.ownership",
            state.ownershipTypeStep.formModel.ownershipType,
            Destination.VisitableStep(state.ownershipTypeStep, state.getCyaJourneyId(CheckableElements.OWNERSHIP_TYPE)),
        )

    private fun getTenancyDetailsSummaryList(state: PropertyRegistrationJourneyState) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val isOccupied = state.occupied.formModel.occupied ?: false
                add(getOccupancyStatusRow(isOccupied, state))
                if (isOccupied) addAll(getOccupiedTenancyDetailsSummaryList(state))
            }

    private fun getOccupancyStatusRow(
        isOccupied: Boolean,
        state: PropertyRegistrationJourneyState,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.tenancyDetails.occupied",
            isOccupied,
            Destination.VisitableStep(state.occupied, state.getCyaJourneyId(CheckableElements.OCCUPATION)),
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
                val rentIncludesBills = rentIncludesBillsStep.formModel.notNullValue(RentIncludesBillsFormModel::rentIncludesBills)
                val rentFrequency = rentFrequencyStep.formModel.notNullValue(RentFrequencyFormModel::rentFrequency)
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.households",
                        householdsStep.formModel.numberOfHouseholds,
                        Destination.VisitableStep(householdsStep, state.getCyaJourneyId(CheckableElements.HOUSEHOLDS)),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.people",
                        tenantsStep.formModel.numberOfPeople,
                        Destination.VisitableStep(tenantsStep, state.getCyaJourneyId(CheckableElements.TENANTS)),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.bedrooms",
                        bedroomsStep.formModel.numberOfBedrooms,
                        Destination.VisitableStep(bedroomsStep, state.getCyaJourneyId(CheckableElements.RENT_LEVELS)),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.rentIncludesBills",
                        rentIncludesBills,
                        Destination.VisitableStep(rentIncludesBillsStep, state.getCyaJourneyId(CheckableElements.RENT_LEVELS)),
                    ),
                )
                if (rentIncludesBills) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkPropertyAnswers.tenancyDetails.billsIncluded",
                            state.getBillsIncluded(messageSource),
                            Destination.VisitableStep(billsIncludedStep, state.getCyaJourneyId(CheckableElements.RENT_LEVELS)),
                        ),
                    )
                }
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.furnishedStatus",
                        furnishedStatusStep.formModel.furnishedStatus,
                        Destination.VisitableStep(furnishedStatusStep, state.getCyaJourneyId(CheckableElements.RENT_LEVELS)),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.rentFrequency",
                        RentDataHelper.getRentFrequency(rentFrequency, rentFrequencyStep.formModel.customRentFrequency),
                        Destination.VisitableStep(rentFrequencyStep, state.getCyaJourneyId(CheckableElements.RENT_LEVELS)),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.rentAmount",
                        state.getRentAmount(messageSource),
                        Destination.VisitableStep(rentAmountStep, state.getCyaJourneyId(CheckableElements.RENT_LEVELS)),
                    ),
                )
            }

    private fun getJointLandLordsSummaryRow(state: PropertyRegistrationJourneyState): SummaryListRowViewModel {
        val hasJointLandlords = state.hasJointLandlordsStep.formModel.notNullValue(HasJointLandlordsFormModel::hasJointLandlords)
        return if (hasJointLandlords) {
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.jointLandlordsDetails.invitations",
                state.invitedJointLandlords,
                Destination(state.hasJointLandlordsStep),
            )
        } else {
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.jointLandlordsDetails.areThereJointLandlords",
                "forms.checkPropertyAnswers.jointLandlordsDetails.noJointLandlords",
                Destination(state.hasJointLandlordsStep),
            )
        }
    }
}

@JourneyFrameworkComponent
final class PropertyRegistrationCyaStep(
    stepConfig: PropertyRegistrationCyaStepConfig,
) : RequestableStep<Complete, CheckAnswersFormModel, PropertyRegistrationJourneyState>(stepConfig)
