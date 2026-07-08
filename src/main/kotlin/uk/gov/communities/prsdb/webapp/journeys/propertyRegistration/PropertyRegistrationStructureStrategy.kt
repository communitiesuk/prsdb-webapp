package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException

@PrsdbFlip(name = PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING, alterBean = "property-registration-structure-flag-on")
interface PropertyRegistrationStructureStrategy {
    fun <T> ifEnabledOrElse(provider: IfEnabledConfig<T>.() -> Unit): T
}

@Primary
@PrsdbWebService("property-registration-structure-flag-off")
class PropertyRegistrationStructureStrategyImplFlagOff : PropertyRegistrationStructureStrategy {
    override fun <T> ifEnabledOrElse(provider: IfEnabledConfig<T>.() -> Unit): T {
        val config = IfEnabledConfig<T>()
        config.provider()
        val ifDisabled = config.ifDisabledProvider ?: throw JourneyInitialisationException("ifEnabledOrElse requires an ifDisabled block")
        return ifDisabled()
    }
}

@PrsdbWebService("property-registration-structure-flag-on")
class PropertyRegistrationStructureStrategyImplFlagOn : PropertyRegistrationStructureStrategy {
    override fun <T> ifEnabledOrElse(provider: IfEnabledConfig<T>.() -> Unit): T {
        val config = IfEnabledConfig<T>()
        config.provider()
        val ifEnabled = config.ifEnabledProvider ?: throw JourneyInitialisationException("ifEnabledOrElse requires an ifEnabled block")
        return ifEnabled()
    }
}
