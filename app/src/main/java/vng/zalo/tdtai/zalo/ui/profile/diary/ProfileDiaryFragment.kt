package vng.zalo.tdtai.zalo.ui.profile.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile_diary.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.ui.home.diary.DiaryAdapter
import vng.zalo.tdtai.zalo.ui.profile.ProfileFragment
import vng.zalo.tdtai.zalo.ui.profile.ProfileViewModel
import javax.inject.Inject

class ProfileDiaryFragment: BaseFragment() {
    @Inject lateinit var profileFragment: ProfileFragment

    private val viewModel: ProfileViewModel by viewModels({ profileFragment }, { viewModelFactory })

    @Inject
    lateinit var adapter: DiaryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile_diary, container, false)
    }

    override fun onBindViews() {
        recyclerView.apply {
            adapter = this@ProfileDiaryFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onViewsBound() {
        viewModel.liveDiaries.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        viewModel.liveSelectedDiary.observe(viewLifecycleOwner, Observer { post ->
            val position = adapter.currentList.indexOfFirst { it.id == post.id }
            if (position != -1) {
                (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
//                recyclerView.scrollToPosition(position)
                profileFragment.viewPager.currentItem = 0
            } else {
                Toast.makeText(requireContext(), "post not loaded", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.imageView -> {
//                homeActivity.addMediaFragment()
            }
            R.id.avatarImgView->{
                addProfileFragment(view)
            }
            R.id.nameTextView->{
                addProfileFragment(view)
            }
        }
    }

    private fun addProfileFragment(view: View){
        val position = recyclerView.getChildAdapterPosition(view.parent as  View)
        val post = adapter.currentList[position]
        activity().addProfileFragment(post.ownerId!!)
    }
}