package uk.gov.communities.prsdb.webapp.journeys.example.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.ExampleEpcJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@JourneyFrameworkComponent
class CheckEpcStepConfig(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractRequestableStepConfig<YesOrNo, CheckMatchedEpcFormModel, ExampleEpcJourneyState>() {
    override val formModelClass = CheckMatchedEpcFormModel::class

    override fun getStepSpecificContent(state: ExampleEpcJourneyState) =
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

    override fun chooseTemplate(state: ExampleEpcJourneyState): String = "forms/checkMatchedEpcForm"

    override fun mode(state: ExampleEpcJourneyState): YesOrNo? =
        getFormModelFromStateOrNull(state)?.let {
            when (it.matchedEpcIsCorrect) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
                null -> null
            }
        }

    override fun isSubClassInitialised(): Boolean = ::getReleventEpc.isInitialized

    private lateinit var getReleventEpc: (ExampleEpcJourneyState) -> EpcDataModel?

    fun usingEpc(getReleventEpc: ExampleEpcJourneyState.() -> EpcDataModel?): CheckEpcStepConfig {
        this.getReleventEpc = getReleventEpc
        return this
    }
}

@JourneyFrameworkComponent
final class CheckEpcStep(
    stepConfig: CheckEpcStepConfig,
) : RequestableStep<YesOrNo, CheckMatchedEpcFormModel, ExampleEpcJourneyState>(stepConfig)
