package com.example.mio.Navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.NoticeBoardAdapter
import com.example.mio.Adapter.NotificationAdapter
import com.example.mio.Helper.SharedPref
import com.example.mio.MainActivity
import com.example.mio.Model.NotificationData
import com.example.mio.Model.PostData
import com.example.mio.Model.SharedViewModel
import com.example.mio.R
import com.example.mio.databinding.FragmentNotificationBinding
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [NotificationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var nfBinding : FragmentNotificationBinding
    private lateinit var nAdapter : NotificationAdapter
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    var sharedPref : SharedPref? = null
    private var setKey = "setting_history"
    //notification data
    private var notificationAllData : ArrayList<NotificationData> = ArrayList()
    private var sharedViewModel: SharedViewModel? = null
    var data: NotificationData? = null
    var title : String? = null

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
        //activity?.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nfBinding = FragmentNotificationBinding.inflate(inflater, container, false)
        sharedPref = SharedPref(requireContext())

        if (arguments != null) {
            title = requireArguments().getString("title")
        }
        setHasOptionsMenu(true)

        initNotificationRV()
        initData()
        /*data = arguments?.getSerializable("notification") as NotificationData
        Log.d("data" , "$data")*/

        nfBinding.notNotificationLl.setOnClickListener {
            println(notificationAllData)
            println(data)
            println(title)
        }

        return nfBinding.root
    }

    private fun initData() {
        CoroutineScope(Dispatchers.IO).launch {
            getHistory()
        }
    }





    private fun initNotificationRV() {

        nAdapter = NotificationAdapter()
        /*if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
        }*/

        nAdapter!!.notificationItemData = notificationAllData
        nfBinding.notificationRV.adapter = nAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        nfBinding.notificationRV.setHasFixedSize(true)
        nfBinding.notificationRV.layoutManager = manager

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        //이건 나중에
        val addObserver = androidx.lifecycle.Observer<ArrayList<NotificationData>> { textValue ->
            notificationAllData = textValue
        }
        sharedViewModel!!.getNotificationLiveData().observe(viewLifecycleOwner, addObserver)

    }

    override fun onStart() {
        super.onStart()
        println("start")
        if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        println("resume")
        getHistory()
        if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
        }
    }


    override fun onPause() {
        super.onPause()
        println("pause")
        if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
        }
    }



    private fun getHistory() {
        val historyData = sharedPref!!.getNotify(requireContext(), setKey)

        if (historyData.isNotEmpty()) {
            //notificationAllData.clear()
            //searchWordList.addAll(historyData)
            for (i in historyData.indices) {
                notificationAllData.add(NotificationData(i,"test", PostData("20201530@daejin.ac.kr", 0, "test", "test", "test", "test", "a","10" ,1, 4, 3000), true, historyData[i]))
                println(historyData[i])
                println(notificationAllData)
            }
            nAdapter!!.notifyDataSetChanged()
        } else {

        }


    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}