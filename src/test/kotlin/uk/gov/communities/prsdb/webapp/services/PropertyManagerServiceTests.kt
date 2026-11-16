package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.PropertyManager
import uk.gov.communities.prsdb.webapp.database.repository.PropertyManagerRepository

@ExtendWith(MockitoExtension::class)
class PropertyManagerServiceTests {
    @Mock
    private lateinit var propertyManagerRepository: PropertyManagerRepository

    @InjectMocks
    private lateinit var propertyManagerService: PropertyManagerService

    @Test
    fun `createPropertyManager saves a property manager`() {
        // Arrange
        whenever(propertyManagerRepository.save(any<PropertyManager>())).thenAnswer { it.arguments[0] }

        // Act
        val propertyManager = propertyManagerService.createPropertyManager("Agent Ltd", "agent@example.com")

        // Assert
        assertEquals("Agent Ltd", propertyManager.name)
        assertEquals("agent@example.com", propertyManager.email)
    }

    @Test
    fun `updatePropertyManager mutates an existing one in place`() {
        // Arrange
        val existing = PropertyManager("Old Agent", "old@example.com")

        // Act
        propertyManagerService.updatePropertyManager(existing, "New Agent", "new@example.com")

        // Assert
        assertEquals("New Agent", existing.name)
        assertEquals("new@example.com", existing.email)
        verify(propertyManagerRepository).save(existing)
    }
}
