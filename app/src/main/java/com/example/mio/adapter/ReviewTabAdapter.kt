package com.example.mio.adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mio.tabaccount.*

class ReviewTabAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> MyReviewReadFragment()
            1 -> MyReviewWriteableFragment()
            else -> MyReviewWrittenFragment()
        }
    }
}
