# Google Cloud Speech API examples

This directory contains Android example that uses the
[Google Cloud Speech API](https://cloud.google.com/speech/).

## Prerequisites

### Enable the Speech API

If you have not already done so,
[enable the Google Speech API for your project](https://cloud.google.com/speech/docs/getting-started). You
must be whitelisted to do this.

### Set Up to Authenticate With Your Project's Credentials

This Android app uses JSON credential file locally stored in the resources. ***You should not do
this in your production app.*** Instead, you should set up your own backend server that
authenticates app users. The server should delegate API calls from your client app. This way, you
can enforce usage quota per user. Alternatively, you should get the access token on the server side,
and supply client app with it. The access token will expire in a short while.

In this sample, we just put the Service Account in the client for ease of use. The app still gets
an access token using the service account credential, and use the token to call the API, so you can
see how to do so.

In order to try out this sample, visit the [Cloud Console](https://console.cloud.google.com/), and
navigate to:
`API Manager > Credentials > Create credentials > Service account key > New service account`.
Create a new service account, and download the JSON credentials file. Put the file in the app
resources as `app/src/main/res/raw/credential.json`.

Again, ***you should not do this in your production app.***

See the [Cloud Platform Auth Guide](https://cloud.google.com/docs/authentication#developer_workflow)
for more information.

### Test

Before running tests update the following environment variable with the service
account:

    GOOGLE_APPLICATION=service-account.json

This environment variable will be used to update the service account used here
`app/src/main/res/raw/credential.json`.

Run tests by using:

    gradle test

### Build signed release

*This step is optional.*

This sample uses ProGuard to decrease the number of methods generated by the gRPC library. It is
enabled by default for release build. If you want to build it, change the path, alias and passwords
of the keystore file specified in gradle.properties.

---------------------------------------------------------------------------------------------------------------------------------

### Basic UI
<img src="app/WhatsApp Image 2019-03-09 at 11.12.21 PM (4).jpeg">

### Features in Basic UI:
***MIC:***  Pressing on the mic enables/disables listening mode.
***Edit :*** Edit icon on the toolbar to Allow user to correct | edit the spoken text  
***Retrieve Data :*** Icon on the top let’s retrieve all the previously stored spoken text from firebase 

### Upon pressing the Mic
Search suggestions matching the current searched text pop from cache memory. The data is cached in Realm database and filtered using Trie.
<img src="app/WhatsApp Image 2019-03-09 at 11.12.21 PM (2).jpeg">

### Server-side (Firebase)
All the spoken texts are stored in Firebase Realtime Database.
<img src="app/WhatsApp Image 2019-03-09 at 11.12.21 PM (3).jpeg">


