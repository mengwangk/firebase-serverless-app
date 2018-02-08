## Setup
1. Firebase tools setup - refer [here](https://firebase.google.com/docs/functions/get-started) and [here](https://github.com/firebase/firebase-tools) for details.
2. To run the backend locally, run this command under the **server** folder
```
firebase serve --only hosting,functions
```
3. To deploy to the server (.firebaserc contains the  project id), run this command under **server** folder.
```
firebase deploy
```

## Source Code

1.  NodeJS app (**server** folder)

    | **Folder**        | **Description**   |
    | ------------- | ------------- |
    | functions     | Cloud functions containings all the API logic.|
    | public        | Cloud hosting. Currently not used.            |


2.  Sample projects (**app** folder)
    * **app** folder contains the sample testing project for Android. Use it as guidelines only. The code is **NOT** clean and some of the values are hardcoded.

3.  Refer [here](API.md) for API documentation.

## Configuration
1. **server/functions/env.json** contains the settings for development and production. Currently they are the same as we don't have a production environment yet.

    | **Parameter**          | **Description**   | 
    | -------------        |-------------| 
    | service_account      | Generate the json file from Firebase console. | 
    | database_url         | Get this from Firebase console.      |  
    | storage_bucket       | Get this from Firebase console.   |   


2. **server/functions/constants.js** contains constants used in the app.


## Coding Conventions
1. The project uses [StandardJS](https://standardjs.com/). Run this command under **server/functions** to format the code and check for errors (StandardJS must be installed).
    ```
    standard --fix
    ```

## Database Structure

| **Collection**    | **Description**   | 
| ------------- | ------------- | 
| entity        | Contains the entity and queue configuration data. | 
| queue         | Contains the queue transaction data.      |  
| history       | Contains the history data.      |   
| archive       | Contains the archived data.      | 
| lookup        | Contains the reference data      | 

### Entity Collection
* User information is stored directly under the **entity** collection with a UUID primary id.
* Queue configuration data is stored under **entity->:entity_id->queue** collection. Use the queue id to look up the bookings in **queue** collection.

### Queue Collection
* The respective bookings for the queues are stored under **queue->:entity_id->:queue_id** collection.

### History Collection
* Contains the removed or done bookings in **history->:entity_id->queue** collection.

### Archive Collection
* Archive summaries are stored in **archive->:entity_id->queue** collection.
* Archive details are stored in **archive->:entity_id->queue->:archive_id->history** collection.

### Lookup Collection
* Stored the reference data under **lookup** collection.

## General Guidelines
1. As much as possible, use the Firestore Query to get or display the data to leverage its realtime feature, use the REST API for insert, update and delete. REST API will handle the transaction and logic in the data manipulation. Refer to [Firestore documentation](https://firebase.google.com/docs/firestore/manage-data/delete-data) on mobile device constraints.
2. If the Query cannot adapt to the screen design, try to get the data and transform the data to adapt to the UI.
3. Minimize network request if data is already available and the app only needs to filter the data.
3. Firestore documentation is good to read to understand the APIs.

## Pending 
1. Secure endpoints
    * Uncomment the following line in server\functions\index.js 
    * Once the line is uncomment and the app is deployed, refer to the Android app (RetrofitClient.java) to see how to pass in the security token.

**index.js**

    ```
    // app.use(authenticate)
    ```

**RetrofitClient.java**

    ```
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new FirebaseUserIdTokenInterceptor())
                    .build();

    retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    //.client(okHttpClient)  // Uncomment to add the authorization header
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
    ```
   

2. Offline features 
    * Android app needs to wrap the Firestore and REST API and provide a local database layer on top of the remote API calls. Need to change the FirestoreAdapter logic. This will require considerable amount of code changes.
    * Refer to [here](https://proandroiddev.com/offline-apps-its-easier-than-you-think-9ff97701a73f) and [here](https://www.youtube.com/watch?v=70WqJxymPr8&t=574s) for general guidelines.


