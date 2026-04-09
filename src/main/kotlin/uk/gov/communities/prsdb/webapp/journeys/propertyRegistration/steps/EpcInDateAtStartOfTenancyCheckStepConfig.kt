package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@JourneyFrameworkComponent("propertyRegistrationEpcExpiryCheckStepConfig")
class EpcInDateAtStartOfTenancyCheckStepConfig :
    AbstractRequestableStepConfig<EpcInDateAtStartOfTenancyCheckMode, EpcExpiryCheckFormModel, EpcState>() {
    override val formModelClass = EpcExpiryCheckFormModel::class

    @Autowired
    lateinit var messageSource: MessageSource

    override fun getStepSpecificContent(state: EpcState): Map<String, Any?> {
        val expiryDate = state.acceptedEpc?.expiryDateAsJavaLocalDate
        val formattedDate = expiryDate?.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH))
        val yesHintValue =
            formattedDate?.let {
                messageSource.getMessageForKey(
                    "forms.epcInDateAtStartOfTenancyCheck.yes.hint",
                    arrayOf(it),
                )
            }
        return mapOf(
            "expiryDate" to expiryDate,
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = true,
                        valueStr = "yes",
                        labelMsgKey = "forms.radios.option.yes.label",
                        hintValue = yesHintValue,
                    ),
                    RadiosButtonViewModel(
                        value = false,
                        valueStr = "no",
                        labelMsgKey = "forms.radios.option.no.label",
                    ),
                ),
        )
    }

    override fun chooseTemplate(state: EpcState) = "forms/epcInDateAtStartOfTenancyCheckForm"

    override fun mode(state: EpcState) =
        getFormModelFromStateOrNull(state)?.let {
            when (it.tenancyStartedBeforeExpiry) {
                true -> EpcInDateAtStartOfTenancyCheckMode.IN_DATE
                false -> EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE
                null -> null
            }
        }
}

@JourneyFrameworkComponent("propertyRegistrationEpcExpiryCheckStep")
final class EpcInDateAtStartOfTenancyCheckStep(
    stepConfig: EpcInDateAtStartOfTenancyCheckStepConfig,
) : RequestableStep<EpcInDateAtStartOfTenancyCheckMode, EpcExpiryCheckFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-in-date-at-start-of-tenancy-check"
    }
}

enum class EpcInDateAtStartOfTenancyCheckMode {
    IN_DATE,
    NOT_IN_DATE,
}
