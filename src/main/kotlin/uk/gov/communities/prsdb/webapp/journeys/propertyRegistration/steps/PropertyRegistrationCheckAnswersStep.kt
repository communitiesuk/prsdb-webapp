package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.example.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService

@Scope("prototype")
@PrsdbWebComponent
class PropertyRegistrationCyaStepConfig(
    private val localAuthorityService: LocalAuthorityService,
) : AbstractGenericStepConfig<Complete, NoInputFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState) =
        mapOf(
            "title" to "registerProperty.title",
            "submitButtonText" to "forms.buttons.completeRegistration",
            "insetText" to true,
            "propertyName" to state.getAddress().singleLineAddress,
            "propertyDetails" to getPropertyDetailsSummaryList(state),
            "licensingDetails" to getLicensingDetailsSummaryList(state),
        )

    private fun getPropertyDetailsSummaryList(state: PropertyRegistrationJourneyState): List<SummaryListRowViewModel> =
        getAddressRows(state) +
            getPropertyTypeRow(state) +
            getOwnershipTypeRow(state) +
            getTenancyRows(state)

    private fun getAddressRows(state: AddressState) =
        state.getAddress().let { address ->
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.address",
                    address.singleLineAddress,
                    JourneyStateService.urlToStep(state.lookupStep),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                    localAuthorityService.retrieveLocalAuthorityById(address.localAuthorityId!!).name,
                    JourneyStateService.urlToStepIfReachable(state.localAuthorityStep),
                ),
            )
        }

    private fun getPropertyTypeRow(state: PropertyRegistrationJourneyState): SummaryListRowViewModel {
        val propertyType = state.propertyTypeStep.formModel.propertyType
        val customType = state.propertyTypeStep.formModel.customPropertyType
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.type",
            if (propertyType == PropertyType.OTHER) listOf(propertyType, customType) else propertyType,
            JourneyStateService.urlToStep(state.propertyTypeStep),
        )
    }

    private fun getOwnershipTypeRow(state: PropertyRegistrationJourneyState) =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.ownership",
            state.ownershipTypeStep.formModel.ownershipType,
            JourneyStateService.urlToStep(state.ownershipTypeStep),
        )

    private fun getTenancyRows(state: OccupiedJourneyState): List<SummaryListRowViewModel> =
        if (state.occupied.formModel.occupied == true) {
            val householdsStep = state.households
            val tenantsStep = state.tenants
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.occupied",
                    true,
                    JourneyStateService.urlToStep(state.occupied),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.households",
                    householdsStep.formModel.numberOfHouseholds,
                    JourneyStateService.urlToStep(householdsStep),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.people",
                    tenantsStep.formModel.numberOfPeople,
                    JourneyStateService.urlToStep(tenantsStep),
                ),
            )
        } else {
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.occupied",
                    false,
                    JourneyStateService.urlToStep(state.occupied),
                ),
            )
        }

    private fun getLicensingDetailsSummaryList(state: PropertyRegistrationJourneyState): List<SummaryListRowViewModel> =
        listOfNotNull(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.propertyDetails.licensingType",
                state.licensingTypeStep.formModel.licensingType,
                JourneyStateService.urlToStep(state.licensingTypeStep),
            ),
            state.getLicenceNumberOrNull()?.let { licenceNumber ->
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
                    licenceNumber,
                    when (state.licensingTypeStep.formModel.licensingType) {
                        LicensingType.HMO_MANDATORY_LICENCE -> JourneyStateService.urlToStep(state.hmoMandatoryLicenceStep)
                        LicensingType.HMO_ADDITIONAL_LICENCE -> JourneyStateService.urlToStep(state.hmoAdditionalLicenceStep)
                        LicensingType.SELECTIVE_LICENCE -> JourneyStateService.urlToStep(state.selectiveLicenceStep)
                        else -> null
                    },
                )
            },
        )

    override fun chooseTemplate(state: PropertyRegistrationJourneyState): String = "forms/propertyRegistrationCheckAnswersForm"

    override fun mode(state: PropertyRegistrationJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@Scope("prototype")
@PrsdbWebComponent
final class PropertyRegistrationCheckAnswersStep(
    stepConfig: PropertyRegistrationCyaStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig)
