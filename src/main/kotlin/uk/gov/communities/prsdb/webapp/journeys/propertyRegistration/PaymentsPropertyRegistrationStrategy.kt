package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.featureFlags.DisabledFeatureFlagStrategy
import uk.gov.communities.prsdb.webapp.config.featureFlags.EnabledFeatureFlagStrategy
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagStrategy
import uk.gov.communities.prsdb.webapp.constants.PAYMENTS

@PrsdbFlip(name = PAYMENTS, alterBean = "payments-property-registration-flag-on")
interface PaymentsPropertyRegistrationStrategy : FeatureFlagStrategy

@Primary
@PrsdbWebService("payments-property-registration-flag-off")
class PaymentsPropertyRegistrationStrategyImplFlagOff :
    DisabledFeatureFlagStrategy(),
    PaymentsPropertyRegistrationStrategy

@PrsdbWebService("payments-property-registration-flag-on")
class PaymentsPropertyRegistrationStrategyImplFlagOn :
    EnabledFeatureFlagStrategy(),
    PaymentsPropertyRegistrationStrategy
