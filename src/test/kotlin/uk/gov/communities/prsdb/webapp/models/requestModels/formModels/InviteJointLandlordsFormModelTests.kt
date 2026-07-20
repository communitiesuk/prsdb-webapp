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

    @Test
    fun `a user cannot invite an email that is already a joint landlord on the property`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                existingLandlordEmails = mutableListOf("landlord@example.com")
                emailAddress = "landlord@example.com"
            }

        assertFalse(formModel.isEmailNotAlreadyOnProperty())
    }

    @Test
    fun `a user can invite an email that is not already a joint landlord on the property`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                existingLandlordEmails = mutableListOf("landlord@example.com")
                emailAddress = "new@example.com"
            }

        assertTrue(formModel.isEmailNotAlreadyOnProperty())
    }

    @Test
    fun `a user cannot invite their own logged-in email address`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                loggedInLandlordEmail = "me@example.com"
                emailAddress = "me@example.com"
            }

        assertFalse(formModel.isEmailNotLoggedInLandlord())
    }

    @Test
    fun `a user can invite an email that is not their own logged-in email address`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                loggedInLandlordEmail = "me@example.com"
                emailAddress = "someone@example.com"
            }

        assertTrue(formModel.isEmailNotLoggedInLandlord())
    }

    @Test
    fun `self-invite check passes when logged-in email is unknown`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                loggedInLandlordEmail = null
                emailAddress = "someone@example.com"
            }

        assertTrue(formModel.isEmailNotLoggedInLandlord())
    }

    @Test
    fun `a user cannot invite their own logged-in email address in a different case`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                loggedInLandlordEmail = "me@example.com"
                emailAddress = "ME@Example.com"
            }

        assertFalse(formModel.isEmailNotLoggedInLandlord())
    }

    @Test
    fun `a user cannot invite an already invited email in a different case`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                invitedEmailAddresses = mutableListOf("first@example.com")
                emailAddress = "First@Example.com"
            }

        assertFalse(formModel.isEmailNotAlreadyInvited())
    }

    @Test
    fun `a user cannot invite an email already on the property in a different case`() {
        val formModel =
            InviteJointLandlordsFormModel().apply {
                existingLandlordEmails = mutableListOf("landlord@example.com")
                emailAddress = "Landlord@Example.com"
            }

        assertFalse(formModel.isEmailNotAlreadyOnProperty())
    }
}
