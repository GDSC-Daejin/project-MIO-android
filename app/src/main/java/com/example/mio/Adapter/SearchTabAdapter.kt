package com.example.mio.Adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mio.TapSearch.LocationSearchFragment
import com.example.mio.TapSearch.PostSearchFragment

//여기 페이지 변경하기
class SearchTabAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> PostSearchFragment()
            else -> LocationSearchFragment()
        }
    }
}
