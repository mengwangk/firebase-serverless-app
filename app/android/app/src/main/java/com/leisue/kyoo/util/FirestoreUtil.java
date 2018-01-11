package com.leisue.kyoo.util;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;

import static com.leisue.kyoo.model.Queue.FIELD_BOOKED_DATE;

/**
 * Firestore helper class
 */

public class FirestoreUtil {

    final static String ENTITY_COLLECTION = "entity";
    final static String QUEUE_COLLECTION = "queue";

    static {
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
    }

    public static FirebaseFirestore getInstance(){
        return FirebaseFirestore.getInstance();
    }

    public static Query getBookings(final Queue queue) {
        final Entity entity = KyooApp.getInstance(KyooApp.getContext()).getEntity();
        return FirebaseFirestore.getInstance().collection(QUEUE_COLLECTION).document(entity.getId()).collection(queue.getId())
            .orderBy(FIELD_BOOKED_DATE, Query.Direction.ASCENDING);
    }
}
