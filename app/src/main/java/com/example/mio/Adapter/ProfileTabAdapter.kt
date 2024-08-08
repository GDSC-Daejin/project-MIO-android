package com.example.mio.Adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mio.TabAccount.*
import com.example.mio.TabCategory.CarpoolTabFragment
import com.example.mio.TabCategory.TaxiTabFragment

class ProfileTabAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> ProfilePostFragment() //남의 프로필에서 그사람이 쓴 글
            else -> ProfileReviewFragment() //남의 프로필에서 그사람이 쓴 리뷰
        }
    }
}
