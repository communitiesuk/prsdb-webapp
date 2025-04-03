package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class PropertyDetailsEmailSectionTests {
    @Test
    fun `PropertyDetailsEmailSection toString returns a template string with the details of a single property`() {
        val expectedMarkdown =
            """
            ### Property 1 
            
            Property registration number: 
            
            ^P-WWW-XXX 
            
            Address: 1 Imaginary Street, Fakeville, FA1 2KE 
            
            
            """.trimIndent()

        val actualMarkdown =
            PropertyDetailsEmailSection(
                1,
                "P-WWW-XXX",
                "1 Imaginary Street, Fakeville, FA1 2KE",
            ).toString()

        assertEquals(expectedMarkdown, actualMarkdown)
    }

    @Test
    fun `PropertyDetailsEmailSectionList toString returns a template string with details of all the properties`() {
        val expectedMarkdown =
            """
            ### Property 1 
            
            Property registration number: 
            
            ^P-WWW-XXX 
            
            Address: 1 Imaginary Street, Fakeville, FA1 2KE 
            
            --- 
            ### Property 2 
            
            Property registration number: 
            
            ^P-YYY-ZZZ 
            
            Address: 2 Mythical Place, Fakeville, FA3 4KE 
            
            
            """.trimIndent()

        val actualMarkdown =
            PropertyDetailsEmailSectionList(
                listOf(
                    PropertyDetailsEmailSection(
                        1,
                        "P-WWW-XXX",
                        "1 Imaginary Street, Fakeville, FA1 2KE",
                    ),
                    PropertyDetailsEmailSection(
                        2,
                        "P-YYY-ZZZ",
                        "2 Mythical Place, Fakeville, FA3 4KE",
                    ),
                ),
            ).toString()

        assertEquals(expectedMarkdown, actualMarkdown)
    }

    @Test
    fun `fromPropertyOwnerships returns a PropertyDetailsEmailSectionList`() {
        val registrationNumber1 = RegistrationNumber(RegistrationNumberType.PROPERTY, 1)
        val registrationNumber2 = RegistrationNumber(RegistrationNumberType.PROPERTY, 2)
        val propertyOwnerships =
            listOf(
                MockLandlordData.createPropertyOwnership(
                    property =
                        MockLandlordData.createProperty(
                            address =
                                MockLandlordData.createAddress(
                                    singleLineAddress = "1 Imaginary Street, Fakeville, FA1 2KE",
                                ),
                        ),
                    registrationNumber = registrationNumber1,
                ),
                MockLandlordData.createPropertyOwnership(
                    property =
                        MockLandlordData.createProperty(
                            address =
                                MockLandlordData.createAddress(
                                    singleLineAddress = "2 Mythical Place, Fakeville, FA3 4KE",
                                ),
                        ),
                    registrationNumber = registrationNumber2,
                ),
            )
        val expectedPropertyDetailsEmailSectionList =
            PropertyDetailsEmailSectionList(
                propertyList =
                    listOf(
                        PropertyDetailsEmailSection(
                            1,
                            RegistrationNumberDataModel.fromRegistrationNumber(registrationNumber1).toString(),
                            "1 Imaginary Street, Fakeville, FA1 2KE",
                        ),
                        PropertyDetailsEmailSection(
                            2,
                            RegistrationNumberDataModel.fromRegistrationNumber(registrationNumber2).toString(),
                            "2 Mythical Place, Fakeville, FA3 4KE",
                        ),
                    ),
            )

        val propertyDetailsEmailSectionList = PropertyDetailsEmailSectionList.fromPropertyOwnerships(propertyOwnerships)

        assertEquals(expectedPropertyDetailsEmailSectionList, propertyDetailsEmailSectionList)
    }
}
