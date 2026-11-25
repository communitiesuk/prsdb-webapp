package uk.gov.communities.prsdb.webapp.journeys.shared.payment

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_FEE_GUIDANCE_URL
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PaymentHelper
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PaymentState
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PaymentSummaryFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.PaymentService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@JourneyFrameworkComponent
class PaymentSummaryStepConfig(
    private val paymentService: PaymentService,
) : AbstractRequestableStepConfig<Complete, PaymentSummaryFormModel, PaymentState>() {
    override val formModelClass = PaymentSummaryFormModel::class

    override val changesAnswers = false

    override fun getStepSpecificContent(state: PaymentState): Map<String, Any?> {
        val dependencies = state.dependencies
        val quote = dependencies.paymentHandler.getQuote()
        state.paymentQuote = quote

        val firstChargeableDate = paymentService.getFirstChargeableDate(quote.paymentStartDate)

        return mapOf(
            "annualFee" to PaymentHelper.formatPence(paymentService.getAnnualFeeInPence()),
            "firstChargeableDate" to firstChargeableDate.format(DATE_FORMATTER),
            "isPreGratisPeriod" to firstChargeableDate.isAfter(quote.paymentStartDate),
            "summaryListRows" to getSummaryListRows(dependencies.paymentSummaryAddressLines, quote),
            "propertyFeeGuidanceUrl" to PROPERTY_FEE_GUIDANCE_URL,
            "quoteDate" to quote.quoteDate.toString(),
        )
    }

    private fun getSummaryListRows(
        addressLines: List<String>,
        quote: PropertyPaymentQuote,
    ) = listOf(
        SummaryListRowViewModel("registerProperty.paymentSummary.summary.address", addressLines),
        SummaryListRowViewModel("registerProperty.paymentSummary.summary.payment", PaymentHelper.formatPence(quote.amountInPence)),
        SummaryListRowViewModel("registerProperty.paymentSummary.summary.paymentDate", quote.quoteDate.format(DATE_FORMATTER)),
        SummaryListRowViewModel("registerProperty.paymentSummary.summary.renewalDate", quote.renewalDate.format(DATE_FORMATTER)),
    )

    override fun chooseTemplate(state: PaymentState) = "forms/paymentSummary"

    override fun mode(state: PaymentState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepIsReached(state: PaymentState) {
        if (state.dependencies.answersChangedSinceConfirmation) {
            throw CyaDataHasChangedException("Answers changed after confirmation for journey ${state.journeyId}")
        }
    }

    override fun afterStepDataIsAdded(state: PaymentState) {
        state.paymentOutcome = null
        state.paymentReference = null

        if (state.dependencies.paymentHandler.isPropertyAlreadyRegistered()) {
            state.dependencies.isPropertyAlreadyRegistered = true
        }
    }

    override fun resolveNextDestination(
        state: PaymentState,
        defaultDestination: Destination,
    ): Destination {
        val dependencies = state.dependencies
        if (dependencies.isPropertyAlreadyRegistered == true) {
            return Destination(dependencies.alreadyRegisteredStep)
        }

        val today = LocalDate.now(DateTimeHelper.UK_ZONE)
        val submittedQuoteDate = getFormModelFromState(state).getQuoteDateOrNull()
        val quote = state.paymentQuote

        if (submittedQuoteDate != today || quote == null || quote.quoteDate != submittedQuoteDate) {
            state.paymentQuote = dependencies.paymentHandler.getQuote(today)
            // TODO PDJB-1869: Inform the user about the updated payment total when a stale quote redirects them back to the payment summary
            //  page. For now, we will just redirect them back to the payment summary page with the updated total and no info text.
            return Destination(state.paymentSummaryStep)
        }

        return when (val creationResult = dependencies.paymentHandler.createPayment(quote)) {
            is PaymentCreationResult.NewPayment -> Destination.ExternalUrl(creationResult.nextUrl)
            PaymentCreationResult.ConcurrentPaymentStarted -> Destination(state.paymentSummaryStep)
        }
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    }
}

@JourneyFrameworkComponent
final class PaymentSummaryStep(
    stepConfig: PaymentSummaryStepConfig,
) : RequestableStep<Complete, PaymentSummaryFormModel, PaymentState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "payment-summary"
    }
}
