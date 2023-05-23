package com.example.mio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.mio.Navigation.AccountFragment
import com.example.mio.Navigation.BlankFragment
import com.example.mio.Navigation.HomeFragment
import com.example.mio.Navigation.SearchFragment
import com.example.mio.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var mBinding : ActivityMainBinding
    private val TAG_HOME = "home_fragment"
    private val TAG_SEARCH = "search_fragment"
    private val TAG_ACCOUNT = "account_fragment"
    private val TAG_CAMERA = "CAMERA_fragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setFragment(TAG_HOME, HomeFragment())
        initNavigationBar()
    }

    private fun initNavigationBar() {
        mBinding.bottomNavigationView.
            setOnItemSelectedListener {item ->
                when(item.itemId) {
                    R.id.navigation_home ->
                        setFragment(TAG_HOME, HomeFragment())

                    R.id.navigation_search ->
                        setFragment(TAG_SEARCH, SearchFragment())

                    R.id.navigation_camera ->
                        setFragment(TAG_CAMERA, BlankFragment())

                    R.id.navigation_account ->
                        setFragment(TAG_ACCOUNT, AccountFragment())


                    else ->
                        setFragment(TAG_HOME, HomeFragment())

                }
                true
            }


    }


    fun setFragment(tag : String, fragment: Fragment) {
        val manager : FragmentManager = supportFragmentManager
        val bt = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null) {
            bt.add(R.id.fragment_content, fragment, tag)
        }

        val home = manager.findFragmentByTag(TAG_HOME)
        val search = manager.findFragmentByTag(TAG_SEARCH)
        val camera = manager.findFragmentByTag(TAG_CAMERA)
        val account = manager.findFragmentByTag(TAG_ACCOUNT)


        if (home != null) {
            bt.hide(home)
        }
        if (search != null) {
            bt.hide(search)
        }
        if (camera != null) {
            bt.hide(camera)
        }
        if (account != null) {
            bt.hide(account)
        }

        if (tag == TAG_HOME) {
            if (home != null) {
                bt.show(home)
            }
        }
        else if (tag == TAG_SEARCH) {
            if (search != null) {
                bt.show(search)
            }
        }
        else if (tag == TAG_CAMERA) {
            if (camera != null) {
                bt.show(camera)
            }
        }
        else if (tag == TAG_ACCOUNT) {
            if (account != null) {
                bt.show(account)
            }
        }
        bt.commitAllowingStateLoss()
    }

    fun changeFragment(fragment : Fragment) {
        //프래그먼트를 교체 하는 작업을 수행할 수 있게 해줍니다.
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_content, fragment)
            .commit()
    }
}