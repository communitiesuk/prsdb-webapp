package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.journeys.shared.payment.PaymentHandler
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.CurrentUserService
import uk.gov.communities.prsdb.webapp.services.PaymentService
import uk.gov.communities.prsdb.webapp.services.PropertyReactivationService
import java.time.LocalDate

class PropertyReactivationPaymentHandler(
    private val state: PropertyReactivationJourneyState,
    private val paymentService: PaymentService,
    private val propertyReactivationService: PropertyReactivationService,
    private val propertyReactivationDataModelFactory: PropertyReactivationDataModelFactory,
    private val addressAvailabilityService: AddressAvailabilityService,
    private val currentUserService: CurrentUserService,
) : PaymentHandler {
    override fun getQuote(quoteDate: LocalDate) = paymentService.getPropertyReactivationPaymentQuote(state.propertyOwnershipId, quoteDate)

    override fun createPayment(quote: PropertyPaymentQuote) =
        paymentService.createPropertyReactivationPayment(
            state.propertyOwnershipId,
            state.journeyId,
            currentUserService.getCurrentEmail(),
            quote,
        )

    override fun getStoredPaymentStatus(paymentReference: String) =
        paymentService.getStoredPropertyReactivationPaymentStatus(state.propertyOwnershipId, paymentReference)

    override fun finalisePayment(
        paymentId: String,
        govUkPayStatus: PaymentStatus,
    ): PaymentStatus {
        val reactivationData = propertyReactivationDataModelFactory.fromJourneyState(state)
        return paymentService.finalisePayment(paymentId, govUkPayStatus) {
            propertyReactivationService.reactivatePropertyWithPayment(it, reactivationData)
        }
    }

    override fun isPropertyAlreadyRegistered(): Boolean {
        val uprn = state.uprn ?: return false
        return addressAvailabilityService.hasAnotherActiveRegistration(uprn, state.propertyOwnershipId)
    }
}

@PrsdbWebComponent
class PropertyReactivationPaymentHandlerFactory(
    private val paymentService: PaymentService,
    private val propertyReactivationService: PropertyReactivationService,
    private val propertyReactivationDataModelFactory: PropertyReactivationDataModelFactory,
    private val addressAvailabilityService: AddressAvailabilityService,
    private val currentUserService: CurrentUserService,
) {
    fun create(state: PropertyReactivationJourneyState): PaymentHandler =
        PropertyReactivationPaymentHandler(
            state,
            paymentService,
            propertyReactivationService,
            propertyReactivationDataModelFactory,
            addressAvailabilityService,
            currentUserService,
        )
}
