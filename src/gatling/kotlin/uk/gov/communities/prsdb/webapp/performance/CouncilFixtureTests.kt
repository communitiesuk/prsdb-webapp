package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CouncilFixtureTests {
    @Test
    fun `council exports reject reused actors targets and invalid field types before HTTP`() {
        val row = """{"subject":"actor","actorName":"Actor","councilId":2,"councilName":"Council",
            "targetId":42,"targetName":"Target","targetEmail":"target@example.com","targetSubject":"target",
            "targetManager":false,"propertyId":100,"address":"1 Street, AB1 2CD","uprn":"100000",
            "propertyRegistrationNumber":200,"landlordId":300,"landlordName":"Landlord",
            "landlordRegistrationNumber":400,"licenseType":null}"""
        assertEquals(42L, CouncilFixture.parse("[$row]", 1).single().targetId)
        for (invalid in listOf(
            "[$row,$row]",
            "[$row]".replace("\"target\"", "\"actor\""),
            "[$row]".replace("\"targetManager\":false", "\"targetManager\":\"false\""),
            "[$row]".replace("\"propertyId\":100", "\"propertyId\":-1"),
        )) {
            assertFailsWith<IllegalArgumentException> { CouncilFixture.parse(invalid, 1) }
        }
        assertFailsWith<IllegalArgumentException> { CouncilFixture.parse("[$row]", 2) }
    }
}
