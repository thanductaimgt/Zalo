const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

// Listens for new messages added to /messages/:pushId/original and creates an
// uppercase version of the message to /messages/:pushId/uppercase
exports.onNewMessage = functions.firestore.document('rooms/{roomId}/messages/{messageId}')
    .onCreate((snapshot, context) => {
//    db.collection('users').where
      // Grab the current value of what was written to the Realtime Database.
//      const message = snapshot.val();
    //   console.log('Uppercasing', context.params.pushId, original);
      message.content = message.content.toUpperCase();
      // You must return a Promise when performing asynchronous tasks inside a Functions such as
      // writing to the Firebase Realtime Database.
      // Setting an "uppercase" sibling in the Realtime Database returns a Promise.
      return snapshot.ref.set(message);

      const newValue = change.after.data();
      const previousValue = change.before.data();

      if(newValue.isOnline != oldValue.isOnline){
          
      }

      // access a particular field as you would any JS property
      const name = newValue.name;
    });