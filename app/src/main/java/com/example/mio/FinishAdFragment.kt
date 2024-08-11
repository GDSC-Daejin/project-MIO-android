package com.example.mio

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.mio.databinding.FragmentFinishAdBinding
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FinishAdFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FinishAdFragment(context : Context, finishAdInterface : FinishAdInterface) : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // 뷰 바인딩 정의
    private var _binding: FragmentFinishAdBinding? = null
    private val binding get() = _binding!!
    private var adLoader: AdLoader? = null
    private var finishAdInterface: FinishAdInterface? = null
    private val contextT = context

    init {
        this.finishAdInterface = finishAdInterface
    }

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
        _binding = FragmentFinishAdBinding.inflate(inflater, container, false)

        // 레이아웃 배경을 투명하게 해줌, 필수 아님
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

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
            .forNativeAd { ad : NativeAd ->
                val adView = binding.nativeAdView
                populateNativeAd(adView, ad)
                // 광고 로드 실패 시 처리할 내용을 여기에 추가
                Log.e("ad test", ad.responseInfo.toString())
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // 광고 로드 실패 시 처리할 내용을 여기에 추가
                    Log.e("ad test", adError.message)
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                // Methods in the NativeAdOptions.Builder class can be
                // used here to specify individual options settings.
                .build())
            .build()

        adLoader?.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAd(adView: NativeAdView, ad: NativeAd) {
        /*nativeAdView = binding.nativeAdView

        binding.adHeadline.text = ad.headline
        binding.adBody.text = ad.body
        // 이미지 로드
        binding.adAppIcon.setImageDrawable(ad.icon?.drawable)
        binding.adAdvertiser.text = ad.advertiser
        // 평점 설정
        binding.adStars.rating = ad.starRating?.toFloat() ?: 0f
        // 미디어 뷰 설정
        binding.adMedia.mediaContent = ad.mediaContent

        binding.nativeAdView.mediaView?.mediaContent = ad.mediaContent

        binding.nativeAdView.setNativeAd(ad)*/
        val icon = adView.findViewById<ImageView>(R.id.adAppIcon)
        icon.setImageDrawable(ad.icon?.drawable)
        adView.iconView = icon

        val mediaView = adView.findViewById<MediaView>(R.id.adMedia)
        val temp = ad.mediaContent
        adView.mediaView = mediaView
        mediaView.mediaContent = temp

        val headline = adView.findViewById<TextView>(R.id.adHeadline)
        adView.headlineView = headline
        headline.text = ad.headline


        /*

        val advertiser = adView.findViewById<TextView>(R.id.adAdvertiser)
        advertiser.text = ad.advertiser
        adView.advertiserView = advertiser


        val body = adView.findViewById<TextView>(R.id.adBody)
        body.text = ad.body
        adView.bodyView = body*/

        adView.setNativeAd(ad)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
interface FinishAdInterface {
    fun onYesButtonClick()
}
