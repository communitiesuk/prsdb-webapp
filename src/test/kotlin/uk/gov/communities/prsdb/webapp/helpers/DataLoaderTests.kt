package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.models.dataModels.PlaceNameDataModel

class DataLoaderTests {
    @Test
    fun `loadPlaceNames returns an ordered list of PlaceNameDataModels`() {
        val placeNameCSVFiles =
            listOf(
                "classpath:data/place_names/country_names.csv",
                "classpath:data/place_names/crown_dependency_names.csv",
                "classpath:data/place_names/overseas_territory_names.csv",
            )

        val expectedPlaceNameDataModels =
            listOf(
                PlaceNameDataModel("Afghanistan"),
                PlaceNameDataModel("Akrotiri"),
                PlaceNameDataModel("Guernsey, Alderney, Sark"),
                PlaceNameDataModel("Isle of Man"),
                PlaceNameDataModel("Pitcairn, Henderson, Ducie and Oeno Islands"),
                PlaceNameDataModel("Zimbabwe"),
            )

        val placeNameDataModels = DataLoader.loadPlaceNames(placeNameCSVFiles)

        assertEquals(expectedPlaceNameDataModels, placeNameDataModels)
    }
}
