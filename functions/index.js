const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

// Listens for new messages added to /messages/:pushId/original and creates an
// uppercase version of the message to /messages/:pushId/uppercase
exports.broadCastRoomOnlineStatus = functions.firestore.document('users/{userId}')
    .onUpdate((snapshot, context) => {
      // Grab the current value of what was written to the Realtime Database.
      const message = snapshot.data();
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