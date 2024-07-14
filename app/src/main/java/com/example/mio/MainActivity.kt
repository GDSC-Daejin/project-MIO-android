package com.example.mio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.mio.Model.AddAlarmResponseData
import com.example.mio.Model.SharedViewModel
import com.example.mio.Model.User
import com.example.mio.Navigation.*
import com.example.mio.NoticeBoard.NoticeBoardEditActivity
import com.example.mio.databinding.ActivityMainBinding
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


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

    private var isFirstAccountEdit : String? = null

    private var selectedTab = ""
    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private var notificationIcon : MenuItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        MobileAds.initialize(this@MainActivity) {}
        this.onBackPressedDispatcher.addCallback(this, callback)
        /*sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        sharedViewModel!!.getCalendarLiveData().observe(this)
        */
        if (oldFragment != null && oldTAG != "") {
            setFragment(oldTAG, oldFragment!!)
        } else {
            oldFragment = HomeFragment()
            oldTAG = TAG_HOME
            setFragment(TAG_HOME, HomeFragment())
        }

        setToolbarView(toolbarType)
        initNavigationBar()
        initUserSet()
        saveSettingData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)

        val actionNotification = menu?.findItem(R.id.action_notification)
        val actionSetting = menu?.findItem(R.id.action_main_setting)

        notificationIcon = actionNotification


       /*if (isClicked) {
            Log.e("isclicked", isClicked.toString())
            actionNotification?.isVisible = !isClicked
            actionNotification?.isVisible = false
        } else {
            actionNotification?.isVisible = !isClicked
        }

        if (isSettingClicked) {
            actionSetting?.isVisible = !isSettingClicked
            actionNotification?.isVisible = false
        } else {
            actionSetting?.isVisible = !isSettingClicked
        }
        actionNotification?.isVisible = !isClicked
        Log.e("actionNotificaion", isClicked.toString())
        actionSetting?.isVisible = !isSettingClicked
        Log.e("actionSetting", isSettingClicked.toString())*/
        if (isClicked) {
            Log.e("isclicked", isClicked.toString())
            actionNotification?.isVisible = false//!isClicked
            toolbarType = "알림"
            setToolbarView(toolbarType)
            //actionSetting?.isVisible = true
        } else {
            Log.e("isclicked", isClicked.toString())
            actionNotification?.isVisible = true//!isClicked
            //actionSetting?.isVisible = false
        }

        if (isSettingClicked) {
            Log.e("isSettingClicked", isSettingClicked.toString())
            actionSetting?.isVisible = false
        } else {
            Log.e("isSettingClicked", isSettingClicked.toString())
            actionSetting?.isVisible = true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_notification -> {
                isClicked = true

                changeFragment(NotificationFragment())
                toolbarType = "알림"
                setToolbarView(toolbarType)
                //setFragment(TAG_NOTIFICATION, NotificationFragment())

                super.onOptionsItemSelected(item)
            }
            R.id.action_main_setting -> {
                //설정 버튼 눌렀을 때
                isSettingClicked = true

                //setFragment(TAG_SETTING, SettingsFragment())
                changeFragment(SettingsFragment())
                toolbarType = "설정"
                setToolbarView(toolbarType)

                //Toast.makeText(applicationContext, "세팅 이벤트 실행", Toast.LENGTH_LONG).show()
                super.onOptionsItemSelected(item)
            }

            android.R.id.home -> {
                //setFragment(oldTAG, oldFragment!!)
                changeFragment(HomeFragment())
                toolbarType = "기본"
                setToolbarView(toolbarType)
                isClicked = false
                isSettingClicked = false
                Log.e("eoerer", oldTAG)
                mBinding.bottomNavigationView.selectedItemId = R.id.navigation_home

                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveSettingData() { //처음 앱 사용 시 저장한 isFirstAccountEdit가 없어서 null이니 true로 저장 후 dialog를 실행토록함
        //다음에는 true가 저장되어있었으니 false로 저장내용을 바꾸고 다시 저장하여 dialog가 나오지 않도록 함
        val sharedPref = this.getSharedPreferences("saveSetting", Context.MODE_PRIVATE)
        isFirstAccountEdit = sharedPref.getString("isFirstAccountEdit", "") ?: ""

        if (isFirstAccountEdit?.isEmpty() == true) {
            Log.d("MainActivity", isFirstAccountEdit.toString())
            Log.d("MainActivity", "비었으니까 처음실행한듯 ")

            with(sharedPref.edit()) {
                putString("isFirstAccountEdit", "true")
                apply() // 비동기적으로 데이터를 저장
            }
        } else {
            Log.d("MainActivity", isFirstAccountEdit.toString())
            Log.d("MainActivity", "안 비었으니까 처음실행x")

            with(sharedPref.edit()) {
                putString("isFirstAccountEdit", "false")
                apply() // 비동기적으로 데이터를 저장
            }
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
                        //setFragment(TAG_HOME, HomeFragment())
                        changeFragment(HomeFragment())
                        toolbarType = "기본"
                        setToolbarView(toolbarType)
                        isClicked = false
                        isSettingClicked = false
                    }

                    R.id.navigation_search -> {
                        oldFragment = SearchFragment()
                        oldTAG = TAG_SEARCH
                        //setToolbarView(TAG_HOME, oldTAG)
                        //setFragment(TAG_SEARCH, SearchFragment())
                        changeFragment(SearchFragment())
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
                        //setFragment(TAG_ACCOUNT, AccountFragment())
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_content, AccountFragment())
                            .commit()
                        //setFragment(oldTAG, oldFragment!!)
                        toolbarType = "기본"
                        setToolbarView(toolbarType)
                        isClicked = false
                        isSettingClicked = false
                    }


                    else -> {
                        oldFragment = HomeFragment()
                        oldTAG = TAG_HOME
                        //setFragment(TAG_HOME, HomeFragment())
                        changeFragment(HomeFragment())
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
        if (it.resultCode == RESULT_OK) {
            CoroutineScope(Dispatchers.Main).launch {
                val flag = it.data?.getIntExtra("flag", -1) ?: -1
                handleResult(flag, it)
            }
        }
    }
    private fun handleResult(flag: Int, result : ActivityResult) {
        when (flag) {
            0 -> {
                // HomeFragment로 전환
                oldFragment = HomeFragment()
                oldTAG = TAG_HOME
                mBinding.bottomNavigationView.selectedItemId = R.id.navigation_home

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, HomeFragment())
                    .commit()
            }
            1 -> {
                // 수정용 HomeFragment로 전환 (동일한 로직이므로 0과 병합 가능)
                oldFragment = HomeFragment()
                oldTAG = TAG_HOME
                mBinding.bottomNavigationView.selectedItemId = R.id.navigation_home

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, HomeFragment())
                    .commit()
            }
            6 -> {
                // AccountFragment로 전환
                oldFragment = AccountFragment()
                oldTAG = TAG_ACCOUNT
                mBinding.bottomNavigationView.selectedItemId = R.id.navigation_account

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, AccountFragment())
                    .commit()
            }
            // 추가적인 flag 처리
            7 -> {
                //이거 먼저 되는지 테스트
                oldFragment = AccountFragment()
                oldTAG = TAG_ACCOUNT
                //setToolbarView(TAG_HOME, oldTAG)
                setFragment(TAG_ACCOUNT, AccountFragment())
                mBinding.bottomNavigationView.selectedItemId = R.id.navigation_account
            }

            //여기는 알람 클릭 시 notificationFragment로 이동하기 위함
            /*8 -> {
                isClicked = true
                toolbarType = "알림"
                setToolbarView(toolbarType)
                oldFragment = NotificationFragment()
                oldTAG = TAG_NOTIFICATION
                //setToolbarView(TAG_HOME, oldTAG)
                setFragment(TAG_NOTIFICATION, NotificationFragment())
                //mBinding.bottomNavigationView.selectedItemId = R.id.action_notification
            }*/

            9 -> {
                toolbarType = "기본"
                setToolbarView(toolbarType)
                setFragment(TAG_HOME, HomeFragment())
                mBinding.bottomNavigationView.selectedItemId = R.id.navigation_home
            }

            22-> {
                toolbarType = "기본"
                setToolbarView(toolbarType)
                selectedTab = result.data?.getStringExtra("selectedTab").toString()

                // HomeFragment에 전달할 데이터를 포함한 Bundle 생성
                val bundle = Bundle().apply {
                    putString("selectedTab", selectedTab)
                }
                val homeFragment = HomeFragment().apply {
                    arguments = bundle
                }
                setFragment(TAG_HOME, homeFragment)

                mBinding.bottomNavigationView.selectedItemId = R.id.navigation_home
            }
        }
    }
    private fun setFragment(tag : String, fragment: Fragment) {
        val manager : FragmentManager = supportFragmentManager
        val bt = manager.beginTransaction()

        /*if (manager.findFragmentByTag(tag) == null) {
            bt.add(R.id.fragment_content, fragment, tag).addToBackStack(null)
        }*/
        val existingFragment = manager.findFragmentByTag(tag)

        if (existingFragment == null) {
            // Fragment가 스택에 없으면 add를 사용하여 추가하고 백 스택에 추가
            bt.add(R.id.fragment_content, fragment, tag).addToBackStack(null)
        } else {
            // Fragment가 스택에 있으면 replace를 사용하여 교체
            bt.replace(R.id.fragment_content, fragment, tag)
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

    private fun initUserSet() {
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
        val userEmail = saveSharedPreferenceGoogleLogin.getUserEMAIL(this).toString()

        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()

                if (expireDate != null && expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    Log.d("MainActivitu Notification", expireDate.toString())

                    // UI 스레드에서 Toast 실행
                    this@MainActivity.runOnUiThread {
                        Toast.makeText(this@MainActivity, "로그인 세션이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                    }

                    // Log.d("MainActivitu Notification", expireDate.toString())
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    startActivity(intent)
                    finish()
                    return@Interceptor chain.proceed(newRequest)
                }

            } else newRequest = chain.request()
            chain.proceed(newRequest)
        }

        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)
        /////
        api.getAccountData(userEmail).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    saveSharedPreferenceGoogleLogin.setUserId(this@MainActivity, responseData?.id)
                    saveSharedPreferenceGoogleLogin.setArea(this@MainActivity, responseData?.activityLocation)
                    CoroutineScope(Dispatchers.IO).launch {
                        initNotification(notificationIcon)
                    }
                } else {
                    Log.e("MainActivity set user", response.message().toString())
                    Log.e("MainActivity set user", response.code().toString())
                    Log.e("MainActivity set user", response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("MainActivity set user", t.message.toString())
            }
        })
    }

    private fun initNotification(menuItem: MenuItem?) {
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
        val notificationCheck = saveSharedPreferenceGoogleLogin.getSharedNotification(this).toString()

        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()

                if (expireDate != null && expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    Log.d("MainActivitu Notification", expireDate.toString())

                    // UI 스레드에서 Toast 실행
                    this@MainActivity.runOnUiThread {
                        Toast.makeText(this@MainActivity, "로그인 세션이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                    }

                   // Log.d("MainActivitu Notification", expireDate.toString())
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    startActivity(intent)
                    finish()
                    return@Interceptor chain.proceed(newRequest)
                }

            } else newRequest = chain.request()
            chain.proceed(newRequest)
        }

        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)
        /////
        api.getMyAlarm().enqueue(object : Callback<List<AddAlarmResponseData>> {
            override fun onResponse(
                call: Call<List<AddAlarmResponseData>>,
                response: Response<List<AddAlarmResponseData>>
            ) {
                if (response.isSuccessful) {
                    if (response.body().isNullOrEmpty() && response.body()?.toString() == "") {
                        menuItem?.setIcon(R.drawable.top_menu_notification)
                        Log.e("MainActivitu Notification??", notificationCheck.toString())
                    } else {
                       if (response.body()?.size!! > notificationCheck.toInt()) { //사이즈가 달라짐 = 데이터가 더 추가되었다
                           menuItem?.setIcon(R.drawable.notification_update_icon)
                           Log.e("MainActivitu Notification??", notificationCheck.toString())
                       } else { //달라진게없으면? 다시 원상태 즉 봣다는거니
                           menuItem?.setIcon(R.drawable.top_menu_notification)
                           Log.e("MainActivitu Notification??", notificationCheck.toString())
                       }
                        /*Log.e("MainActivitu Notification", response.body()?.size.toString())
                        Log.e("MainActivitu Notification", notificationCheck.toString())*/

                    }

                } else {
                    Log.e("MainActivitu Notification", response.code().toString())
                    Log.e("MainActivitu Notification", response.errorBody().toString())
                    Log.e("MainActivitu Notification", response.message().toString())
                }
            }

            override fun onFailure(call: Call<List<AddAlarmResponseData>>, t: Throwable) {
                Log.d("MainActivitu Notification", t.message.toString())
            }

        })
    }

    private fun changeFragment(fragment : Fragment) {
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