package com.example.mio.Adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mio.TabAccount.MyBookmarkFragment
import com.example.mio.TabAccount.MyParticipationFragment
import com.example.mio.TabAccount.MyPostFragment
import com.example.mio.TabCategory.CarpoolTabFragment
import com.example.mio.TabCategory.TaxiTabFragment

//여기 페이지 변경하기
class AccountTabAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> MyPostFragment()
            1 -> MyParticipationFragment()
            else -> MyBookmarkFragment()
        }
    }
}
