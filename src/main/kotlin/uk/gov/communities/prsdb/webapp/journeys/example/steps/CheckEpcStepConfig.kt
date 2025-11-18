package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.EpcJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@Scope("prototype")
@PrsdbWebComponent
class CheckEpcStepConfig(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractStepConfig<YesOrNo, CheckMatchedEpcFormModel, EpcJourneyState>() {
    override val formModelClass = CheckMatchedEpcFormModel::class

    override fun getStepSpecificContent(state: EpcJourneyState) =
        getReleventEpc(state)?.let { epcDetails ->
            mapOf(
                "title" to "propertyCompliance.title",
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
        getFormModelFromStateOrNull(state)?.let {
            when (it.matchedEpcIsCorrect) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
                null -> null
            }
        }

    override fun isSubClassInitialised(): Boolean = ::getReleventEpc.isInitialized

    private lateinit var getReleventEpc: (EpcJourneyState) -> EpcDataModel?

    fun usingEpc(getReleventEpc: EpcJourneyState.() -> EpcDataModel?): CheckEpcStepConfig {
        this.getReleventEpc = getReleventEpc
        return this
    }
}

@Scope("prototype")
@PrsdbWebComponent
final class CheckEpcStep(
    stepConfig: CheckEpcStepConfig,
) : RequestableStep<YesOrNo, CheckMatchedEpcFormModel, EpcJourneyState>(stepConfig)
