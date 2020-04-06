package vng.zalo.tdtai.zalo.data_model

import com.google.firebase.firestore.DocumentSnapshot

interface BaseDataModel {
    fun toMap(): HashMap<String, Any?>

    fun applyDoc(doc: DocumentSnapshot)
}