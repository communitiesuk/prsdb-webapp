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

const val EXAMPLE_FEATURE_FLAG_FOUR = "example-feature-flag-four"

const val FAILOVER_TEST_ENDPOINTS = "failover-test-endpoints"

const val MIGRATE_PROPERTY_REGISTRATION = "migrate-property-registration"

const val MIGRATE_LOCAL_COUNCIL_USER_REGISTRATION = "migrate-local-council-user-registration"

const val MIGRATE_PROPERTY_COMPLIANCE = "migrate-property-compliance"

const val MIGRATE_PROPERTY_DEREGISTRATION = "migrate-property-deregistration"

const val MIGRATE_LANDLORD_EMAIL_UPDATE = "migrate-landlord-email-update"

const val MIGRATE_LANDLORD_PHONE_NUMBER_UPDATE = "migrate-landlord-phone-number-update"

const val MIGRATE_LANDLORD_NAME_UPDATE = "migrate-landlord-name-update"

const val MIGRATE_LANDLORD_DATE_OF_BIRTH_UPDATE = "migrate-landlord-date-of-birth-update"

val featureFlagNames =
    listOf(
        EXAMPLE_FEATURE_FLAG_ONE,
        EXAMPLE_FEATURE_FLAG_TWO,
        EXAMPLE_FEATURE_FLAG_THREE,
        EXAMPLE_FEATURE_FLAG_FOUR,
        FAILOVER_TEST_ENDPOINTS,
        MIGRATE_PROPERTY_REGISTRATION,
        MIGRATE_LOCAL_COUNCIL_USER_REGISTRATION,
        MIGRATE_PROPERTY_COMPLIANCE,
        MIGRATE_PROPERTY_DEREGISTRATION,
        MIGRATE_LANDLORD_EMAIL_UPDATE,
        MIGRATE_LANDLORD_PHONE_NUMBER_UPDATE,
        MIGRATE_LANDLORD_NAME_UPDATE,
        MIGRATE_LANDLORD_DATE_OF_BIRTH_UPDATE,
    )
