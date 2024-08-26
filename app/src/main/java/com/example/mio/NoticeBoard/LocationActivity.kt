package com.example.mio.NoticeBoard

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mio.R
import com.example.mio.databinding.ActivityLocationBinding
import com.kakao.vectormap.*
import com.kakao.vectormap.label.*


class LocationActivity : AppCompatActivity() {
    private lateinit var lBinding : ActivityLocationBinding
    private var mapView: MapView? = null
    private var kakaoMapValue : KakaoMap? = null
    private var labelLayer: LabelLayer? = null
    private var startPosition: LatLng? = null
    private var centerLabel: Label? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lBinding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(lBinding.root)

        initMap()
    }

    private fun initMap() {
        mapView = lBinding.mapView
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        startPosition = LatLng.from(latitude, longitude)
    }

    private fun startMapLifeCycle() {
        mapView?.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.e("LocationActivity", "onMapDestroy")
            }

            override fun onMapError(error: Exception?) {
                Log.e("LocationActivity", "onMapError", error)
            }

        }, object : KakaoMapReadyCallback() {
            override fun getPosition(): LatLng {
                return super.getPosition()
            }

            override fun getZoomLevel(): Int {
                return 17
            }

            override fun onMapReady(kakaoMap: KakaoMap) {
                Log.e("LocationActivity", "onMapReady")
                kakaoMapValue = kakaoMap
                labelLayer = kakaoMap.labelManager!!.layer
                val trackingManager = kakaoMap.trackingManager
                // 스타일 지정
                val style = kakaoMap.labelManager?.addLabelStyles(
                    LabelStyles.from(
                        LabelStyle.from(R.drawable.map_poi_black_icon).apply {
                            setAnchorPoint(0.5f, 0.5f)
                            isApplyDpScale = true
                        }
                    )
                )

                // 라벨 옵션 지정
                val options = LabelOptions.from(startPosition).setStyles(style)
                if (startPosition != null) {
                    centerLabel = labelLayer!!.addLabel(options)

                    trackingManager!!.startTracking(centerLabel)
                }

                /*kakaoMapValue!!.setOnMapClickListener { _, latLng, _, poi ->
                    //showInfoWindow(position, poi)
                    trackingManager?.stopTracking()

                    if (poi.name.isNotEmpty()) {
                        labelLatLng = LatLng.from(latLng.latitude, latLng.longitude)
                        val style = kakaoMap.labelManager?.addLabelStyles(
                            LabelStyles.from(
                                LabelStyle.from(com.kakao.vectormap.R.drawable.map_poi_icon).apply {
                                    setAnchorPoint(0.5f, 0.5f)
                                    isApplyDpScale = false
                                }
                            )
                        )
                        val options = LabelOptions.from(labelLatLng).setStyles(style).setRank(1)
                        val label = labelLayer?.addLabel(options)
                        label?.let { trackingManager?.startTracking(it) }
                    }
                }*/

                /*kakaoMapValue!!.setOnLabelClickListener { _, _, label ->
                    trackingManager?.startTracking(label)
                }*/
            }
        })
    }

    override fun onStart() {
        super.onStart()
        Log.e("LocationActivity", "onSTart")
    }

    override fun onResume() {
        super.onResume()
        Log.e("LocationActivity", "onREsume")
        startMapLifeCycle()
    }

    override fun onPause() {
        super.onPause()
        Log.e("LocationActivity", "onpause")
        mapView?.pause()
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.e("LocationActivity", "onDestory")
        mapView?.finish()
        mapView = null
    }
}