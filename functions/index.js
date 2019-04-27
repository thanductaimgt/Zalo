const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
exports.updateRoomLastMessage = functions.firestore
    .document('rooms/{roomId}/messages/{messageId}')
    .onCreate(async (snap, context) => {

      var newMessage = snap.data();

      // Get value of the newly added rating
      var ratingVal = change.after.data();

      // Get a reference to the restaurant
      var restRef = db.collection('restaurants').doc(context.params.restId);

      // Update aggregations in a transaction
      return db.runTransaction(transaction => {
        return transaction.get(restRef).then(restDoc => {
          // Compute new number of ratings
          var newNumRatings = restDoc.data('numRatings') + 1;

          // Compute new average rating
          var oldRatingTotal = restDoc.data('avgRating') * restDoc.data('numRatings');
          var newAvgRating = (oldRatingTotal + ratingVal) / newNumRatings;

          // Update restaurant info
          return transaction.update(restRef, {
            avgRating: newAvgRating,
            numRatings: newNumRatings
          });
        });
      });
    });

    exports.sendNotifications = functions.firestore.document('Notifications/{notifId}')
        .onCreate(async (snap, context) => {

          const newValue = snap.data();

          // perform desired operations ...
          // You may not need the notifId value but you have to keep the wildcard in the document path
        });