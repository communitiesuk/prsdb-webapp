package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
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
    fun `updatePropertyManager creates one when there is none`() {
        // Arrange
        whenever(propertyManagerRepository.save(any<PropertyManager>())).thenAnswer { it.arguments[0] }

        // Act
        val propertyManager = propertyManagerService.updatePropertyManager(null, "Agent Ltd", "agent@example.com")

        // Assert
        assertEquals("Agent Ltd", propertyManager!!.name)
        assertEquals("agent@example.com", propertyManager.email)
    }

    @Test
    fun `updatePropertyManager mutates an existing one in place`() {
        // Arrange
        val existing = PropertyManager("Old Agent", "old@example.com")

        // Act
        val propertyManager = propertyManagerService.updatePropertyManager(existing, "New Agent", "new@example.com")

        // Assert
        assertSame(existing, propertyManager)
        assertEquals("New Agent", propertyManager!!.name)
        assertEquals("new@example.com", propertyManager.email)
        verify(propertyManagerRepository).save(existing)
    }

    @Test
    fun `updatePropertyManager deletes an existing one when the landlord no longer uses a property manager`() {
        // Arrange
        val existing = PropertyManager("Old Agent", "old@example.com")

        // Act
        val propertyManager = propertyManagerService.updatePropertyManager(existing, null, null)

        // Assert
        assertNull(propertyManager)
        verify(propertyManagerRepository).delete(existing)
    }

    @Test
    fun `updatePropertyManager does nothing when there is none and the landlord does not use one`() {
        // Act
        val propertyManager = propertyManagerService.updatePropertyManager(null, null, null)

        // Assert
        assertNull(propertyManager)
        verifyNoInteractions(propertyManagerRepository)
    }

    @Test
    fun `updatePropertyManager throws when a name is given without an email`() {
        // Act & Assert
        assertThrows<NullPointerException> {
            propertyManagerService.updatePropertyManager(null, "Agent Ltd", null)
        }
    }

    @Test
    fun `updatePropertyManager leaves an existing one untouched when a name is given without an email`() {
        // Arrange
        val existing = PropertyManager("Old Agent", "old@example.com")

        // Act & Assert
        assertThrows<NullPointerException> {
            propertyManagerService.updatePropertyManager(existing, "New Agent", null)
        }
        assertEquals("Old Agent", existing.name)
        assertEquals("old@example.com", existing.email)
        verifyNoInteractions(propertyManagerRepository)
    }
}
