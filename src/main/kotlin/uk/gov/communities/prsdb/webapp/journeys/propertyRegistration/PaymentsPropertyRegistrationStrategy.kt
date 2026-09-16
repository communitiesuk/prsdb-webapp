package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PAYMENTS

@PrsdbFlip(name = PAYMENTS, alterBean = "payments-property-registration-flag-on")
interface PaymentsPropertyRegistrationStrategy {
    fun <T> ifEnabledOrElse(
        ifEnabled: () -> T,
        ifDisabled: () -> T,
    ): T

    fun ifEnabled(action: () -> Unit)
}

@Primary
@PrsdbWebService("payments-property-registration-flag-off")
class PaymentsPropertyRegistrationStrategyImplFlagOff : PaymentsPropertyRegistrationStrategy {
    override fun <T> ifEnabledOrElse(
        ifEnabled: () -> T,
        ifDisabled: () -> T,
    ): T = ifDisabled()

    override fun ifEnabled(action: () -> Unit) {}
}

@PrsdbWebService("payments-property-registration-flag-on")
class PaymentsPropertyRegistrationStrategyImplFlagOn : PaymentsPropertyRegistrationStrategy {
    override fun <T> ifEnabledOrElse(
        ifEnabled: () -> T,
        ifDisabled: () -> T,
    ): T = ifEnabled()

    override fun ifEnabled(action: () -> Unit) {
        action()
    }
}
