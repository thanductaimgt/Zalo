package vng.zalo.tdtai.zalo.data_model.react

import vng.zalo.tdtai.zalo.data_model.BaseDataModel

data class ReactPage(
        var reactType: Int = React.TYPE_LOVE,
        var reacts:ArrayList<React> = arrayListOf()
) : BaseDataModel {
    override fun toMap(): HashMap<String, Any?> {
        return HashMap()
    }
}