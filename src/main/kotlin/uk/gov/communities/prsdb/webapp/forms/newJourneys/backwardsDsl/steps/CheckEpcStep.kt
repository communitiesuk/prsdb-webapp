package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.forms.newJourneys.YesOrNo
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.EpcJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@Scope("prototype")
@PrsdbWebComponent
class CheckEpcStep(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractStep<YesOrNo, CheckMatchedEpcFormModel, EpcJourneyState>() {
    override val formModelClazz = CheckMatchedEpcFormModel::class

    private lateinit var getReleventEpc: (EpcJourneyState) -> EpcDataModel?

    fun usingEpc(getReleventEpc: EpcJourneyState.() -> EpcDataModel?): CheckEpcStep {
        this.getReleventEpc = getReleventEpc
        return this
    }

    override fun getStepContent(state: EpcJourneyState) =
        getReleventEpc(state)?.let { epcDetails ->
            mapOf(
                "title" to "propertyCompliance.title",
                "fieldSetHeading" to "forms.checkMatchedEpc.fieldSetHeading",
                "epcDetails" to epcDetails,
                "epcCertificateUrl" to epcDetails.certificateNumber.let { epcCertificateUrlProvider.getEpcCertificateUrl(it) },
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
            )
        } ?: emptyMap()

    override fun chooseTemplate(state: EpcJourneyState): String = "forms/checkMatchedEpcForm"

    override fun mode(state: EpcJourneyState): YesOrNo? =
        getFormModelFromState(state)?.let {
            when (it.matchedEpcIsCorrect) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
                null -> null
            }
        }
}
