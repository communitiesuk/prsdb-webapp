package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PaymentService
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@JourneyFrameworkComponent
class PaymentSummaryStepConfig(
    private val addressAvailabilityService: AddressAvailabilityService,
    private val paymentService: PaymentService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any?> {
        val quote = paymentService.getPropertyRegistrationPaymentQuote()
        state.paymentQuote = quote

        val firstChargeableDate = paymentService.getFirstChargeableDate(quote.quoteDate)

        return mapOf(
            "annualFee" to formatPence(paymentService.getAnnualFeeInPence()),
            "amount" to formatPence(quote.amountInPence),
            "paymentDate" to quote.quoteDate.format(DATE_FORMATTER),
            "renewalDate" to quote.renewalDate.format(DATE_FORMATTER),
            "firstChargeableDate" to firstChargeableDate.format(DATE_FORMATTER),
            "isInFreePeriod" to firstChargeableDate.isAfter(quote.quoteDate),
            "addressParts" to state.propertyDetailsTask.addressTask.getAddress().toMultiLineAddress().split("\n"),
        )
    }

    override fun chooseTemplate(state: PropertyRegistrationJourneyState) = "forms/paymentSummary"

    override fun mode(state: PropertyRegistrationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: PropertyRegistrationJourneyState) {
        val addressTask = state.propertyDetailsTask.addressTask
        val uprn = addressTask.getAddress().uprn
        if (uprn != null && addressAvailabilityService.isAddressOwned(uprn)) {
            addressTask.isAddressAlreadyRegistered = true
        }
    }

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        val addressTask = state.propertyDetailsTask.addressTask
        if (addressTask.isAddressAlreadyRegistered == true) {
            return Destination(addressTask.alreadyRegisteredStep)
        }

        val today = LocalDate.now(DateTimeHelper.UK_ZONE)
        val quote = state.paymentQuote

        // TODO PDJB-1861: Check that the quote the user was shown matches the stored quote before creating the payment (e.g. when                                                                                                                                                                                                                                               ┃
        //  the page is open in several tabs), and explain to the user when the quoted amount has changed.
        if (quote == null || quote.quoteDate != today) {
            state.paymentQuote = paymentService.getPropertyRegistrationPaymentQuote(today)
            // TODO PDJB-996: Confirm how to explain to the user when an overnight refresh changes the quoted amount.
            //  This will most likely be a rare occurrence, but we should still handle it gracefully and inform the user of what has happened.
            //  For now, we will just redirect them back to the payment summary page. If no design decision is made before PDJB-996 is completed
            //  we will raise another ticket to handle this scenario and point this at it.
            return Destination(state.paymentSummaryStep)
        }

        val nextUrl =
            paymentService.createPropertyRegistrationPayment(
                state.journeyId,
                state.loggedInLandlordEmailAtStartOfJourney,
                quote,
            )
        return Destination.ExternalUrl(nextUrl)
    }

    private fun formatPence(pence: Int): String {
        val pounds = BigDecimal(pence).movePointLeft(2)
        return if (pence % 100 == 0) "£${pounds.toBigInteger()}" else "£${pounds.setScale(2)}"
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    }
}

@JourneyFrameworkComponent
final class PaymentSummaryStep(
    stepConfig: PaymentSummaryStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "payment-summary"
    }
}
