package com.leisue.kyoo.commons;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;

import static com.leisue.kyoo.model.History.FIELD_HISTORY_DATE;
import static com.leisue.kyoo.model.Queue.FIELD_BOOKED_DATE;
import static com.leisue.kyoo.model.Queue.FIELD_NAME;

/**
 * Firestore helper class.
 */

public class FirestoreUtils {

    final private static String ENTITY_COLLECTION = "entity";
    final private static String QUEUE_COLLECTION = "queue";
    final private static String HISTORY_COLLECTION = "history";

    static {
        // Firestore logging
        FirebaseFirestore.setLoggingEnabled(false);
    }

    public static FirebaseFirestore getInstance() {
        return FirebaseFirestore.getInstance();
    }

    public static Query getQueryForQueue(final Queue queue) {
        final Entity entity = KyooApp.getInstance(KyooApp.getContext()).getEntity();
        return FirebaseFirestore.getInstance().collection(QUEUE_COLLECTION).document(entity.getId()).collection(queue.getId()).orderBy(FIELD_BOOKED_DATE, Query.Direction.ASCENDING);
    }

    public static Query getQueryForHistoryQueue() {
        final Entity entity = KyooApp.getInstance(KyooApp.getContext()).getEntity();

        // Sort by history date
        return FirebaseFirestore.getInstance().collection(HISTORY_COLLECTION).document(entity.getId()).collection(QUEUE_COLLECTION).orderBy(FIELD_HISTORY_DATE, Query.Direction.DESCENDING);

        // Sort by booked date
        //return FirebaseFirestore.getInstance().collection(HISTORY_COLLECTION).document(entity.getId()).collection(QUEUE_COLLECTION).orderBy(FIELD_HISTORY_DATE, Query.Direction.DESCENDING);
    }

    public static Query getQueryForQueueList() {
        final Entity entity = KyooApp.getInstance(KyooApp.getContext()).getEntity();
        // Sort by name
        return FirebaseFirestore.getInstance().collection(ENTITY_COLLECTION).document(entity.getId()).collection(QUEUE_COLLECTION).orderBy(FIELD_NAME);
    }

    public static Query getQueryForLatestBooking(final Queue queue) {
        final Entity entity = KyooApp.getInstance(KyooApp.getContext()).getEntity();
        return FirebaseFirestore.getInstance().collection(QUEUE_COLLECTION).document(entity.getId()).collection(queue.getId()).orderBy(FIELD_BOOKED_DATE, Query.Direction.DESCENDING).limit(1);
    }
}
