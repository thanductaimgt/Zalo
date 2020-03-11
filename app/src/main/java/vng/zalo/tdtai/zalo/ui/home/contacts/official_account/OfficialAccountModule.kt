package vng.zalo.tdtai.zalo.ui.home.contacts.official_account

import android.view.View
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey

@Module
interface OfficialAccountModule {
    @Binds
    fun bindClickListener(officialAccountFragment: OfficialAccountFragment):View.OnClickListener

    @Binds
    @IntoMap
    @ViewModelKey(OfficialAccountViewModel::class)
    fun bindOfficialAccountViewModel(viewModel: OfficialAccountViewModel): ViewModel
}