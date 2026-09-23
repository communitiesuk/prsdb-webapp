package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.featureFlags.DisabledFeatureFlagSelector
import uk.gov.communities.prsdb.webapp.config.featureFlags.EnabledFeatureFlagSelector
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagSelector
import uk.gov.communities.prsdb.webapp.constants.PAYMENTS

@PrsdbFlip(name = PAYMENTS, alterBean = "payments-property-registration-flag-on")
interface PaymentsPropertyRegistrationStrategy : FeatureFlagSelector

@Primary
@PrsdbWebService("payments-property-registration-flag-off")
class PaymentsPropertyRegistrationStrategyImplFlagOff :
    DisabledFeatureFlagSelector(),
    PaymentsPropertyRegistrationStrategy

@PrsdbWebService("payments-property-registration-flag-on")
class PaymentsPropertyRegistrationStrategyImplFlagOn :
    EnabledFeatureFlagSelector(),
    PaymentsPropertyRegistrationStrategy
