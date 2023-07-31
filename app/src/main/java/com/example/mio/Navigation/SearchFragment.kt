package com.example.mio.Navigation

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.SearchTabAdapter
import com.example.mio.Adapter.SearchWordAdapter
import com.example.mio.BuildConfig
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.SearchWordData
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.FragmentSearchBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class SearchFragment : Fragment(), MapView.MapViewEventListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var sBinding : FragmentSearchBinding? = null

    private lateinit var geocoder: Geocoder
    private lateinit var mapView: MapView

 /*

    //검색어 저장용 키
    private var setKey = "setting_search_history"

    //검색어 저장 데이터
    private var searchWordList : ArrayList<SearchWordData> = ArrayList()
    private var searchPos = 0*/
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
    ): View {
        sBinding = FragmentSearchBinding.inflate(inflater, container, false)

        mapView = sBinding!!.searchMapView
        mapView.setMapViewEventListener(this)
        //geocoder = Geocoder(this)
        firstVF()
        secondVF()


        return sBinding!!.root
    }

    private fun firstVF() {
        val resources = context?.resources
        val resourceId = resources?.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId != null && resourceId > 0) {
            val navigationBarHeight = resources.getDimensionPixelSize(resourceId)

            // 아래쪽 네비게이션 뷰에 마진 추가
            val activity = activity as AppCompatActivity
            val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            val layoutParams = bottomNavigationView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin += navigationBarHeight
            bottomNavigationView.layoutParams = layoutParams
        }
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        activity?.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                statusBarColor = Color.TRANSPARENT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                activity?.window?.setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }

        }



        sBinding?.etSearchField?.setOnClickListener {
            sBinding?.searchViewflipper?.showNext()
        }
    }

    private fun secondVF() {
    }



/*    private fun initRecentSearchRecyclerview() {
        swAdapter = SearchWordAdapter()
        swAdapter!!.searchWordData = searchWordList
        sBinding!!.searchRV.adapter = swAdapter
        sBinding!!.searchRV.layoutManager = LinearLayoutManager(sContext)
        sBinding!!.searchRV.setHasFixedSize(true)
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        sBinding = null
    }


/*    private fun getHistory() {
        val historyData = sContext?.let { sharedPref!!.getSearchHistory(it, setKey) }
        if (historyData!!.isNotEmpty()) {
            searchWordList.clear()
            //searchWordList.addAll(historyData)
            for (i in historyData.indices) {
                searchWordList.add(SearchWordData(i,historyData[i]))
                println(historyData[i])
                println(searchWordList)
                swAdapter!!.notifyDataSetChanged()
            }
        }
    }*/

    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        private const val API_KEY = BuildConfig.map_api_key
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMapViewInitialized(p0: MapView?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {

    }
    override fun onMapViewSingleTapped(mapView: MapView?, mapPoint: MapPoint?) {
/*        latitude = mapPoint?.mapPointGeoCoord?.latitude ?: return
        longitude = mapPoint.mapPointGeoCoord.longitude

        isAllCheck.isSecondVF.isPlaceName = true
        isAllCheck.isSecondVF.isPlaceRode = true
        myViewModel.postCheckValue(isAllCheck)


        if (marker != null) {
            mapView?.removePOIItem(marker)
            marker!!.markerType = MapPOIItem.MarkerType.BluePin
        }
        marker = MapPOIItem().apply {
            itemName = "선택 위치"
            this.mapPoint = mapPoint //MapPoint.mapPointWithGeoCoord(tlatitude, tlongitude)
            markerType = MapPOIItem.MarkerType.CustomImage
            customImageResourceId = R.drawable.map_poi_icon
            isCustomImageAutoscale = false
            isDraggable = true
            setCustomImageAnchor(0.5f, 1.0f)
        }
        mapView?.addPOIItem(marker)

        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                val address = addresses[0].getAddressLine(0)
                mBinding.placeRoad.text = "$address"
            }
        }*/
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {

    }
}