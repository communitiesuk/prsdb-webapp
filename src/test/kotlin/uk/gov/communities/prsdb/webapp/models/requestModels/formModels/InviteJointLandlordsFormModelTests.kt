package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InviteJointLandlordsFormModelTests {
    @Test
    fun `when editing, a user can submit their original email with no changes`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                invitedEmailAddresses = mutableListOf("first@example.com", "second@example.com")
                emailBeingEdited = "first@example.com"
                emailAddress = "first@example.com"
            }

        assertTrue(formModel.isEmailNotAlreadyInvited())
    }

    @Test
    fun `when editing, a user cannot edit their email to another already invited email`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                invitedEmailAddresses = mutableListOf("first@example.com", "second@example.com")
                emailBeingEdited = "first@example.com"
                emailAddress = "second@example.com"
            }

        assertFalse(formModel.isEmailNotAlreadyInvited())
    }

    @Test
    fun `when editing, a user can add a new email that has not already been invited`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                invitedEmailAddresses = mutableListOf("first@example.com")
                emailBeingEdited = "first@example.com"
                emailAddress = "new@example.com"
            }

        assertTrue(formModel.isEmailNotAlreadyInvited())
    }

    @Test
    fun `without editing, a user cannot submit their original email with no changes`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                invitedEmailAddresses = mutableListOf("first@example.com")
                emailAddress = "first@example.com"
            }

        assertFalse(formModel.isEmailNotAlreadyInvited())
    }

    @Test
    fun `without editing, a user can add a new email that has not already been invited`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                invitedEmailAddresses = mutableListOf("first@example.com")
                emailAddress = "new@example.com"
            }

        assertTrue(formModel.isEmailNotAlreadyInvited())
    }
}
