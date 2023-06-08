package com.example.mio.Navigation

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.NoticeBoardAdapter
import com.example.mio.Adapter.NotificationAdapter
import com.example.mio.MainActivity
import com.example.mio.Model.NotificationData
import com.example.mio.R
import com.example.mio.databinding.FragmentNotificationBinding
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

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

    //notification data
    private var notificationAllData : ArrayList<NotificationData> = ArrayList()

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


        setHasOptionsMenu(true)
        initToolbarView()
        initNotificationRV()

        return nfBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_another_top_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            nfBinding.anotherBackBtn.id -> {
                requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
                requireActivity().supportFragmentManager.popBackStack()
                true
            }
            else -> {false}
        }
        return super.onOptionsItemSelected(item)
    }


    private fun initNotificationRV() {
        nAdapter = NotificationAdapter()
        if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
        }

        nAdapter!!.notificationItemData = notificationAllData
        nfBinding.notificationRV.adapter = nAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        nfBinding.notificationRV.setHasFixedSize(true)
        nfBinding.notificationRV.layoutManager = manager
    }

    private fun initToolbarView() {
        nfBinding.fragmentAnotherToolBar.inflateMenu(R.menu.fragment_another_top_menu)

        nfBinding.fragmentAnotherToolBar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.action_back -> {

                    true
                }

                else -> {
                    false
                }
            }
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