package com.gdsc.mio.adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gdsc.mio.tabaccount.MyBookmarkFragment
import com.gdsc.mio.tabaccount.MyParticipationFragment
import com.gdsc.mio.tabaccount.MyPostFragment

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
