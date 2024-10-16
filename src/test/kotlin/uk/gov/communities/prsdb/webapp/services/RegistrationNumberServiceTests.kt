package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import uk.gov.communities.prsdb.webapp.constants.MAX_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.MIN_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.RegistrationNumberRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class RegistrationNumberServiceTests {
    private lateinit var mockRegNumRepository: RegistrationNumberRepository
    private lateinit var regNumService: RegistrationNumberService

    @BeforeEach
    fun setup() {
        mockRegNumRepository = mock()
        regNumService = RegistrationNumberService(mockRegNumRepository)
    }

    companion object {
        @JvmStatic
        fun provideParseableStringsAndRegNumDataModels() =
            listOf(
                // Expected format
                Arguments.of("L-CCCC-CCCC", RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MIN_REG_NUM)),
                Arguments.of("P-9999-9999", RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, MAX_REG_NUM)),
                Arguments.of("A-CCCC-CCCC", RegistrationNumberDataModel(RegistrationNumberType.AGENT, MIN_REG_NUM)),
                // Unexpected segmentation
                Arguments.of("L99999999", RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MAX_REG_NUM)),
                Arguments.of("L-999-999-99", RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MAX_REG_NUM)),
                // Unexpected segmenting symbol
                Arguments.of("L 9999 9999", RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MAX_REG_NUM)),
                // Unexpected case
                Arguments.of("l-cccc-cccc", RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MIN_REG_NUM)),
            )

        @JvmStatic
        fun provideNonParseableRegNumStrings() =
            listOf(
                // Invalid registration number type
                "9-9999-9999",
                // Invalid registration number characters
                "L-1111-1111",
                "L-LLLL-LLLL",
                // Invalid registration number length
                "L-9999-9999-9",
                "L-9999",
            )

        @JvmStatic
        fun provideRegNumsAndStrings() =
            listOf(
                Arguments.of(RegistrationNumber(RegistrationNumberType.LANDLORD, MIN_REG_NUM), "L-CCCC-CCCC"),
                Arguments.of(RegistrationNumber(RegistrationNumberType.PROPERTY, MAX_REG_NUM), "P-9999-9999"),
                Arguments.of(RegistrationNumber(RegistrationNumberType.AGENT, MAX_REG_NUM), "A-9999-9999"),
            )
    }

    @Test
    fun `createRegistrationNumber creates a registration number for the given entity type`() {
        `when`(mockRegNumRepository.existsByNumber(any(Long::class.java))).thenReturn(false)

        regNumService.createRegistrationNumber(RegistrationNumberType.LANDLORD)

        val regNumCaptor = captor<RegistrationNumber>()
        verify(mockRegNumRepository).save(regNumCaptor.capture())
        assertEquals(RegistrationNumberType.LANDLORD, regNumCaptor.value.type)
    }

    @Test
    fun `createRegistrationNumber creates a unique registration number`() {
        `when`(mockRegNumRepository.existsByNumber(any(Long::class.java))).thenReturn(true, false)

        regNumService.createRegistrationNumber(RegistrationNumberType.LANDLORD)

        verify(mockRegNumRepository, times(2)).existsByNumber(any(Long::class.java))
    }

    @ParameterizedTest
    @MethodSource("provideParseableStringsAndRegNumDataModels")
    fun `stringToRegNum returns a corresponding registration number data model`(
        parseableString: String,
        expectedRegNum: RegistrationNumberDataModel,
    ) {
        assertEquals(regNumService.stringToRegNum(parseableString), expectedRegNum)
    }

    @ParameterizedTest
    @MethodSource("provideNonParseableRegNumStrings")
    fun `stringToRegNum throws an illegal argument exception when given an invalid registration number`(nonParseableString: String) {
        assertThrows<IllegalArgumentException> { regNumService.stringToRegNum(nonParseableString) }
    }

    @ParameterizedTest
    @MethodSource("provideRegNumsAndStrings")
    fun `regNumToString returns a correctly formatted registration number string`(
        regNum: RegistrationNumber,
        expectedString: String,
    ) {
        assertEquals(regNumService.regNumToString(regNum), expectedString)
    }
}
