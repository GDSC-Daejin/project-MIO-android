package com.example.mio.bottomsheetfragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.mio.R
import com.example.mio.databinding.FragmentBottomAdBinding
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BottomAdFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BottomAdFragment : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnSendFromBottomSheetDialog? = null
    private lateinit var binding : FragmentBottomAdBinding
    //private var interstitialAd : InterstitialAd? = null
    private lateinit var nativeAdView: NativeAdView
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
        binding = FragmentBottomAdBinding.inflate(inflater, container, false)

        //initAd()


       /* if (interstitialAd != null) {
            interstitialAd?.show(requireActivity())
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    Log.d("onADClick", "ad clicked")
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    Log.d("onaddismissed", "ad dismissed")
                    interstitialAd = null
                    initAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    Log.e("onadfailshow", p0.message)
                    interstitialAd = null
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    Log.d("onadimpression", "ad recorded an impression")
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    Log.d("onadshow", "ad show")
                }
            }
        }*/


        binding.bottomSheetButton.setOnClickListener {
            //interstitialAd = null
            initAd()
            if (listener == null) return@setOnClickListener
            listener?.sendValue("dismiss front")
            dismiss()
        }


        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 광고 초기화 및 로드
        initAd()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        /*val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }*/
        val dialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogTheme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                /*val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED*/
            }
        }

        return dialog
    }
    private fun initAd() { //전면광고
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val nativeAdOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(requireContext(), "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad ->
                // 네이티브 광고 로드 완료 시 실행되는 콜백
                val adView = binding.nativeAdView
                populateNativeAd(adView, ad)

                //displayBigNativeAd(adView, nativeAd)
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
                    .setVideoOptions(videoOptions).build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
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
        val headline = adView.findViewById<TextView>(R.id.adHeadline)
        headline.text = ad.headline
        adView.headlineView = headline

        val advertiser = adView.findViewById<TextView>(R.id.adAdvertiser)
        advertiser.text = ad.advertiser
        adView.advertiserView = advertiser

        val icon = adView.findViewById<ImageView>(R.id.adAppIcon)
        icon.setImageDrawable(ad.icon?.drawable)
        adView.iconView = icon

        val mediaView = adView.findViewById<MediaView>(R.id.adMedia)
        adView.mediaView = mediaView

        val body = adView.findViewById<TextView>(R.id.adBody)
        body.text = ad.body
        adView.bodyView = body

        // 평점 설정
        binding.adStars.rating = ad.starRating?.toFloat() ?: 0f

        adView.setNativeAd(ad)
    }

    interface OnSendFromBottomSheetDialog {
        fun sendValue(value: String)
    }

    fun setCallback(listener: OnSendFromBottomSheetDialog) {
        this.listener = listener
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BottomAdFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BottomAdFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}