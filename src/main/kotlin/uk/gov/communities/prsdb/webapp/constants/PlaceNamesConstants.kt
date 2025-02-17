package uk.gov.communities.prsdb.webapp.constants

import uk.gov.communities.prsdb.webapp.helpers.DataLoader

// Files taken from https://www.gov.wales/bydtermcymru/international-place-names
val internationalPlaceNameCSVFiles =
    listOf(
        "classpath:data/place_names/country_names.csv",
        "classpath:data/place_names/crown_dependency_names.csv",
        "classpath:data/place_names/overseas_territory_names.csv",
    )

val INTERNATIONAL_PLACE_NAMES = DataLoader.loadPlaceNames(internationalPlaceNameCSVFiles)

const val ENGLAND_OR_WALES = "England or Wales"
