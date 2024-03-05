package com.example.mio.Navigation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.contains
import androidx.fragment.app.Fragment

import com.example.mio.*
import com.example.mio.Model.*
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.TapSearch.NearbypostActivity
import com.example.mio.TapSearch.SearchResultActivity
import com.example.mio.databinding.FragmentSearchBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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

    private val permissionRequestCode1 = 20
    private val permissionRequestCode2 = 21

    private var PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private var sBinding : FragmentSearchBinding? = null

    private lateinit var geocoder: Geocoder
    private var mapView: MapView? = null


    val sharedViewModel: FragSharedViewModel by lazy {
        (activity?.application as FragSharedViewModel2).sharedViewModel
    }

    //private lateinit var horizontalAdapter: HorizontalAdapter

    //private lateinit var searchResultAdapter: SearchResultAdapter

    // val contents = ArrayList<Content>()

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

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sBinding = FragmentSearchBinding.inflate(inflater, container, false)

        multiplePermissionsLauncher.launch(PERMISSIONS)



        geocoder = Geocoder(requireContext())

        // test 했는데 됨 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        //getPostsByLocation(37.870684661337016, 127.15612168310325)

        val resources = context?.resources
        val resourceId = resources?.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId != null && resourceId > 0) {
            val navigationBarHeight = resources.getDimensionPixelSize(resourceId)

            // 아래쪽 네비게이션 뷰에 마진 추가
            val activity = activity as AppCompatActivity
            val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            val layoutParams = bottomNavigationView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = 0
            layoutParams.bottomMargin += navigationBarHeight
            bottomNavigationView.layoutParams = layoutParams
        }
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        activity?.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            //activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            activity?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            // 새로운 시스템UI 비헤이비어를 가져옵니다.
            val windowInsetsController = activity?.window?.insetsController

            // 시스템UI를 변경하기 전에 null 체크를 합니다.
            windowInsetsController?.let { controller ->
                // 상태 표시줄을 투명하게 만듭니다.
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                // 상태 표시줄의 색상을 라이트 모드로 변경합니다.
                activity?.window?.decorView?.apply {
                    systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
            //activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        sBinding?.btSearchField?.setOnClickListener {
/*            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_content, SearchResultFragment())
            //transaction.addToBackStack(null)
            transaction.commit()*/
            val intent = Intent(activity, SearchResultActivity::class.java)
            startActivity(intent)
        }

        Log.d("ObserverSetup", "Setting up observer")
        sharedViewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            Log.d("DEBUG", "LiveData observed: $location")
            location?.let {

                // 마커로 선택된 게시글 위치 표시
                displaySelectedPostOnMap(location)

            }
        }

        mapView?.setPOIItemEventListener(object : MapView.POIItemEventListener {
            override fun onPOIItemSelected(mapView: MapView, poiItem: MapPOIItem) {
                val selectedPost = poiItem.userObject as? LocationReadAllResponse
                if (selectedPost == null) {
                    Log.d("DEBUG", "poiItem.userObject is not of type LocationReadAllResponse")
                } else {
                    Log.d("DEBUG", "Selected post after casting: $selectedPost")
                    displaySelectedPostOnMap(selectedPost)
                }
/*                selectedPost?.let {
                    sharedViewModel.selectedLocation.value = it
                    Log.d("11111111", "1111" + sharedViewModel.selectedLocation.value)
                }*/
            }

            override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView, poiItem: MapPOIItem) {}
            override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView, poiItem: MapPOIItem, buttonType: MapPOIItem.CalloutBalloonButtonType) {}
            override fun onDraggablePOIItemMoved(mapView: MapView, poiItem: MapPOIItem, newMapPoint: MapPoint) {}
        })


        return sBinding!!.root
    }

    private fun displaySelectedPostOnMap(location: LocationReadAllResponse) {
        Log.d("DEBUG", "displaySelectedPostOnMap triggered with location: $location")
        val selectedLatLng = MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude)

        // 선택된 게시글 위치 표시
        val Marker = MapPOIItem().apply {
            itemName = "Selected Post"
            mapPoint = selectedLatLng
            markerType = MapPOIItem.MarkerType.CustomImage
            customImageResourceId = R.drawable.map_poi_sr2
            isShowCalloutBalloonOnTouch = false
        }

        mapView?.addPOIItem(Marker)


        // 지도의 중심을 선택된 위치로 이동
        mapView?.setMapCenterPointAndZoomLevel(selectedLatLng, 2, true)

        sBinding?.btSearchField?.hint = location.location

        // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
        // 주변 게시글 데이터를 불러옵니다.
        loadNearbyPostData(location.postId)
        // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ


        sBinding?.postData?.apply {
            visibility = View.VISIBLE
            sBinding?.postTitle?.text = location.title
            sBinding?.postDate?.text = location.targetDate + " " + location.targetTime
            sBinding?.postLocation?.text = location.location
            sBinding?.postParticipation?.text = location.participantsCount.toString()
            sBinding?.postParticipationTotal?.text = location.numberOfPassengers.toString()

            sBinding?.postSearchItem?.setOnClickListener {
/*                val intent = Intent(context, NoticeBoardReadActivity::class.java)
                intent.putExtra("POST_ID", location.postId)  // 게시글의 ID 또는 유일한 키를 전달
                startActivity(intent)*/
                val postData = PostData(
                    accountID = location.user.studentId,
                    postID = location.postId,
                    postTitle = location.title,
                    postContent = location.content,
                    postTargetDate = location.targetDate,
                    postTargetTime = location.targetTime,
                    postCategory = location.category.categoryName,
                    postLocation = location.location,
                    postParticipation = location.participantsCount,
                    postParticipationTotal = location.numberOfPassengers,
                    postCost = location.cost,
                    postVerifyGoReturn = location.verifyGoReturn,
                    user = location.user,
                    postlatitude = location.latitude,
                    postlongitude = location.longitude
                )

                // Intent를 통해 NoticeBoardReadActivity로 전달
                val intent = Intent(context, NoticeBoardReadActivity::class.java)
                intent.putExtra("type", "READ")
                intent.putExtra("postItem", postData)
                startActivity(intent)
            }
        }


        // 더보기 부분분
        sBinding?.postMoreLayout?.apply {
            visibility = View.VISIBLE

            sBinding?.postMore?.setOnClickListener {
                // NearbyPostsActivity로 이동
                val intent = Intent(context, NearbypostActivity::class.java)
                intent.putExtra("POST_ID", location.postId)
                startActivity(intent)

            }
        }
   }

    // 주변 게시글 데이터를 불러오는 함수
    private fun loadNearbyPostData(postId: Int) {

        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getNearByPostData(postId).enqueue(object :
                Callback<List<LocationReadAllResponse>> {
                override fun onResponse(call: Call<List<LocationReadAllResponse>>, response: Response<List<LocationReadAllResponse>>) {
                    if (response.isSuccessful) {
                        response.body()?.forEach { nearbyPost ->
                            addMarker(nearbyPost) // 지도에 마커 추가 메소드 호출
                        }
                    }
                    else {
                        println("loadNearbyPostData!!!!!!!!!!!!!!!!!")
                        Log.d("comment", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<List<LocationReadAllResponse>>, t: Throwable) {
                    Log.d("error", t.toString())
                    Log.e("SearchFragment", "Error fetching nearby post data: ${t.localizedMessage}")
                }
            })
        }
    }

    // 주변 게시글 데이터를 기반으로 지도에 마커를 추가하는 메소드
    private fun addMarker(location: LocationReadAllResponse) {
        val latLng = MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude)
        val marker = MapPOIItem().apply {
            itemName = location.title
            mapPoint = latLng
            markerType = MapPOIItem.MarkerType.CustomImage
            customImageResourceId = R.drawable.map_poi_srn
            isShowCalloutBalloonOnTouch = true
            userObject = location
        }
        mapView?.addPOIItem(marker)

        // 마커 클릭시
/*        mapView.setPOIItemEventListener(object : MapView.POIItemEventListener {
            override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, mapPOIItem: MapPOIItem?) {
                updatePostData(mapPOIItem?.userObject as? LocationReadAllResponse)
                Log.d("1111", "111111")
            }

            override fun onCalloutBalloonOfPOIItemTouched(p0: MapView, p1: MapPOIItem, p2: MapPOIItem.CalloutBalloonButtonType) {
            }

            override fun onPOIItemSelected(p0: MapView, p1: MapPOIItem) {
            }

            override fun onDraggablePOIItemMoved(p0: MapView, p1: MapPOIItem, p2: MapPoint) {
            }
        })*/
    }

    //안드로이드 13 이상 PostNotification 대응
    /*private fun checkPostNotificationPermission() {
        //Android 13 이상 && 푸시권한 없음
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            && PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            val permissionCheck1 = ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )

            val permissionCheck2 = ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if (permissionCheck1 != PackageManager.PERMISSION_GRANTED && permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    permissionRequestCode1
                )
            }
        }
    }*/


    private val multiplePermissionsLauncher  =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { (permission, isGranted) ->
                when {
                    isGranted -> {
                        // 권한이 승인된 경우 처리할 작업
                        //mapView = MapView(requireActivity())
                        //val mapViewContainer = sBinding?.mapView
                        if (mapView == null/* && mapViewContainer?.contains(mapView!!) == true*/) {
                            try {
                                // 다시 맵뷰 초기화 및 추가
                                /*mapViewContainer.addView(mapView)
                                mapView?.setMapViewEventListener(this)*/
                                //initMapView()

                            } catch (re: RuntimeException) {
                                Log.e("SERACHFRAGMENT", re.toString())
                            }
                        } else {
                            //initMapView()
                            Log.e("SearchFragment", "mapview is not null")
                        }
                        //val mapViewContainer = sBinding?.mapView

                        //mapView = sBinding?.searchMapView
                        Toast.makeText(requireContext(), "권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    !isGranted -> {
                        // 권한이 거부된 경우 처리할 작업
                        Toast.makeText(requireContext(), "권한이 거부되었습니다. 설정에서 권한을 승인해주세요.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // 사용자가 "다시 묻지 않음"을 선택한 경우 처리할 작업
                        Log.d("SearchFragment", "permission 다시묻지않음")
                    }
                }
            }
            // multiple permission 처리에 대한 선택적 작업
            // - 모두 허용되었을 경우에 대한 code
            // - 허용되지 않은 Permission에 대한 재요청 code
        }


    // util method
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun initMapView() {
        Log.d("SearchFragment", "지도 세팅")
        mapView = MapView(requireActivity())
        mapView?.setMapViewEventListener(this)
        sBinding?.mapMyMapcontainer?.addView(mapView)
    }

/*    private fun updatePostData(selectedLocation: LocationReadAllResponse?) {
        selectedLocation?.let {
            sBinding?.postData?.apply {
                Log.d("2222", "222222")
                visibility = View.VISIBLE
                sBinding?.postTitle?.text = selectedLocation.title
                sBinding?.postDate?.text = selectedLocation.targetDate + " " + selectedLocation.targetTime
                sBinding?.postLocation?.text = selectedLocation.location
                sBinding?.postParticipation?.text = selectedLocation.participantsCount.toString()
                sBinding?.postParticipationTotal?.text = selectedLocation.numberOfPassengers.toString()
            }
        }
    }*/


// ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
/*    private fun updateSearchResults(keyword: String) {
        val results: List<Content> = getSearchResults(keyword)

        // 어댑터 초기화 및 설정
        searchResultAdapter = SearchResultAdapter(keyword, results) { content ->
            // 아이템 클릭 시 동작
            showMarkerOnMap(content)
        }
        sBinding?.rvSearchList?.adapter = searchResultAdapter
    }

    // 검색 결과 데이터를 가져오는 메서드 (실제 구현에 따라 변경)
    private fun getSearchResults(keyword: String): List<Content> {
        // TODO: 실제 검색 로직 구현 (API 호출 또는 데이터베이스 쿼리 등)

        return emptyList()
    }

    // 지도에 마커 표시하는 메서드
    private fun showMarkerOnMap(content: Content) {
        // 빨간 마커로 표시할 장소의 좌표
        val mapPoint = MapPoint.mapPointWithGeoCoord(content.locationLat, content.locationLng)
        mapView.addPOIItem(createPOIItem("장소", mapPoint, true))

        // 1km 이내의 다른 게시글 위치 파란 마커로 표시하는 로직
        // TODO: 해당 부분의 구현 (다른 게시글을 가져와 마커 추가)
    }

    private fun createPOIItem(name: String, mapPoint: MapPoint, isRed: Boolean): MapPOIItem {
        val marker = MapPOIItem()
        marker.itemName = name
        marker.mapPoint = mapPoint
        marker.markerType = if (isRed) MapPOIItem.MarkerType.RedPin else MapPOIItem.MarkerType.BluePin
        return marker
    }*/
// ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
/*    private fun initRecentSearchRecyclerview() {
        swAdapter = SearchWordAdapter()
        swAdapter!!.searchWordData = searchWordList
        sBinding!!.searchRV.adapter = swAdapter
        sBinding!!.searchRV.layoutManager = LinearLayoutManager(sContext)
        sBinding!!.searchRV.setHasFixedSize(true)
    }*/

/*    override fun onPause() {
        super.onPause()

        Log.i("GGGGGGGGGGGGGGGGG", "gggggggggggggggggggggggggggg")
        // 상태바와 하단 네비게이션 바를 원래대로 복원
        (activity as? AppCompatActivity)?.supportActionBar?.show()

        activity?.window?.apply {
            // 예: 원래의 상태바 색상을 복원하는 코드. 적절한 색상으로 변경하세요.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                statusBarColor = resources.getColor(R.color.white, null)
            }
            clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

            // 원래의 UI 플래그를 설정하려면 아래의 코드를 수정하세요.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }

        // 네비게이션 바 마진 복원. 원래 마진 값을 설정하세요.
        val activity = activity as AppCompatActivity
        val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        val layoutParams = bottomNavigationView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = 0
            bottomNavigationView.layoutParams = layoutParams
    }*/

    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            sBinding?.mapMyMapcontainer?.removeAllViews()
            sBinding?.mapMyMapcontainer?.removeAllViewsInLayout()
            mapView = null

           /* mapView?.removeAllPOIItems()
            mapView?.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
            mapView?.setMapViewEventListener(null as MapView.MapViewEventListener?)
            sBinding?.editFirstVf?.removeView(mapView)
            mapView?.onPause()
            mapView?.onSurfaceDestroyed()
//            mapView.onStop()
//            mapView.onDestroy()
            mapView = null*/

            /*// 상태바와 하단 네비게이션 바를 원래대로 복원
            (activity as? AppCompatActivity)?.supportActionBar?.show()

            activity?.window?.apply {
                // 원래의 상태바 색상을 복원.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBarColor = resources.getColor(R.color.white, null)
                }
                clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

                // 원래의 UI 플래그를 설정
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
            }

            // 네비게이션 바 마진 복원.
            val activity = activity as AppCompatActivity
            val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            val layoutParams = bottomNavigationView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = 0
            bottomNavigationView.layoutParams = layoutParams*/
        }
    }


    override fun onStart() {
        super.onStart()
        Log.d("searchFragment1", "start")
        if (mapView == null) {
            initMapView()
        }
    }


    override fun onStop() {
        super.onStop()
        Log.d("SearchFragment onStop", "STOP")
        if (mapView == null) {
            mapView?.removeAllPOIItems()
            mapView?.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
            mapView?.setMapViewEventListener(null as MapView.MapViewEventListener?)
            sBinding?.editFirstVf?.removeView(mapView)
//            mapView.onStop()
//            mapView.onDestroy()
            mapView = null

            // 상태바와 하단 네비게이션 바를 원래대로 복원
            (activity as? AppCompatActivity)?.supportActionBar?.show()

            activity?.window?.apply {
                // 원래의 상태바 색상을 복원.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBarColor = resources.getColor(R.color.white, null)
                }
                clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

                // 원래의 UI 플래그를 설정
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
            }

            // 네비게이션 바 마진 복원.
            val activity = activity as AppCompatActivity
            val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            val layoutParams = bottomNavigationView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = 0
            bottomNavigationView.layoutParams = layoutParams
        }
    }

    override fun onDestroyView() {
        Log.d("GGGGGGGGGGGGGGGGG", "destroy")
        super.onDestroyView()
        if (mapView != null) {

            mapView?.removeAllPOIItems()
            mapView?.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
            mapView?.setMapViewEventListener(null as MapView.MapViewEventListener?)
            sBinding?.editFirstVf?.removeView(mapView)
            mapView = null

            // 상태바와 하단 네비게이션 바를 원래대로 복원
            (activity as? AppCompatActivity)?.supportActionBar?.show()

            activity?.window?.apply {
                // 원래의 상태바 색상을 복원.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBarColor = resources.getColor(R.color.white, null)
                }
                clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

                // 원래의 UI 플래그를 설정
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
            }

            // 네비게이션 바 마진 복원.
            val activity = activity as AppCompatActivity
            val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            val layoutParams = bottomNavigationView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = 0
            bottomNavigationView.layoutParams = layoutParams
        }
        mapView?.removeAllPOIItems() // 모든 마커 제거
        mapView?.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff // 현재 위치 추적 비활성화
        mapView?.mapType = MapView.MapType.Standard // 지도 타입을 기본값으로 설정
        //sBinding?.mapContainer?.removeAllViews() // 지도를 포함하는 컨테이너의 모든 뷰를 제거
        sBinding?.mapContainer?.removeView(mapView)
        //mapView.onDestroy() // 지도의 리소스를 해제*//*
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