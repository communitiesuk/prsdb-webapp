# Analytics

We are using both [Plausible](https://plausible.io/docs) and [Google Analytics](https://developers.google.com/analytics/devguides/collection/ga4) to track user interactions with the service.

We have not yet implemented any custom events in either, but both should be tracking page views and some basic user interactions.

Both sets of analytics are added via code snippets on the "layout.html" page.

## Plausible

Plausible has been set up to track optional events including outbound links, file downloads, 404 error pages and hashed page paths.
All of those except for hashed page paths add to our billable pageviews.
Modifying these requires a code change.

### Plausible Team

To view the analytics on Plausible, you will need to be added to the Team on the Plausible account or be added as a guest on the sites.
Check with your team lead who can add more members if you're unsure.

(To edit the team, click on your name in the top right of the screen and select "Team settings" from the drop-down menu.
Team members can be added in the "General" section)

Note there may be a team limit of 10 members, so we may need to remove people if we reach that limit and instead add them as guests on the sites (see below).

#### Restricting access to production Analytics

We may need to only include people on the team who are authorized to view the production analytics (team members can see all "sites")
We should be able to add people to view the test analytics only by going to the site settings and adding people as guests.

### Viewing Plausible Analytics

To view the analytics, from the [dashboard](https://plausible.io/sites) click on the site you want to view.
* prod.register-home-to-rent.communities.gov.uk will be for production (it won't receive anything until prod is live).
* register-home-to-rent.communities.gov.uk is for everything else (integration, test, register-home-to-rent, search-landlord-home-information).
On the dashboard we can filter data by hostname to get environment specific data https://plausible.io/docs/subdomain-hostname-filter
Each site will receive data from both the register-home-to-rent and the search-landlord-home-information domains.

## Google Analytics (GA)

Google Analytics uses cookies to track unique users, so full data is only available when users accept cookies.
GA has been set up so that it can still send data such as page views to GA. It is unclear how useful this data will be but we can filter by whether or not the user has consented to cookies when it comes to viewing.

### Google Analytics Team and Accounts
In order to view the Google Analytics data, you will need to be added to the PRSD Google Analytics account.
For that you need a google account with a .communities.gov.uk email address.

If you don't have one already, you can create one from the google account sign in page by going to Create account -> For work or my business -> Get a Gmail address.
When you get to the page asking you to choose an email address, there should be a "Use an existing email address" link next to the "Next" button - for some people this hasn't been appearing on the first try but has appeared later in the day.
You should then be able to use your communities.gov.uk email address to create a google account.

Once you have a google account, ask someone to add you to the Google Analytics account (check with your team lead who can add more members if you're unsure).

Once you are added, it should show up at https://analytics.google.com/

### Viewing Google Analytics
Google Analytics should viewable at https://analytics.google.com/

There are two "Properties" set up on the PRSD account, one for PRSD Test and one for PRSD Prod.
Each property will receive data from both the register-home-to-rent and the search-landlord-home-information domains.
PRSD Test will receive data from all environments except production.

We haven't set much up in the way of reports yet, but you should be able to see information such as page views, visitors etc.
From manual testing, it looks like page titles show up for users who have accepted cookies, but not for those who haven't.
You can view Real-Time data by clicking on the "Realtime" section in the left-hand menu - note it can take a while for events to show up here!

### Debugging with Google Tag Assistant

[Google Tag Assistant](https://tagassistant.google.com/) is useful tool for viewing analytics events being fired by a page.

Click on "Add domain", enter the url you want to test (this can be a localhost url) and click "Connect". 
It will open the page in a new window and show you the events being fired in the left hand panel on the original window. You can see more details of the events by clicking on them.
As you click around the site, you should see events being fired for page views and other interactions.
