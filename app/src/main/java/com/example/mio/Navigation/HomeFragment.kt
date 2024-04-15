package com.example.mio.Navigation

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.TypefaceCompat
import androidx.fragment.app.Fragment
import com.example.mio.Adapter.CategoryTabAdapter
import com.example.mio.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var homeBinding: FragmentHomeBinding? = null
    private val tabTextList = listOf("카풀", "택시")
    // 선택된 탭을 저장할 변수
    private var selectedTabPosition: Int? = null
    private var selectedTab : String? = null
    //private val tabIconList = listOf(R.drawable.baseline_local_taxi_24, R.drawable.baseline_directions_car_24)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        // Bundle에서 데이터 추출
        selectedTab = arguments?.getString("selectedTab") //택시

        // 선택된 탭이 있다면 tabTextList에서 위치를 찾아 selectedTabPosition에 저장
        if (selectedTab != null) {
            selectedTabPosition = tabTextList.indexOf(selectedTab)
        }

        homeBinding!!.viewpager.adapter = CategoryTabAdapter(requireActivity())

        TabLayoutMediator(homeBinding!!.categoryTabLayout, homeBinding!!.viewpager) { tab, pos ->
            tab.text = tabTextList[pos]
            //val typeface = resources.getFont(com.example.mio.R.font.pretendard_medium)
            //tab.setIcon(tabIconList[pos])
            // 선택된 탭이 있고 현재 탭이 선택된 탭과 같으면 선택된 탭으로 설정
            if (selectedTabPosition != null && pos == selectedTabPosition) {
                homeBinding!!.categoryTabLayout.selectTab(tab)
            }
        }.attach()

        return homeBinding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeBinding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}