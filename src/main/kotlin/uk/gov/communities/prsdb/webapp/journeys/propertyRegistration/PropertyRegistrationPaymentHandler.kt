package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.journeys.shared.payment.PaymentHandler
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PaymentService
import java.time.LocalDate

class PropertyRegistrationPaymentHandler(
    private val state: PropertyRegistrationJourneyState,
    private val paymentService: PaymentService,
    private val propertyRegistrationDataModelFactory: PropertyRegistrationDataModelFactory,
    private val addressAvailabilityService: AddressAvailabilityService,
) : PaymentHandler {
    override fun getQuote(quoteDate: LocalDate) = paymentService.getPropertyRegistrationPaymentQuote(quoteDate)

    override fun createPayment(quote: PropertyPaymentQuote) =
        paymentService.createPropertyRegistrationPayment(state.journeyId, state.loggedInLandlordEmailAtStartOfJourney, quote)

    override fun getStoredPaymentStatus(paymentReference: String) =
        paymentService.getStoredPropertyRegistrationPaymentStatus(state.journeyId, paymentReference)

    override fun finalisePayment(
        paymentId: String,
        govUkPayStatus: PaymentStatus,
    ) = paymentService.finalisePayment(paymentId, govUkPayStatus, propertyRegistrationDataModelFactory.fromJourneyState(state))

    override fun isAddressTaken(): Boolean {
        val uprn = state.propertyDetailsTask.addressTask.getAddress().uprn ?: return false
        return addressAvailabilityService.isAddressOwned(uprn)
    }
}

@PrsdbWebComponent
class PropertyRegistrationPaymentHandlerFactory(
    private val paymentService: PaymentService,
    private val propertyRegistrationDataModelFactory: PropertyRegistrationDataModelFactory,
    private val addressAvailabilityService: AddressAvailabilityService,
) {
    fun create(state: PropertyRegistrationJourneyState): PaymentHandler =
        PropertyRegistrationPaymentHandler(state, paymentService, propertyRegistrationDataModelFactory, addressAvailabilityService)
}
