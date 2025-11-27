package uk.gov.communities.prsdb.webapp.constants

// Feature releases defined in application.yml should have a corresponding constant for their names here.
// They should also be added to the featureFlagReleaseNames list.
//
// Feature releases in code should be referred to using these constants.
//
// When loading the feature configuration, the application will check that all feature releases names
// defined in application.yml are included in this list.

// TODO PRSD-1683 - delete these example feature flags when no longer needed
const val RELEASE_1_0 = "release-1-0"

val featureFlagReleaseNames =
    listOf(
        RELEASE_1_0,
    )
