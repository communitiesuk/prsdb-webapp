# Multi Step Form Journey Framework

There are several multi-page forms on the service, and they're all implemented by the Forms package.

The entire multi-page form is represented by the `Journey` class, and each point of interaction with the user is a 
`Step`.

## Steps
A journey is made up of a collection of steps, each of which represents a single transaction where we ask the user for
some information and the user provides it. Each has a unique ID, which is an emum value where each journey has its own 
enum. The step is also responsible for the journey flow via the `nextAction` and `handleSubmitAndRedirect` functions. 

Most of the flow is managed by the former, but the decision to save the journey to the database is determined by the 
presence of the latter. For a simpler journeys, each step's `nextAction` would just return the fixed next step in the 
flow with the last having a `handleSubmitAndRedirect`. 
Most of the journeys in the service are not that simple and have branching paths based on the users previous answers. 
In this case the `nextAction` function looks at the data the user has submitted so far to determine which step should be
visited next. 
The `handleSubmitAndRedirect` function can also be used if the user should be "jumped" to another point in the journey
when completing a `Step`.

Each step also has a `Page` class which represents the step's form page. It is responsible for providing the Thymeleaf 
template name, the content attributes for the template and for validating the data submitted on that page. 
The `Step` by default delegates all data validation to the `Page`, but custom validation can be set on a `Step`.

### Subpages
Some steps are repeatable an indeterminate number of times, for example if you need to specify all interested parties
or list all of your addresses for the past 5 years. In these cases `subpage` can be specified while using those steps.
This is just an integer submitted as a URL parameter that is tracked as part of the `StepDetails` and used to record the
step data as separate. All the above logic should handle subpages for steps to which it applies, and can specify a 
specific subpage as the next step or redirect destination.

At time of writing there are no uses of this in the service, so it has not been thoroughly tested.

### Sections and tasks
Each step belongs to a task. A task is a logical unit of data that may take multiple steps to submit. For example, a
task could be to submit your licensing information which is made up of a step for selecting your licence type then a
step for submitting your licence number. Currently tasks are only used by the task-list page for the relevant journeys.

Each task belongs to a section. A section is related set of tasks, for example the LandlordRegistrationJourney has three
sections: "privacy notice", "register details", "check and submit". These sections are used both on the task-list page
to display tasks in logical groupings and on the pages of the journey to show the user which section their current step
belongs to.

## The Journey base class
The `Journey` class is responsible for marshalling the steps. The base class has two main functions for progressing 
through the multi-page form:

### `getPageForStep`/`populateModelAndGetViewName`
returns the name of the ThymeLeaf template for a step and populates the model for it, or returns a 
redirect if the requested step is unreachable. This includes validation errors for the page if data has been submitted
for it.

### `completeStep`/`updateJourneyDataAndGetViewNameOrRedirect`
validates the data submitted for the step and either adds it to the journey data and redirects to the next step or
returns the page for the current step with validation errors. If a `handleSubmitAndRedirect` has been set for the step
that will be called instead of moving to the next step.

### Saving journey progress
A step can be set to `saveAfterSubmit` - if this is set to true, then whenever that step is completed a copy of the 
`JourneyData` is saved to the database as a JSON encoded string. 
That journey can be restored by calling `loadJourneyDataIfNotLoaded` on the `Journey`, which will return the data to the
session to allow the user to resume the in progress journey.

### The iterator
Journey implements `Iterable<StepDetails>`, which represents the series of steps taken in order based on the current
journey data. The first `StepDetails` is always the same, then the iterator validates that step and calculates the 
`nextAction`, repeating the process until it reaches the end of the journey (`nextAction` returns null) or a step that 
does not have valid data. 

This is used to determine whether a step is reachable. That is to say that a step is reachable if and only if the 
current data would result in visiting the current page if, starting on the initial step, you went to the next action for
each page reached in turn.

## Journey's Subclasses
There are a couple of abstract subclasses of journey:
### The JourneyWithTaskList
the JourneyWithTaskList adds a task-list page, which is like
a contents page for the journey - it shows the list of all tasks in the journey along with their current status. If the
first step of a task is reachable then it will also link to that step.

### The UpdateJourney
The `UpdateJourney` represents a journey for updating details and has a few idiosyncrasies. While the `nextAction` flow
proceeds through all the steps in the usual way, the iterator takes into account a merge of original data and the
new journey data to determine whether a step is reachable. Many of the steps set a `handleSubmitAndRedirect` to redirect
the user to the equivalent of the "check your answers" page in other journeys. This allows us to check the new combined 
data is valid when multiple changes are made, and to direct the user to the correct page if there is a problem. 

### Final children
Each final child of `Journey` represents a particular multi-page form in the service. The actual steps that make up that
journey are specified, including the `nextAction` and `handleSubmitAndRedirect` logic for each. 
