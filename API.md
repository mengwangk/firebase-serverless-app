# REST API documentation

## Lookup

* File - server/functions/routes/lookup.js
* Collection - lookup

    | **URL**           | **HTTP**        |  **Description** | 
    | ------------- |-------------      | -------------|
    | /lookup/     |  GET            | Return all reference data.|
    | /lookup/:lookupType | GET     | Return the reference data. Currently only supports lookupType='industry' | 


## Entity

* server/functions/routes/entity.js
* Collection - entity

    | **URL**           | **HTTP**        |  **Description** | 
    | -------------     |-------------    | -------------|
    | /entity/          |  GET            | Retrieve entity if email query parameter is passed in, else return all entities.|
    | /entity/:entityId | GET     | Return the specificed entity. | 
    | /entity/          | POST   | Create the entity. | 
    | /entity/:entityId | PUT    | Update the entity. | 
    | /entity/user      | POST   | Create the user, entity and also upload the photo to Cloud Storage. | 
    | /entity/:entityId/queue     | GET   | Get all the queues belonged to this entity. | 
    | /entity/:entityId/queue/:queueId    | GET   | Get a specified queue. | 
    | /entity/:entityId/queue    | POST   | Create a queue for the specified entity. | 
    | /entity/:entityId/queue/:queueId    | PUT   | Update a specified queue for the entity. | 
    | /entity/:entityId/queue/:queueId    | DELETE   | Delete a specified queue for the entity. The queue must be empty and all related histories are archived.| 


## Queue

* server/functions/routes/queue.js
* Collection - queue

    | **URL**                        | **HTTP**        |  **Description** | 
    | -------------                  |-------------    | -------------|
    | /queue/:entityId/:queueId      |  GET             | Get bookings under a specified queue.|
    | /queue/:entityId/:queueId/count|  GET             | Get the booking count for a specified queue.|
    | /queue/:entityId/:queueId      |  POST            | Create a booking for a specified queue.|
    | /queue/:entityId/:queueId/:bookingId      |  PUT            | Update the booking for a specified queue.|
    | /queue/:entityId/:queueId/     |  DELETE            | Clear the specified queue.|
    | /queue/:entityId/:queueId/:bookingId/:action     |  DELETE            | Perform "done" or "remove" on the booking.|
    

## History

* server/functions/routes/history.js
* Collection - history

    | **URL**               | **HTTP**        |  **Description** | 
    | -------------         |-------------      | -------------|
    | /history/:entityId    |  GET            | Retrieve all histories for a specified entity.|
    | /history/:entityId/:queueId/:bookingId/:action    |  DELETE            | Return the history to the original queue. Action='return'.|
    | /history/:entityId/:action    |  DELETE            | Archive all histories for the specified entity. Action='archive'.|

## Archive
* server/functions/routes/archive.js
* Collection - archive

    | **URL**           | **HTTP**        |  **Description** | 
    | ------------- |-------------      | -------------|
    | /archive/:entityId     |  GET            | Retrieve archive summaries for a specified entity.|
    | /archive/:entityId/:archiveId | GET     | Retrieve archive details for a specified archive id. | 
    | /archive/:entityId/:archiveIds | DELETE     | Delete all archives passed in as comma separated list of archive ids.| 

## Support
* server/functions/routes/support.js
* Change the email and password in env.json accordingly
* Make sure the Gmail account is allowed to send email (refer to https://github.com/firebase/functions-samples/tree/master/quickstarts/email-users )
* Gmail has a limit. Do change this API later to use a custom SMTP provider.


    | **URL**           | **HTTP**        |  **Description** | 
    | -------------     |-------------    | -----------------|
    | /support/    |  POST            | Send a support email.|
