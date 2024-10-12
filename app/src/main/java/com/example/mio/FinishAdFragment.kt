package com.example.mio

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.mio.databinding.FragmentFinishAdBinding
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class FinishAdFragment(context: Context, finishAdInterface: FinishAdInterface) : DialogFragment() {

    private var _binding: FragmentFinishAdBinding? = null
    private val binding get() = _binding!!
    private var adLoader: AdLoader? = null
    private var finishAdInterface: FinishAdInterface? = null
    private val contextT = context
    private var loadedAd: NativeAd? = null

    init {
        this.finishAdInterface = finishAdInterface
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinishAdBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.adBtnLl.visibility = View.GONE
        createAd()

        // 취소 버튼 클릭
        binding.dialogLeftBtn.setOnClickListener {
            dismiss()
        }

        // 확인 버튼 클릭
        binding.dialogRightBtn.setOnClickListener {
            this.finishAdInterface?.onYesButtonClick()
            dismiss()
        }

        return binding.root
    }

    private fun createAd() {
        MobileAds.initialize(contextT)
        adLoader = AdLoader.Builder(contextT, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad: NativeAd ->
                loadedAd = ad // 광고가 로드되면 이를 저장
                val adView = binding.nativeAdView
                populateNativeAd(adView, ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // 광고 로드 실패 시 기본 동작 수행 (예: 기본 UI 표시)
                    binding.nativeAdView.visibility = View.VISIBLE
                    //binding.defaultAdView.visibility = View.VISIBLE
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .build()
            )
            .build()

        adLoader?.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAd(adView: NativeAdView, ad: NativeAd) {
        val icon = adView.findViewById<ImageView>(R.id.adAppIcon)
        val mediaView = adView.findViewById<MediaView>(R.id.adMedia)
        val headline = adView.findViewById<TextView>(R.id.adHeadline)

        // 광고에 필요한 필수 요소들 초기화
        if (icon != null && ad.icon != null) {
            icon.setImageDrawable(ad.icon?.drawable)
            adView.iconView = icon
        }

        if (mediaView != null && ad.mediaContent != null) {
            mediaView.mediaContent = ad.mediaContent
            adView.mediaView = mediaView
        }

        if (headline != null && ad.headline != null) {
            headline.text = ad.headline
            adView.headlineView = headline
        }

        // 광고를 뷰에 설정
        adView.setNativeAd(ad)

        binding.adBtnLl.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        loadedAd?.destroy() // 광고 객체가 사용 후 메모리에서 해제되도록 설정
    }
}

interface FinishAdInterface {
    fun onYesButtonClick()
}
