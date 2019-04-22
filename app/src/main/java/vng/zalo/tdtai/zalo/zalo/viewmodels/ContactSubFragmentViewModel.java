package vng.zalo.tdtai.zalo.zalo.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.ContactItemModel;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_IS_FRIEND_OF;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.COLLECTION_USERS;

public class ContactSubFragmentViewModel extends ViewModel {


    private List<Task<QuerySnapshot>> tasks;

    public MutableLiveData<List<ContactItemModel>> liveContacts;
    private static final String TAG = ContactSubFragmentViewModel.class.getSimpleName();

    public ContactSubFragmentViewModel() {
        FirebaseFirestore firestore = ZaloApplication.getFirebaseInstance();
        liveContacts = new MutableLiveData<>((List<ContactItemModel>) new ArrayList<ContactItemModel>());

        tasks = new ArrayList<>();

        //get contacts'phone
        tasks.add(firestore.collection(COLLECTION_IS_FRIEND_OF)
                .whereArrayContains("userPhones", ZaloApplication.currentUserPhone)
                .get());

        tasks.add(firestore.collection(COLLECTION_USERS)
                .get());

        Tasks.whenAll(tasks).addOnCompleteListener(new ContactQueryListener());
    }

    class ContactQueryListener implements OnCompleteListener<Void> {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {
                List<String> contactsPhone = new ArrayList<>();
                for (QueryDocumentSnapshot doc : tasks.get(0).getResult()) {
                    List<String> userPhones = (List<String>) doc.get("userPhones");
                    for (String userPhone : userPhones) {
                        if (!userPhone.equals(ZaloApplication.currentUserPhone)) {
                            contactsPhone.add(userPhone);
                            break;
                        }
                    }
                }

                List<ContactItemModel> contactItemModels = new ArrayList<>();
                for (QueryDocumentSnapshot doc : tasks.get(1).getResult()) {
                    String phone = doc.getString("phone");
                    if(contactsPhone.contains(phone)){
                        ContactItemModel contactItemModel = new ContactItemModel(
                                phone,
                                doc.getString("avatar")
                        );
                        contactItemModels.add(contactItemModel);
                    }
                }
                Log.d(TAG,contactItemModels.get(0).phone);
                liveContacts.setValue(contactItemModels);
                Log.d(TAG, "Contact Query Successful");
            } else {
                Log.d(TAG, "Contact Query Fail");
            }
        }
    }
}