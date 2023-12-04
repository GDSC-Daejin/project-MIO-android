package com.example.mio

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.alpha
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.example.mio.Model.AddPostData
import com.example.mio.Model.PostData
import com.example.mio.Model.SharedViewModel
import com.example.mio.Navigation.*
import com.example.mio.NoticeBoard.NoticeBoardEditActivity
import com.example.mio.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var mBinding : ActivityMainBinding

    private val TAG_HOME = "home_fragment"
    private val TAG_SEARCH = "search_fragment"
    private val TAG_ACCOUNT = "account_fragment"
    private val TAG_NOTIFICATION = "notification_fragment"
    private val TAG_SETTING = "setting_fragment"
    private var isClicked = false
    private var isSettingClicked = false
    //notification에서 뒤로가기 구현할 때 그 전에 어느 fragment에 있었는 지 알기위한 변수
    private var oldFragment : Fragment? = null
    private var oldTAG = ""
    // private lateinit var loadingDialog : LoadingProgressDialog
    private var backPressedTime = 0L

    private var sharedViewModel: SharedViewModel? = null

    private var toolbarType = "기본"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        this.onBackPressedDispatcher.addCallback(this, callback)
        /*sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        sharedViewModel!!.getCalendarLiveData().observe(this)
        */
        oldFragment = HomeFragment()
        oldTAG = TAG_HOME
        setFragment(TAG_HOME, HomeFragment())
        setToolbarView(toolbarType)
        initNavigationBar()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)

        val actionNotification = menu!!.findItem(R.id.action_notification)
        val actionSetting = menu!!.findItem(R.id.action_main_setting)

        if (isClicked) {
            actionNotification.isVisible = !isClicked
        } else {
            actionNotification.isVisible = !isClicked
        }

        if (isSettingClicked) {
            actionSetting.isVisible = !isSettingClicked
            actionNotification.isVisible = false
        } else {
            actionSetting.isVisible = !isSettingClicked
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_notification -> {
                isClicked = true
                toolbarType = "알림"
                setToolbarView(toolbarType)
                setFragment(TAG_NOTIFICATION, NotificationFragment())

                println(isClicked)
                super.onOptionsItemSelected(item)
            }
            R.id.action_main_setting -> {
                //공유 버튼 눌렀을 때
                isSettingClicked = true

                setFragment(TAG_SETTING, SettingsFragment())
                toolbarType = "설정"
                setToolbarView(toolbarType)

                Toast.makeText(applicationContext, "세팅 이벤트 실행", Toast.LENGTH_LONG).show()
                super.onOptionsItemSelected(item)
            }

            android.R.id.home -> {
                setFragment(oldTAG, oldFragment!!)
                toolbarType = "기본"
                setToolbarView(toolbarType)
                isClicked = false
                isSettingClicked = false
                Log.e("eoerer", oldTAG)
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initNavigationBar() {
        mBinding.bottomNavigationView.
        setOnItemSelectedListener {item ->
            when(item.itemId) {
                R.id.navigation_home -> {
                    oldFragment = HomeFragment()
                    oldTAG = TAG_HOME
                    //setToolbarView(TAG_HOME, oldTAG)
                    setFragment(TAG_HOME, HomeFragment())
                    toolbarType = "기본"
                    setToolbarView(toolbarType)
                    isClicked = false
                    isSettingClicked = false
                }

                R.id.navigation_search -> {
                    oldFragment = SearchFragment()
                    oldTAG = TAG_SEARCH
                    //setToolbarView(TAG_HOME, oldTAG)
                    setFragment(TAG_SEARCH, SearchFragment())
                    toolbarType = "기본"
                    setToolbarView(toolbarType)
                    isClicked = false
                    isSettingClicked = false

                }

                R.id.navigation_writing -> {
                    val intent = Intent(this, NoticeBoardEditActivity::class.java).apply {
                        putExtra("type","ADD")
                    }
                    requestActivity.launch(intent)
                    /*oldFragment = HomeFragment()
                    oldTAG = TAG_HOME
                    //setToolbarView(TAG_HOME, oldTAG)
                    setFragment(TAG_HOME, HomeFragment())
                    setToolbarView(false)
                    isClicked = false
                    isSettingClicked = false*/
                }


                R.id.navigation_account -> {
                    oldFragment = AccountFragment()
                    oldTAG = TAG_ACCOUNT
                    //setToolbarView(TAG_HOME, oldTAG)
                    setFragment(TAG_ACCOUNT, AccountFragment())
                    toolbarType = "기본"
                    setToolbarView(toolbarType)
                    isClicked = false
                    isSettingClicked = false
                }


                else -> {
                    oldFragment = HomeFragment()
                    oldTAG = TAG_HOME
                    setFragment(TAG_HOME, HomeFragment())
                    toolbarType = "기본"
                    setToolbarView(toolbarType)
                    isClicked = false
                    isSettingClicked = false

                }

            }
            true
        }


    }



    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        oldFragment = HomeFragment()
                        oldTAG = TAG_HOME
                        //setToolbarView(TAG_HOME, oldTAG)
                        setFragment(oldTAG, oldFragment!!)

                        mBinding.bottomNavigationView.selectedItemId = R.id.navigation_home

                        CoroutineScope(Dispatchers.Main).launch {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_content, HomeFragment())
                                .commit()
                        }

                        //finish()
                    }
                    //수정 테스트 해보기 todo//edit
                    1 -> {
                        oldFragment = HomeFragment()
                        oldTAG = TAG_HOME
                        //setToolbarView(TAG_HOME, oldTAG)
                        setFragment(TAG_HOME, HomeFragment())

                        mBinding.bottomNavigationView.selectedItemId = R.id.navigation_home

                        CoroutineScope(Dispatchers.Main).launch {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_content, HomeFragment())
                                .commit()
                        }

                        //finish()
                    }

                    7 -> {
                        //이거 먼저 되는지 테스트
                        oldFragment = AccountFragment()
                        oldTAG = TAG_ACCOUNT
                        //setToolbarView(TAG_HOME, oldTAG)
                        setFragment(TAG_ACCOUNT, AccountFragment())
                        mBinding.bottomNavigationView.selectedItemId = R.id.navigation_account
                    }

                    //여기는 알람 클릭 시 notificationFragment로 이동하기 위함
                    8 -> {
                        isClicked = true
                        toolbarType = "알림"
                        setToolbarView(toolbarType)
                        oldFragment = NotificationFragment()
                        oldTAG = TAG_NOTIFICATION
                        //setToolbarView(TAG_HOME, oldTAG)
                        setFragment(TAG_NOTIFICATION, NotificationFragment())
                        //mBinding.bottomNavigationView.selectedItemId = R.id.action_notification
                    }

                    9 -> {
                        toolbarType = "기본"
                        setToolbarView(toolbarType)
                        setFragment(TAG_HOME, HomeFragment())
                        mBinding.bottomNavigationView.selectedItemId = R.id.navigation_home
                    }

                }
                //getSerializableExtra = intent의 값을 보내고 받을때사용
                //타입 변경을 해주지 않으면 Serializable객체로 만들어지니 as로 캐스팅해주자
                /*val pill = it.data?.getSerializableExtra("pill") as PillData
                val selectCategory = it.data?.getSerializableExtra("cg") as String*/

                //선택한 카테고리 및 데이터 추가


                /*if (selectCategory.isNotEmpty()) {
                    selectCategoryData[selectCategory] = categoryArr
                }*/


                //api 33이후 아래로 변경됨
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    getSerializable(key, T::class.java)
                } else {
                    getSerializable(key) as? T
                }*/
                /*when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            data.add(pill)
                            categoryArr.add(pill)
                            //add면 그냥 추가
                            selectCategoryData[selectCategory] = categoryArr
                            //전
                            //println( categoryArr[dataPosition])
                        }
                        println("전 ${selectCategoryData[selectCategory]}")
                        //livedata
                        sharedViewModel!!.setCategoryLiveData("add", selectCategoryData)


                        homeAdapter!!.notifyDataSetChanged()
                        Toast.makeText(activity, "추가되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    //edit
                    1 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            data[dataPosition] = pill
                            categoryArr[dataPosition] = pill
                            selectCategoryData.clear()
                            selectCategoryData[selectCategory] = categoryArr
                            //후
                            //println(categoryArr[dataPosition])
                        }
                        println("선택 $selectCategory")
                        //livedata
                        sharedViewModel!!.categoryLiveData.value = selectCategoryData
                        println(testselectCategoryData)
                        homeAdapter!!.notifyDataSetChanged()
                        //Toast.makeText(activity, "수정되었습니다.", Toast.LENGTH_SHORT).show()
                        Toast.makeText(activity, "$testselectCategoryData", Toast.LENGTH_SHORT).show()
                    }
                }*/
            }
        }
    }

    private fun setFragment(tag : String, fragment: Fragment) {
        val manager : FragmentManager = supportFragmentManager
        val bt = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null) {
            bt.add(R.id.fragment_content, fragment, tag).addToBackStack(null)
        }

        val home = manager.findFragmentByTag(TAG_HOME)
        val search = manager.findFragmentByTag(TAG_SEARCH)
        val account = manager.findFragmentByTag(TAG_ACCOUNT)
        val notification = manager.findFragmentByTag(TAG_NOTIFICATION)
        val setting = manager.findFragmentByTag(TAG_SETTING)

        if (home != null) {
            bt.hide(home)
        }
        if (search != null) {
            bt.hide(search)
        }
        if (account != null) {
            bt.hide(account)
        }
        if (notification != null) {
            bt.hide(notification)
        }
        if (setting != null) {
            bt.hide(setting)
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
        else if (tag == TAG_ACCOUNT) {
            if (account != null) {
                bt.show(account)
            }
        }
        else if (tag == TAG_NOTIFICATION) {
            if (notification != null) {
                bt.show(notification)
            }
        }
        else if (tag == TAG_SETTING) {
            if (setting != null) {
                bt.show(setting)
            }
        }

        bt.commitAllowingStateLoss()
    }

    private fun setToolbarView(type : String) {
        when (type) {
            "기본" -> {
                setSupportActionBar(mBinding.toolBar)
                supportActionBar?.setLogo(R.drawable.top_icon_vector)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setDisplayShowTitleEnabled(false)
            }
            "설정" -> {
                setSupportActionBar(mBinding.toolBar)
                supportActionBar?.title = "설정"
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setLogo(null)

                supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
                supportActionBar?.setDisplayShowTitleEnabled(true)

            }
            "알림" -> {
                setSupportActionBar(mBinding.toolBar)
                supportActionBar?.title = "알림"
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setLogo(null)

                supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
                supportActionBar?.setDisplayShowTitleEnabled(true)

            }
        }
    }

    fun changeFragment(fragment : Fragment) {
        //프래그먼트를 교체 하는 작업을 수행할 수 있게 해줍니다.
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_content, fragment)
            .commit()
    }

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로가기 클릭 시 실행시킬 코드 입력
            val transaction = supportFragmentManager.beginTransaction()
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_content)
            oldFragment = null
            oldTAG = ""

            val fragmentManager = supportFragmentManager

            if (System.currentTimeMillis() > backPressedTime + 2000) {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(this@MainActivity, "뒤로 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
                return
            }

            if (System.currentTimeMillis() <= backPressedTime + 2000) {
                if (fragmentManager.backStackEntryCount > 0) {
                    fragmentManager.popBackStack()

                } else {
                    this@MainActivity.finishAffinity()
                }

                if (currentFragment != null) {
                    transaction.remove(currentFragment)

                    transaction.commit()
                }

                this@MainActivity.finishAffinity()
            }
        }
    }
}