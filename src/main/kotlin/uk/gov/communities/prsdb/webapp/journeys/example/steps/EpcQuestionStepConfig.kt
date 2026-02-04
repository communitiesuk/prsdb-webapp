package uk.gov.communities.prsdb.webapp.journeys.example.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.ExampleEpcJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class EpcQuestionStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val epcLookupService: EpcLookupService,
) : AbstractRequestableStepConfig<EpcStatus, EpcFormModel, ExampleEpcJourneyState>() {
    override val formModelClass = EpcFormModel::class

    override fun getStepSpecificContent(state: ExampleEpcJourneyState) =
        mapOf(
            "formModel" to EpcFormModel(),
            "address" to propertyOwnershipService.getPropertyOwnership(state.propertyId).address.singleLineAddress,
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
                    RadiosDividerViewModel("forms.radios.dividerText"),
                    RadiosButtonViewModel(
                        value = HasEpc.NOT_REQUIRED,
                        labelMsgKey = "forms.epc.radios.option.notRequired.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: ExampleEpcJourneyState): String = "forms/certificateForm"

    override fun afterStepDataIsAdded(state: ExampleEpcJourneyState) {
        val uprn = propertyOwnershipService.getPropertyOwnership(state.propertyId).address.uprn
        if (uprn != null) {
            val epc = epcLookupService.getEpcByUprn(uprn)
            state.automatchedEpc = epc
        }
    }

    override fun mode(state: ExampleEpcJourneyState) =
        getFormModelFromStateOrNull(state)?.hasCert?.let {
            when (it) {
                HasEpc.YES -> if (state.automatchedEpc != null) EpcStatus.AUTOMATCHED else EpcStatus.NOT_AUTOMATCHED
                HasEpc.NO, HasEpc.NOT_REQUIRED -> EpcStatus.NO_EPC
            }
        }
}

@JourneyFrameworkComponent
final class EpcQuestionStep(
    stepConfig: EpcQuestionStepConfig,
) : RequestableStep<EpcStatus, EpcFormModel, ExampleEpcJourneyState>(stepConfig)
