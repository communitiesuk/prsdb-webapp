package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericInnerStep
import uk.gov.communities.prsdb.webapp.journeys.example.EpcJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@Scope("prototype")
@PrsdbWebComponent
class EpcQuestionStep(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val epcLookupService: EpcLookupService,
) : AbstractGenericInnerStep<EpcStatus, EpcFormModel, EpcJourneyState>() {
    override val formModelClazz = EpcFormModel::class

    override fun getStepSpecificContent(state: EpcJourneyState) =
        mapOf(
            "formModel" to EpcFormModel(),
            "address" to
                propertyOwnershipService
                    .getPropertyOwnership(state.propertyId)
                    .property.address.singleLineAddress,
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

    override fun chooseTemplate(): String = "forms/certificateForm"

    override fun afterSubmitFormData(state: EpcJourneyState) {
        super.afterSubmitFormData(state)
        val uprn =
            propertyOwnershipService
                .getPropertyOwnership(state.propertyId)
                .property.address.uprn
        if (uprn != null) {
            val epc = epcLookupService.getEpcByUprn(uprn)
            state.automatchedEpc = epc
        }
    }

    override fun mode(state: EpcJourneyState) =
        getFormModelFromState(state)?.hasCert?.let {
            when (it) {
                HasEpc.YES -> if (state.automatchedEpc != null) EpcStatus.AUTOMATCHED else EpcStatus.NOT_AUTOMATCHED
                HasEpc.NO, HasEpc.NOT_REQUIRED -> EpcStatus.NO_EPC
            }
        }
}
