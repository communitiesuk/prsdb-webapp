package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class VerifiedIdentityDataModelTests {
    @Test
    fun `Converting to and from map retains data`() {
        val originalModel = VerifiedIdentityDataModel("John Doe", LocalDate.of(1990, 1, 1))
        val map = originalModel.toMap()
        val reconstructedModel = VerifiedIdentityDataModel.fromMap(map)

        assertEquals(originalModel, reconstructedModel)
    }
}
