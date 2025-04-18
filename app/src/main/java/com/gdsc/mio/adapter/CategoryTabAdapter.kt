package com.gdsc.mio.adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gdsc.mio.tabcategory.CarpoolTabFragment
import com.gdsc.mio.tabcategory.TaxiTabFragment

//여기 페이지 변경하기
class CategoryTabAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> CarpoolTabFragment()
            else -> TaxiTabFragment()
        }
    }
}
