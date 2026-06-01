# PDJB-299 — Branch Status Write-up (TEMPORARY)

> **Temporary document.** This is a working note capturing the in-progress state of the
> `feat/PDJB-299-property-record-landlords-tab` branch. It is not intended to be committed
> and can be deleted once the work is merged.
>
> Generated: 2026-06-01

## Summary

**Ticket:** PDJB-299 — add a new **"Landlords"** tab to the Property Record page
(landlord view), gated behind the `joint-landlords` feature flag.

- **Branch:** `feat/PDJB-299-property-record-landlords-tab`
- **State:** all changes are **uncommitted** (working tree only).
- **Sync:** branch is **11 commits behind `origin/main`** and can be fast-forwarded.
- **Tests:** written but **not yet run**.

## Changes

### New files
| File | Purpose |
| --- | --- |
| `src/main/kotlin/.../models/viewModels/landlordsTab/PropertyLandlordsTabViewModel.kt` | View model for the Landlords tab, with a `fromPropertyOwnership` factory. |
| `src/main/resources/templates/fragments/propertyDetails/landlordsTab.html` | Thymeleaf fragment rendering the tab contents. |
| `src/test/kotlin/.../models/viewModels/landlordsTab/PropertyLandlordsTabViewModelTests.kt` | Unit tests for the view model. |

### Modified files
| File | Change |
| --- | --- |
| `controllers/PropertyDetailsController.kt` | Injects `FeatureFlagManager`; builds the `landlordsTab` model attribute only when `JOINT_LANDLORDS` is enabled (landlord view only, not local council). |
| `templates/propertyDetailsView.html` | Renders the new `landlordsTab` fragment when present; adds a join-requests notification banner; falls back to the legacy summary list when the flag is off. |
| `messages/propertyDetails.yml` | New `landlords.*` message keys. |
| `application-local.yml` | `joint-landlords` flag flipped to `enabled: true` (local dev only). |
| `PropertyDetailsControllerTests.kt` | 3 new tests (flag on/off, local council view). |
| `integration/PropertyDetailsTests.kt` | ~9 new integration tests; old "landlord name link" test disabled by design. |
| `integration/.../PropertyDetailsPageLandlordView.kt` | New `LandlordsTab` page-object helpers. |

## Behaviour implemented

- **Registered landlords**: card built from the primary landlord (name, Landlord
  Registration Number, email), with an `isCurrentUser` flag driving a "(you)" suffix.
- **Sole-landlord inset**: shown when there is exactly one registered landlord, with a
  "confirm that you're the only landlord" link.
- **Invite a joint landlord** button.
- **Pending / expired invitations** and **joint landlord requests** sections plus a
  **join-requests notification banner** — DOM scaffolding in place, currently driven by
  empty lists.
- **Feature-flag fallback**: when `joint-landlords` is disabled, the legacy registered
  landlord summary list is shown instead.

## Tests written (not yet run)

- **Unit** — `PropertyLandlordsTabViewModelTests` (4 tests).
- **Controller** — flag enabled adds attribute, flag disabled does not, local council view
  never adds it.
- **Integration** — registered landlords heading/count, "(you)" suffix, no "Remove me"
  link for sole landlord, sole-landlord inset, invite button, empty
  invitation/join-request sections hidden, banner hidden when no requests, and legacy
  fallback when the flag is disabled.

## Open items / dependencies

- Many links are placeholder `href="#"` with `TODO PDJB-299/304/307` markers — their
  targets depend on sibling tickets (invite journey, confirm-sole-landlord page,
  respond-to-request, resend/cancel invitation, etc.).
- Invitations and join-requests data are intentionally stubbed as empty lists pending the
  underlying workflow/data that does not yet exist.
- **No tests have been run** and **nothing is committed** yet.

## Suggested next steps

1. Run unit + integration tests to verify the current state.
2. Run ktlint.
3. Rebase / fast-forward onto `origin/main`.
4. Commit and raise a draft PR.
