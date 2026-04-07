package uk.gov.communities.prsdb.webapp.constants

// Feature flags defined in application.yml should have a corresponding constant for their names here.
// They should also be added to the featureFlagNames list.
//
// Feature flags in code should be referred to using these constants.
//
// When loading the feature flag configuration, the application will check that all feature flag names
// defined in application.yml are included in this list.

const val FAILOVER_TEST_ENDPOINTS = "failover-test-endpoints"

const val JOINT_LANDLORDS = "joint-landlords"

const val SUBJECT_IDENTIFIER_PAGE = "subject-identifier-page"

val featureFlagNames =
    listOf(
        FAILOVER_TEST_ENDPOINTS,
        JOINT_LANDLORDS,
        SUBJECT_IDENTIFIER_PAGE,
    )
