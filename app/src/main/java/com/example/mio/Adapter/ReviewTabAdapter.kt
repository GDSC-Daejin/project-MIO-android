package com.example.mio.Adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mio.TabAccount.*

//여기 페이지 변경하기
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
