package uk.gov.communities.prsdb.webapp.constants

// Feature flags defined in application.yml should have a corresponding constant for their names here.
// They should also be added to the featureFlagNames list.
//
// Feature flags in code should be referred to using these constants.
//
// When loading the feature flag configuration, the application will check that all feature flag names
// defined in application.yml are included in this list.

// TODO PRSD-1683 - delete these example feature flags when no longer needed
// Feature flags for toy examples
const val EXAMPLE_FEATURE_FLAG_ONE = "example-feature-flag-one"

const val EXAMPLE_FEATURE_FLAG_TWO = "example-feature-flag-two"

const val EXAMPLE_FEATURE_FLAG_THREE = "example-feature-flag-three"

const val FAILOVER_TEST_ENDPOINTS = "failover-test-endpoints"

val featureFlagNames =
    listOf(
        EXAMPLE_FEATURE_FLAG_ONE,
        EXAMPLE_FEATURE_FLAG_TWO,
        EXAMPLE_FEATURE_FLAG_THREE,
        FAILOVER_TEST_ENDPOINTS,
    )
