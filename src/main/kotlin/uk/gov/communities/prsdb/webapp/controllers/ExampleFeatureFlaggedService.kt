package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
interface ExampleFeatureFlaggedService {
    @PrsdbFlip(name = EXAMPLE_FEATURE_FLAG_ONE, alterBean = "example-feature-flag-one-flag-on")
    fun getFeatureFlagPageHeading(): String

    @PrsdbFlip(name = EXAMPLE_FEATURE_FLAG_ONE, alterBean = "example-feature-flag-one-flag-on")
    fun getTemplateName(): String
}

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
@Primary
@PrsdbWebService("example-feature-flag-one-flag-off")
class ExampleFeatureFlagServiceImplFlagOff : ExampleFeatureFlaggedService {
    override fun getFeatureFlagPageHeading() = "Using ExampleFeatureFlaggedService - Flag OFF"

    // TODO PRSD-1683 - delete template when no longer needed
    override fun getTemplateName(): String = "featureFlagExamples/disabledFeature"
}

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
@PrsdbWebService("example-feature-flag-one-flag-on")
class ExampleFeatureFlagServiceImplFlagOn : ExampleFeatureFlaggedService {
    override fun getFeatureFlagPageHeading() = "Using ExampleFeatureFlaggedService - Flag ON"

    // TODO PRSD-1683 - delete template when no longer needed
    override fun getTemplateName(): String = "featureFlagExamples/enabledFeature"
}
