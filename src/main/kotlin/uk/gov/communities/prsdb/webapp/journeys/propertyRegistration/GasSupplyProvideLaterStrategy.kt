package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT

@PrsdbFlip(name = DELEGATE_TO_LETTING_AGENT, alterBean = "gas-supply-provide-later-flag-on")
interface GasSupplyProvideLaterStrategy {
    fun <T> ifEnabledOrElse(
        ifEnabled: () -> T,
        ifDisabled: () -> T,
    ): T
}

@Primary
@PrsdbWebService("gas-supply-provide-later-flag-off")
class GasSupplyProvideLaterStrategyImplFlagOff : GasSupplyProvideLaterStrategy {
    override fun <T> ifEnabledOrElse(
        ifEnabled: () -> T,
        ifDisabled: () -> T,
    ): T = ifDisabled()
}

@PrsdbWebService("gas-supply-provide-later-flag-on")
class GasSupplyProvideLaterStrategyImplFlagOn : GasSupplyProvideLaterStrategy {
    override fun <T> ifEnabledOrElse(
        ifEnabled: () -> T,
        ifDisabled: () -> T,
    ): T = ifEnabled()
}
