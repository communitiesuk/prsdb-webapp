package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.WebServerOnly
import uk.gov.communities.prsdb.webapp.config.featureFlags.DisabledFeatureFlagSelector
import uk.gov.communities.prsdb.webapp.config.featureFlags.EnabledFeatureFlagSelector
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagSelector
import uk.gov.communities.prsdb.webapp.constants.PAYMENTS

// Implementations use @Service rather than @PrsdbWebService so FF4J can read their bean names without creating every bean
@PrsdbFlip(name = PAYMENTS, alterBean = "payments-property-registration-flag-on")
interface PaymentsPropertyRegistrationStrategy : FeatureFlagSelector

@Primary
@Conditional(WebServerOnly::class)
@Service("payments-property-registration-flag-off")
class PaymentsPropertyRegistrationStrategyImplFlagOff :
    DisabledFeatureFlagSelector(),
    PaymentsPropertyRegistrationStrategy

@Conditional(WebServerOnly::class)
@Service("payments-property-registration-flag-on")
class PaymentsPropertyRegistrationStrategyImplFlagOn :
    EnabledFeatureFlagSelector(),
    PaymentsPropertyRegistrationStrategy
