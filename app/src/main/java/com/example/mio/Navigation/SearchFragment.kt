package com.example.mio.Navigation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.mio.*
import com.example.mio.BuildConfig
import com.example.mio.Model.*
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.R
import com.example.mio.TapSearch.NearbypostActivity
import com.example.mio.TapSearch.SearchResultActivity
import com.example.mio.databinding.FragmentSearchBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.kakao.vectormap.*
import com.kakao.vectormap.label.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private var sBinding : FragmentSearchBinding? = null

    private lateinit var geocoder: Geocoder
    private var map : com.kakao.vectormap.MapView? = null
    private var kakaoMapValue : KakaoMap? = null
    //private var infoWindow: InfoWindow? = null
    private var centerLabel: Label? = null
    //private val requestingLocationUpdates = false
    //private val locationRequest: LocationRequest? = null
    private var startPosition: LatLng? = null
    private var labelLayer: LabelLayer? = null
    //주위 게시글
    private var latLngList = ArrayList<LatLng>()
    private var labelLatLng : LatLng? = null
    //전에 찍은 라벨
    private var preLabel : Label? = null
    //
    private var isGrantedCheck : Boolean? = null

    //private var eventListener : MarkerEventListener? = null   // 마커 클릭 이벤트 리스너

    val sharedViewModel: FragSharedViewModel by lazy {
        (activity?.application as FragSharedViewModel2).sharedViewModel
    }

    private var pendingLocationData : LocationReadAllResponse? = null


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
            Log.e("searchresultintent", "click start")
            //map?.pause()
            requestActivity.launch(intent)
        }



        /*mapView?.setPOIItemEventListener(object : MapView.POIItemEventListener {
            override fun onPOIItemSelected(mapView: MapView, poiItem: MapPOIItem) {
                val selectedPost = poiItem.userObject as? LocationReadAllResponse
                if (selectedPost == null) {
                    Log.d("DEBUG", "poiItem.userObject is not of type LocationReadAllResponse")
                } else {
                    Log.d("DEBUG", "Selected post after casting: $selectedPost")
                    displaySelectedPostOnMap(selectedPost)
                }
*//*                selectedPost?.let {
                    sharedViewModel.selectedLocation.value = it
                    Log.d("11111111", "1111" + sharedViewModel.selectedLocation.value)
                }*//*
            }

            override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView, poiItem: MapPOIItem) {}
            override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView, poiItem: MapPOIItem, buttonType: MapPOIItem.CalloutBalloonButtonType) {}
            override fun onDraggablePOIItemMoved(mapView: MapView, poiItem: MapPOIItem, newMapPoint: MapPoint) {}
        })*/


        return sBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initMapView()
        } else {
            map?.resume()
        }
    }

    private fun displaySelectedPostOnMap(location: LocationReadAllResponse) {
        Log.d("DEBUG", "displaySelectedPostOnMap triggered with location: $location")
        /*val selectedLatLng = MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude)

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
*/
        sBinding?.btSearchField?.hint = location.location

        labelLayer = kakaoMapValue?.labelManager!!.layer
        // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
        // 주변 게시글 데이터를 불러옵니다.
        //loadNearbyPostData(location.postId)
        // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ


        sBinding?.postData?.apply {
            visibility = View.VISIBLE
            sBinding?.postTitle?.text = location.title
            sBinding?.postDate?.text = location.targetDate + " " + location.targetTime
            sBinding?.postLocation?.text = location.location
            sBinding?.postParticipation?.text = location.participantsCount.toString()
            sBinding?.postParticipationTotal?.text = location.numberOfPassengers.toString()

            sBinding?.postLl?.setOnClickListener {
/*                val intent = Intent(context, NoticeBoardReadActivity::class.java)
                intent.putExtra("POST_ID", location.postId)  // 게시글의 ID 또는 유일한 키를 전달
                startActivity(intent)*/
                val postData = PostData(
                    accountID = location.user?.studentId!!,
                    postID = location.postId,
                    postTitle = location.title,
                    postContent = location.content,
                    postTargetDate = location.targetDate,
                    postTargetTime = location.targetTime,
                    postCategory = location.category?.categoryName!!,
                    postLocation = location.location,
                    postParticipation = location.participantsCount,
                    postParticipationTotal = location.numberOfPassengers,
                    postCost = location.cost,
                    postVerifyGoReturn = location.verifyGoReturn,
                    user = location.user!!,
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
                val intent = Intent(context, NearbypostActivity::class.java).apply {
                    putExtra("POST_ID", location.postId)
                    putExtra("searchWord", location.location)
                }
                startActivity(intent)

            }
        }
    }


    // 주변 게시글 데이터를 불러오는 함수
    private fun loadNearbyPostData(postId: Int) {
        Log.d("requestActivity loadNearbyPostData", "loadNearbyPostData")
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getNearByPostData(postId).enqueue(object :
                Callback<List<LocationReadAllResponse>> {
                override fun onResponse(call: Call<List<LocationReadAllResponse>>, response: Response<List<LocationReadAllResponse>>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()
                        if (responseData.isNullOrEmpty()) {
                            Toast.makeText(requireActivity(), "검색된 게시글이 없습니다", Toast.LENGTH_SHORT).show()
                        } else {
                            val selectedData = responseData.first { it.postId == postId }
                            // 마커로 선택된 게시글 위치 표시
                            displaySelectedPostOnMap(selectedData)
                            //addMarker(response.body()!!) // 지도에 마커 추가 메소드 호출
                            /*if (latLngList.isNotEmpty()) {

                            }*/
                            for (i in response.body()!!.filter { it.postId != postId }) {
                                //latLngList.add(LatLng.from(i.latitude, i.longitude))
                                // 스타일 지정. LabelStyle.from()안에 원하는 이미지 넣기
                                val style = kakaoMapValue?.labelManager?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.map_poi_icon)))
                                // 라벨 옵션 지정. 위경도와 스타일 넣기
                                val options = LabelOptions.from(LatLng.from(i.latitude, i.longitude)).setStyles(style)
                                // 레이어 가져오기
                                val layer = kakaoMapValue?.labelManager?.layer
                                // 레이어에 라벨 추가
                                layer?.addLabel(options)
                            }
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
    private fun addMarker(locationList: List<LocationReadAllResponse>) {
        //val latLng = LatLng.from(location.latitude, location.longitude)
        //val labelLayer = kakaoMapValue?.labelManager?.layer
        for (i in locationList) {
            latLngList.add(LatLng.from(i.latitude, i.longitude))
        }
    }

    private fun showInfoWindow(poi: Poi?, pointF: PointF) {
        if (poi != null) {
            // TextView에 POI 정보를 설정
            sBinding?.poiInfoText?.text = poi.name

            // dp 단위를 픽셀로 변환
            val offsetY = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics
            ).toInt()

            // TextView의 위치를 화면 좌표로 설정
            sBinding?.poiInfoText?.x = pointF.x
            sBinding?.poiInfoText?.y = pointF.y - sBinding?.poiInfoText!!.height - offsetY

            // TextView를 화면에 표시
            sBinding?.poiInfoText?.visibility = View.VISIBLE
        }
    }

    /*class MarkerEventListener(val context: Context, val location: LocationReadAllResponse): MapView.POIItemEventListener {
        override fun onPOIItemSelected(mapView: MapView?, poiItem: MapPOIItem?) {
            // 마커 클릭 시
        }

        override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, poiItem: MapPOIItem?) {
            // 말풍선 클릭 시 (Deprecated)
            // 이 함수도 작동하지만 그냥 아래 있는 함수에 작성하자
        }

        override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, poiItem: MapPOIItem?, buttonType: MapPOIItem.CalloutBalloonButtonType?) {
            val temp = PostData(
                accountID = location.user?.studentId!!,
                postID = location.postId,
                postTitle = location.title,
                postContent = location.content,
                postTargetDate = location.targetDate,
                postTargetTime = location.targetTime,
                postCategory = location.category?.categoryName!!,
                postLocation = location.location,
                postParticipation = location.participantsCount,
                postParticipationTotal = location.numberOfPassengers,
                postCost = location.cost,
                postVerifyGoReturn = location.verifyGoReturn,
                user = location.user!!,
                postlatitude = location.latitude,
                postlongitude = location.longitude
            )

            val intent = Intent(context, NoticeBoardReadActivity::class.java).apply {
                putExtra("type", "READ")
                putExtra("postItem", temp)
                putExtra("uri", temp.user.profileImageUrl)
            }
            context.startActivity(intent)
        }

        override fun onDraggablePOIItemMoved(mapView: MapView?, poiItem: MapPOIItem?, mapPoint: MapPoint?) {
            // 마커의 속성 중 isDraggable = true 일 때 마커를 이동시켰을 경우
        }
    }*/

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


    /*private val multiplePermissionsLauncher  =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { (permission, isGranted) ->
                if (isGranted) {
                    Toast.makeText(requireContext(), "권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
                    if (map == null) {
                        try {
                            // 다시 맵뷰 초기화 및 추가
                            Log.e("SearchFragment", "try")
                            initMapView()
                        } catch (re: RuntimeException) {
                            Log.e("SearchFragment", re.toString())
                        }
                    } else {
                        Log.e("SearchFragment", "map is not null")
                        map = null
                        initMapView()
                        //map?.finish()
                    }
                } else {
                    Toast.makeText(requireContext(), "$permission 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            // multiple permission 처리에 대한 선택적 작업
            // - 모두 허용되었을 경우에 대한 code
            // - 허용되지 않은 Permission에 대한 재요청 code
        }*/
    private val multiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedPermissions = permissions.filter { !it.value }.keys
            val grantedPermissions = permissions.filter { it.value }.keys

            if (grantedPermissions.isNotEmpty()) {
                Toast.makeText(requireContext(), "권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
                initializeMapIfNeeded()
            }

            if (deniedPermissions.isNotEmpty()) {
                Toast.makeText(requireContext(), "${deniedPermissions.joinToString(", ")} 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                // 권한 재요청 코드
                requestDeniedPermissionsAgain(deniedPermissions)
            }
        }
    private fun requestDeniedPermissionsAgain(deniedPermissions: Set<String>) {
        // 권한 재요청을 위한 대화상자 또는 로직 구현
        AlertDialog.Builder(requireContext())
            .setTitle("권한 요청")
            .setMessage("이 기능을 사용하려면 권한이 필요합니다. 권한을 승인하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                multiplePermissionsLauncher.launch(deniedPermissions.toTypedArray())
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()
                handlePermissionDenied()
            }
            .create()
            .show()
    }
    private fun handlePermissionDenied() {
        // 권한이 거부된 경우 지도 뷰를 숨기거나 처리하는 코드 추가
        sBinding?.mapView?.visibility = View.GONE
        Toast.makeText(requireContext(), "권한이 거부되어 지도를 표시할 수 없습니다.", Toast.LENGTH_SHORT).show()
    }
    private fun initializeMapIfNeeded() {
        if (map == null) {
            try {
                Log.e("SearchFragment", "try")
                initMapView()
            } catch (re: RuntimeException) {
                Log.e("SearchFragment", re.toString())
            }
        } else {
            Log.e("SearchFragment", "map is not null")
            map = null
            initMapView()
        }
    }
    private fun startMapLifeCycle() {
        map?.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.e("searchFragment1", "onMapDestroy")
            }

            override fun onMapError(error: Exception?) {
                Log.e("searchFragment1", "onMApError", error)
            }

        }, object : KakaoMapReadyCallback() {
            override fun getPosition(): LatLng {
                //startPosition = super.getPosition()
                return super.getPosition()
            }

            override fun getZoomLevel(): Int {
                return 17
            }

            override fun onMapReady(kakaoMap: KakaoMap) {
                Log.e("searchFragment1", "onMapReady")
                kakaoMapValue = kakaoMap
                labelLayer = kakaoMap.labelManager!!.layer
                val trackingManager = kakaoMap.trackingManager

                //선택한 값을 중심으로 poi찍기
                if (pendingLocationData != null) {
                    Log.e("pendingTest", pendingLocationData.toString())
                    loadNearbyPostData(pendingLocationData!!.postId)
                    // 현재 위치를 나타낼 label를 그리기 위해 kakaomap 인스턴스에서 LabelLayer를 가져옵니다.
                    val layer = kakaoMap.labelManager!!.layer
                    startPosition = LatLng.from(pendingLocationData!!.latitude, pendingLocationData!!.longitude)
                    // LabelLayer에 라벨을 추가합니다. 카카오 지도 API 공식 문서에 지도에서 사용하는 이미지는 drawable-nodpi/ 에 넣는 것을 권장합니다.
                    //Label 을 생성하기 위해 초기화 값을 설정하는 클래스.
                    centerLabel = layer!!.addLabel(
                        LabelOptions.from("centerLabel", startPosition)
                            .setStyles(
                                LabelStyle.from(R.drawable.map_poi_icon).setAnchorPoint(0.5f, 0.5f)
                            )
                            .setRank(1) //우선순위
                    )
                    trackingManager!!.startTracking(centerLabel)
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed(java.lang.Runnable {
                        trackingManager.stopTracking()
                    },1000)
                }
                //trackingManager?.stopTracking()



                kakaoMapValue!!.setOnMapClickListener { kakaoMap, latLng, pointF, poi ->
                    // POI 정보창 표시
                    //showInfoWindow(position, poi)
                    showInfoWindow(poi, pointF)

                    Log.e("kakaoMapValue", "$latLng $pointF ${poi.name}")

                    if (poi.name.isNotEmpty()) {
                        if (preLabel == null) { //눌러진 poi 또는 label이없으면
                            labelLatLng = LatLng.from(latLng.latitude, latLng.longitude)
                            Log.e("labelLatLng", "$labelLatLng")

                            // 레이어 가져오기
                            val labelLayer = kakaoMap.labelManager?.layer

                            // 스타일 지정
                            val style = kakaoMap.labelManager?.addLabelStyles(
                                LabelStyles.from(
                                    LabelStyle.from(R.drawable.map_poi_sr2).apply {
                                        setAnchorPoint(0.5f, 0.5f)
                                        isApplyDpScale = false
                                    }
                                )
                            )

                            // 라벨 옵션 지정
                            val options = LabelOptions.from(labelLatLng).setStyles(style)

                            // 라벨 추가
                            val label = labelLayer?.addLabel(options)
                            preLabel = label

                            // 라벨로 트래킹 시작
                            if (label != null) {
                                trackingManager?.startTracking(label)
                                val handler = Handler(Looper.getMainLooper())
                                handler.postDelayed(java.lang.Runnable {
                                    trackingManager?.stopTracking()
                                },1000)
                            } else {
                                Log.e("kakaoMapValue", "Label is null, tracking cannot be started.")
                            }
                        } else { //만약 누른 poi나 label이 있다면? 지우고 그리기
                            // 레이어 가져오기
                            val labelLayer = kakaoMap.labelManager?.layer
                            // 스타일 지정
                            val style = kakaoMap.labelManager?.addLabelStyles(
                                LabelStyles.from(
                                    LabelStyle.from(R.drawable.map_poi_sr2).apply {
                                        setAnchorPoint(0.5f, 0.5f)
                                        isApplyDpScale = false
                                    }
                                )
                            )
                            labelLatLng = LatLng.from(latLng.latitude, latLng.longitude)
                            Log.e("labelLatLng", "$labelLatLng")
                            //이전 값 지우고?
                            labelLayer?.remove(preLabel)
                            // 라벨 옵션 지정
                            val options = LabelOptions.from(labelLatLng).setStyles(style)
                            // 라벨 추가
                            val label = labelLayer?.addLabel(options)
                            // 라벨로 트래킹 시작
                            if (label != null) {
                                trackingManager?.startTracking(label)
                                val handler = Handler(Looper.getMainLooper())
                                handler.postDelayed(java.lang.Runnable {
                                    trackingManager?.stopTracking()
                                },1000)
                            } else {
                                Log.e("kakaoMapValue", "Label is null, tracking cannot be started.")
                            }
                        }
                    }
                }

                kakaoMapValue!!.setOnLabelClickListener { kakaoMap, labelLayer, label ->
                    //trackingManager.stopTracking()
                    trackingManager?.startTracking(label)
                }

                kakaoMapValue!!.setOnCameraMoveStartListener { kakaoMap, gestureType ->
                    sBinding?.poiInfoText?.visibility = View.GONE
                }
            }
        })
    }

    // util method
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun initMapView() {
        Log.d("SearchFragment", "지도 세팅")
        map = sBinding?.mapView
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data?.getSerializableExtra("location") as LocationReadAllResponse?
            val flag = result.data?.getIntExtra("flag", -1)
            if (flag == 103) {
                CoroutineScope(Dispatchers.IO).launch {
                    // 맵을 다시 초기화하거나 필요한 작업 수행
                    //initMapView()

                    Log.d("requestActivity couroutine", "initMapView")
                    if (data != null) {
                        pendingLocationData = data
                    }
                }
                /*sharedViewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
                    Log.d("DEBUG", "LiveData observed: $location")
                    location?.let {

                        loadNearbyPostData(location.postId)

                    }
                }*/
            }
        }
    }
    override fun onStart() {
        super.onStart()
        Log.d("searchFragment1", "start")
    }

    override fun onResume() {
        super.onResume()
        Log.d("searchFragment1", "resume")
        map?.resume()
        startMapLifeCycle()
    }


    override fun onPause() {
        super.onPause()
        map?.pause()
        Log.d("searchFragment1", "pause")
        if (map != null) {
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
            // 그림자 효과 없애기
            bottomNavigationView.background = MaterialShapeDrawable().apply {
                // 배경색을 투명하게 설정하여 그림자 효과를 없앱니다.
                setTint(Color.TRANSPARENT)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("searchfragment1", "onDestroy")
        map?.finish()
        map = null
    }

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
}