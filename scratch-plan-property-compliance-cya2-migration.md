# Plan: Update UpdateLicensingJourney to use CheckYourAnswersJourneyState2

**Target journey:** `UpdateLicensingJourney` (factory in `UpdatePropertyLicensingJourneyFactory.kt`, CYA step config in `UpdateLicensingCyaConfig.kt`)

**Reference implementation:** `PropertyRegistrationJourney` / `PropertyComplianceJourney` / `LandlordRegistrationJourney` (already migrated)

---

## Step 1: Remove old CYA calls from the journey factory
**Status:** complete

- Verify approval before proceeding.
- In `UpdatePropertyLicensingJourneyFactory.kt`:
  - Remove imports of `CheckYourAnswersJourneyState`, `CheckYourAnswersJourneyState.Companion.checkYourAnswersJourney`, and `CheckYourAnswersJourneyState.Companion.checkable`.
  - Remove the `checkable()` call on the `licensingTask`.
  - Remove the `checkYourAnswersJourney()` call at the end of the journey block.
- **Tests:** No new tests; downstream errors expected until later steps.

---

## Step 2: Create an `UpdateLicensingCheckableElements` enum
**Status:** complete

- Verify approval before proceeding.
- Define an enum in `UpdateLicensingCyaConfig.kt` with a single value corresponding to the summary list section with a visitable change-link destination:
  - `LICENSING` — links back to the licensing task
- **Tests:** No dedicated tests.

---

## Step 3: Update `UpdateLicensingCyaConfig` to extend `AbstractCheckYourAnswersStepConfig2`
**Status:** complete

- Verify approval before proceeding.
- Replace `AbstractCheckYourAnswersStepConfig` import with `AbstractCheckYourAnswersStepConfig2`.
- Change the class to extend `AbstractCheckYourAnswersStepConfig2<UpdateLicensingCheckableElements, UpdateLicensingJourneyState>()`.
- Add child journey initialisation loop at the top of `getStepSpecificContent()`.
- Replace the `childJourneyId` reference passed to `licensingDetailsHelper.getCheckYourAnswersSummaryList` with `state.getCyaJourneyId(UpdateLicensingCheckableElements.LICENSING)`.
- **Tests:** Verify project compiles (downstream errors expected until later steps).

---

## Step 4: Update `UpdateLicensingCyaStep` to use the new abstract base class
**Status:** complete

- Verify approval before proceeding.
- Change `UpdateLicensingCyaStep` to extend `AbstractCheckYourAnswersStep<UpdateLicensingCheckableElements, UpdateLicensingJourneyState>`.
- **Tests:** Verify project compiles.

---

## Step 5: Update `UpdateLicensingJourneyState` interface to implement `CheckYourAnswersJourneyState2`
**Status:** complete

- Verify approval before proceeding.
- Change interface to extend `CheckYourAnswersJourneyState2<UpdateLicensingCheckableElements>` instead of `CheckYourAnswersJourneyState`.
- Add `override val finishCyaStep: FinishCyaJourneyStep<UpdateLicensingCheckableElements>` to the interface.
- Add necessary imports (`FinishCyaJourneyStep`, `CheckYourAnswersJourneyState2`, `UpdateLicensingCheckableElements`).
- **Tests:** Verify project compiles.

---

## Step 6: Update `UpdateLicensingJourney` concrete class to implement the new interface
**Status:** complete

- Verify approval before proceeding.
- Add constructor parameters: `finishCyaStep`, `objectFactory: ObjectFactory<UpdateLicensingJourneyState>`.
- Replace `cyaChildJourneyIdIfInitialized` with `cyaJourneys`, `checkingAnswersFor`, `returnToCyaPageDestination` (backed by `cyaRouteSegment`).
- Implement `createChildJourneyState()`.
- Add `Destination` import.
- **Tests:** Verify project compiles.

---

## Step 7: Update the journey factory to support CYA child journeys
**Status:** complete

- Verify approval before proceeding.
- Restructure `createJourneySteps()` to check `state.checkingAnswersFor` and dispatch to `mainJourneyMap()` or `checkYourAnswersJourneyMap()`.
- Create `checkYourAnswersJourneyMap()` with a `when` block over `UpdateLicensingCheckableElements`, using `checkAnswerTask` for `LICENSING`.
- Import `checkAnswerTask` as needed.
- **Tests:** Verify project compiles. Run existing tests.

---

## Step 8: Final verification
**Status:** not started

- Verify approval before proceeding.
- Run the full test suite.
- Review that all old CYA pattern references are fully removed.
- Remove this scratch file.
