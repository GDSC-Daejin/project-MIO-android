package com.example.mio.navigation

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mio.*
import com.example.mio.model.*
import com.example.mio.noticeboard.NoticeBoardReadActivity
import com.example.mio.R
import com.example.mio.tapsearch.NearbypostActivity
import com.example.mio.tapsearch.SearchResultActivity
import com.example.mio.databinding.FragmentSearchBinding
import com.example.mio.viewmodel.FragSharedViewModel
import com.example.mio.viewmodel.FragSharedViewModel2
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

    private var permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private var sBinding : FragmentSearchBinding? = null

    private lateinit var geocoder: Geocoder
    private var map : MapView? = null
    private var kakaoMapValue : KakaoMap? = null
    //private var infoWindow: InfoWindow? = null
    private var centerLabel: Label? = null
    //private val requestingLocationUpdates = false
    //private val locationRequest: LocationRequest? = null
    private var startPosition: LatLng? = null
    private var labelLayer: LabelLayer? = null

    private var hashMapPoiAndPostData : HashMap<String, LocationReadAllResponse>? = HashMap()
    //로딩창
    private var loadingDialog : LoadingProgressDialog? = null

    //private var eventListener : MarkerEventListener? = null   // 마커 클릭 이벤트 리스너

    val sharedViewModel: FragSharedViewModel by lazy {
        (activity?.application as FragSharedViewModel2).sharedViewModel
    }

    //private var pendingLocationData : LocationReadAllResponse? = null

    private var searchPostData : ArrayList<LocationReadAllResponse>? = ArrayList()


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

        multiplePermissionsLauncher.launch(permissions)

        geocoder = Geocoder(requireContext())

        sBinding?.btSearchField?.setOnClickListener {
/*            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_content, SearchResultFragment())
            //transaction.addToBackStack(null)
            transaction.commit()*/
            val intent = Intent(activity, SearchResultActivity::class.java)
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
        sBinding?.btSearchField?.hint = location.location

        kakaoMapValue?.labelManager?.layer?.let { _ ->
            // Add code to display the selected post on the map
        } ?: Log.e("DEBUG", "Label Manager layer is null")

        sBinding?.postData?.apply {
            visibility = View.VISIBLE
            sBinding?.postTitle?.text = location.title
            sBinding?.postDate?.text = getString(R.string.setDateText2, location.targetDate, location.targetTime)//"${location.targetDate} ${location.targetTime}"
            sBinding?.postLocation?.text = location.location
            sBinding?.postParticipation?.text = location.participantsCount.toString()
            sBinding?.postParticipationTotal?.text = location.numberOfPassengers.toString()

            sBinding?.postData?.setOnClickListener {
                val postData = PostData(
                    accountID = location.user?.studentId ?: "",
                    postID = location.postId,
                    postTitle = location.title,
                    postContent = location.content,
                    postCreateDate = location.createDate,
                    postTargetDate = location.targetDate,
                    postTargetTime = location.targetTime,
                    postCategory = location.category?.categoryName ?: "",
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
                    putExtra("postItem", postData)
                }
                startActivity(intent)
            }
        }

        sBinding?.postMore?.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                val intent = Intent(context, NearbypostActivity::class.java).apply {
                    putExtra("POST_ID", location.postId)
                    putExtra("searchWord", location.location)
                }
                startActivity(intent)
            }
        }
    }

    private fun loadSearchPost(location : String?) {
        RetrofitServerConnect.create(requireActivity()).getLocationPostData(location!!).enqueue(object : Callback<List<LocationReadAllResponse>> {
            override fun onResponse(
                call: Call<List<LocationReadAllResponse>>,
                response: Response<List<LocationReadAllResponse>>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()

                    if (responseData?.isNotEmpty() == true) {
                        responseData.let {
                            searchPostData?.clear()
                            searchPostData?.addAll(it.filter { it1 -> it1.isDeleteYN == "N" && it1.postType == "BEFORE_DEADLINE" })
                        }
                        startMapLifeCycle()
                    } else {
                        requireActivity().runOnUiThread {
                            if (isAdded && !requireActivity().isFinishing) {
                                Toast.makeText(requireContext(), "검색된 게시글이 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    loadingDialog?.dismiss()
                    if (loadingDialog != null && loadingDialog!!.isShowing) {
                        loadingDialog?.dismiss()
                        loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                    }
                } else {
                    requireActivity().runOnUiThread {
                        if (isAdded && !requireActivity().isFinishing) {
                            loadingDialog?.dismiss()
                            Toast.makeText(requireContext(), "검색에 실패하였습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<LocationReadAllResponse>>, t: Throwable) {
                requireActivity().runOnUiThread {
                    if (isAdded && !requireActivity().isFinishing) {
                        loadingDialog?.dismiss()
                        Toast.makeText(requireContext(), "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    // 주변 게시글 데이터를 불러오는 함수
    private fun loadNearbyPostData(postId: Int?) {
        if (postId != null) {
            RetrofitServerConnect.create(requireContext()).getNearByPostData(postId).enqueue(object :
                Callback<List<LocationReadAllResponse>> {
                override fun onResponse(call: Call<List<LocationReadAllResponse>>, response: Response<List<LocationReadAllResponse>>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()
                        if (responseData.isNullOrEmpty()) {
                            Toast.makeText(requireActivity(), "검색된 주위 게시글이 없습니다", Toast.LENGTH_SHORT).show()
                        } else {
                            val selectedData = responseData.firstOrNull { it.postId == postId }
                            if (selectedData != null) {
                                displaySelectedPostOnMap(selectedData)
                            }
                            for (i in responseData.filter { it.postId != postId && it.isDeleteYN == "N" && it.postType == "BEFORE_DEADLINE"}) {
                                val style = kakaoMapValue?.labelManager?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.map_poi_srn)))
                                val options = LabelOptions.from("${i.postId}", LatLng.from(i.latitude, i.longitude)).setStyles(style)
                                val layer = kakaoMapValue?.labelManager?.layer
                                layer?.addLabel(options)
                                if (layer?.layerId != null) {
                                    hashMapPoiAndPostData?.set(options.labelId, i)
                                }
                            }
                        }
                    } else {
                        Log.d("comment", response.errorBody()?.string() ?: "Unknown error")
                        Log.d("message", call.request().toString())
                        Log.d("response code", response.code().toString())
                        loadingDialog?.dismiss()
                        Toast.makeText(requireActivity(), "주위 게시글 검색에 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<LocationReadAllResponse>>, t: Throwable) {
                    loadingDialog?.dismiss()
                    Toast.makeText(requireActivity(), "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

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
                initMapView()
            } catch (re: RuntimeException) {
                Log.e("SearchFragment", re.toString())
            }
        } else {
            map = null
            initMapView()
        }
    }
    private fun startMapLifeCycle() {
        map?.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(error: Exception?) {
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

                kakaoMapValue = kakaoMap
                labelLayer = kakaoMap.labelManager!!.layer
                val trackingManager = kakaoMap.trackingManager

                //선택한 값을 중심으로 poi찍기
                if (searchPostData?.isNotEmpty() == true) {
                    loadNearbyPostData(searchPostData?.first()?.postId)
                    // 현재 위치를 나타낼 label를 그리기 위해 kakaomap 인스턴스에서 LabelLayer를 가져옵니다.
                    val layer = kakaoMap.labelManager!!.layer
                    startPosition = LatLng.from(searchPostData?.first()?.latitude!!, searchPostData?.first()?.longitude!!)
                    // LabelLayer에 라벨을 추가합니다. 카카오 지도 API 공식 문서에 지도에서 사용하는 이미지는 drawable-nodpi/ 에 넣는 것을 권장합니다.
                    //Label 을 생성하기 위해 초기화 값을 설정하는 클래스.
                    centerLabel = layer!!.addLabel(
                        LabelOptions.from(searchPostData?.first()?.postId.toString(), startPosition)
                            .setStyles(
                                LabelStyle.from(R.drawable.map_poi_sr2).setAnchorPoint(0.5f, 0.5f)
                            )
                            .setRank(1) //우선순위
                    )
                    trackingManager!!.startTracking(centerLabel)
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed( {
                        trackingManager.stopTracking()
                    },1000)

                    hashMapPoiAndPostData?.set(searchPostData?.first()?.postId.toString(),
                        searchPostData?.first()!!
                    )

                    if (searchPostData?.isNotEmpty() == true) {
                        if (searchPostData?.size!! > 1) {
                            sBinding?.postMoreCount?.visibility = View.VISIBLE
                            sBinding?.postMoreCount?.text = getString(R.string.setSearchPostCount, "${searchPostData?.size}")//"+${searchPostData?.size}"
                        }
                    }
                }

                kakaoMapValue!!.setOnMapClickListener { _, _, _, _ ->
                }

                kakaoMapValue!!.setOnPoiClickListener { _, _, _, poiId ->
                    val setPostData = hashMapPoiAndPostData?.get(poiId)
                    if (setPostData != null) {
                        kakaoMap.labelManager?.removeAllLabelLayer()
                        if (hashMapPoiAndPostData?.isNotEmpty() == true) {
                            if (hashMapPoiAndPostData!!.filter { it.value.location == setPostData.location }.size > 1) {
                                sBinding?.postMoreCount?.visibility = View.VISIBLE
                                sBinding?.postMoreCount?.text = getString(R.string.setSearchPostCount, "${hashMapPoiAndPostData!!.filter { it.value.location == setPostData.location }.size}")//"+${hashMapPoiAndPostData!!.filter { it.value.location == setPostData.location }.size}"
                            } else {
                                sBinding?.postMoreCount?.visibility = View.GONE
                                //sBinding?.postMoreCount?.text = "+${hashMapPoiAndPostData!!.filter { it.value.location == setPostData.location }.size}"
                                sBinding?.postMoreCount?.visibility = View.GONE
                            }
                        }
                        // 현재 위치를 나타낼 label를 그리기 위해 kakaomap 인스턴스에서 LabelLayer를 가져옵니다.
                        val layer = kakaoMap.labelManager!!.layer
                        startPosition = LatLng.from(setPostData.latitude, setPostData.longitude)
                        // LabelLayer에 라벨을 추가합니다. 카카오 지도 API 공식 문서에 지도에서 사용하는 이미지는 drawable-nodpi/ 에 넣는 것을 권장합니다.
                        //Label 을 생성하기 위해 초기화 값을 설정하는 클래스.
                        centerLabel = layer!!.addLabel(
                            LabelOptions.from(setPostData.postId.toString(), startPosition)
                                .setStyles(
                                    LabelStyle.from(R.drawable.map_poi_sr2).setAnchorPoint(0.5f, 0.5f)
                                )
                                .setRank(1) //우선순위
                        )
                        trackingManager!!.startTracking(centerLabel)
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed( {
                            trackingManager.stopTracking()
                        },1000)

                        for (i in hashMapPoiAndPostData!!.filter { it.key != setPostData.postId.toString() }) {
                            //latLngList.add(LatLng.from(i.latitude, i.longitude))
                            //labelLayer.addLabel(LabelOptions.from("centerLabel", centerPosition)
                            // 스타일 지정. LabelStyle.from()안에 원하는 이미지 넣기
                            val style = kakaoMapValue?.labelManager?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.map_poi_srn)))
                            // 라벨 옵션 지정. 위경도와 스타일 넣기
                            val options = LabelOptions.from(i.key, LatLng.from(i.value.latitude, i.value.longitude)).setStyles(style)
                            // 레이어 가져오기
                            //val layer = kakaoMap.labelManager?.layer
                            // 레이어에 라벨 추가
                            layer.addLabel(options)
                            //Log.d("requestActivity loadNearbyPostData", "$i")
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            sBinding?.postData?.visibility = View.VISIBLE
                            sBinding?.postTitle?.text = setPostData.title
                            sBinding?.postDate?.text = getString(R.string.setDateText2, setPostData.targetDate, setPostData.targetTime)//setPostData.targetDate + " " + setPostData.targetTime
                            sBinding?.postLocation?.text = setPostData.location
                            sBinding?.postParticipation?.text = setPostData.participantsCount.toString()
                            sBinding?.postParticipationTotal?.text = setPostData.numberOfPassengers.toString()
                        }
                        sBinding?.postData?.setOnClickListener {
/*                val intent = Intent(context, NoticeBoardReadActivity::class.java)
                intent.putExtra("POST_ID", location.postId)  // 게시글의 ID 또는 유일한 키를 전달
                startActivity(intent)*/
                            val postData = PostData(
                                accountID = setPostData.user?.studentId!!,
                                postID = setPostData.postId,
                                postTitle = setPostData.title,
                                postContent = setPostData.content,
                                postCreateDate = setPostData.createDate,
                                postTargetDate = setPostData.targetDate,
                                postTargetTime = setPostData.targetTime,
                                postCategory = setPostData.category?.categoryName!!,
                                postLocation = setPostData.location,
                                postParticipation = setPostData.participantsCount,
                                postParticipationTotal = setPostData.numberOfPassengers,
                                postCost = setPostData.cost,
                                postVerifyGoReturn = setPostData.verifyGoReturn,
                                user = setPostData.user!!,
                                postlatitude = setPostData.latitude,
                                postlongitude = setPostData.longitude
                            )

                            // Intent를 통해 NoticeBoardReadActivity로 전달
                            val intent = Intent(context, NoticeBoardReadActivity::class.java)
                            intent.putExtra("type", "READ")
                            intent.putExtra("postItem", postData)
                            startActivity(intent)
                        }
                    }
                }

                //KakaoMap kakaoMap, LabelLayer layer, Label label
                kakaoMapValue!!.setOnLabelClickListener { _, _, label ->
                    return@setOnLabelClickListener label == null
                }
            }
        })
    }

    // util method
    /*private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }*/

    private fun initMapView() {
        map = sBinding?.mapView
    }



    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // API 30 이하와 이상을 나누기 위한 조건문
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // API 30 이하의 경우
                val data = result.data?.getParcelableExtra<LocationReadAllResponse>("location")
                val flag = result.data?.getIntExtra("flag", -1)

                if (flag == 103) {
                    // 로딩창 실행
                    loadingDialog = LoadingProgressDialog(activity).apply {
                        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        window?.attributes?.windowAnimations = R.style.FullScreenDialog
                        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        show()
                    }
                    map?.finish()
                    kakaoMapValue = null
                    initMapView()
                    CoroutineScope(Dispatchers.IO).launch {
                        data?.let {
                            loadSearchPost(it.location)
                        }
                    }
                }
            } else {
                val data = result.data?.getParcelableExtra("location", LocationReadAllResponse::class.java)
                val flag = result.data?.getIntExtra("flag", -1)

                if (flag == 103) {
                    // 로딩창 실행
                    loadingDialog = LoadingProgressDialog(activity).apply {
                        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        window?.attributes?.windowAnimations = R.style.FullScreenDialog
                        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        show()
                    }
                    map?.finish()
                    kakaoMapValue = null
                    initMapView()
                    CoroutineScope(Dispatchers.IO).launch {
                        data?.let {
                            loadSearchPost(it.location)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startMapLifeCycle()
    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onPause() {
        super.onPause()
        map?.pause()
        if (map != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.show()

            activity?.window?.apply {
                // 원래의 상태바 색상을 복원.
                statusBarColor = resources.getColor(R.color.white, null)
                clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

                // 원래의 UI 플래그를 설정
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // API 30 이상에서는 WindowInsetsController 사용
                    val windowInsetsController = requireActivity().window.insetsController
                    windowInsetsController?.show(WindowInsets.Type.systemBars())
                } else {
                    // API 30 미만에서는 기존 방식 사용
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
        map?.finish()
        map = null
    }

    companion object {
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