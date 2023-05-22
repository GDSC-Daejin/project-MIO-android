package com.example.mio.NoticeBoard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.NoticeBoardAdapter
import com.example.mio.Model.PostData
import com.example.mio.databinding.ActivityNoticeBoardBinding
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoticeBoardActivity : AppCompatActivity() {
    private lateinit var nbBinding : ActivityNoticeBoardBinding
    private var manager : LinearLayoutManager = LinearLayoutManager(this)
    private var noticeBoardAdapter : NoticeBoardAdapter? = null
    //게시글 데이터
    private var data : MutableList<PostData?> = mutableListOf()
    var dataPosition = 0
    //게시글 포지션
    private var position = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nbBinding = ActivityNoticeBoardBinding.inflate(layoutInflater)
        setContentView(nbBinding.root)

        initRecyclerView()

        //recyclerview item클릭 시
        noticeBoardAdapter!!.setItemClickListener(object : NoticeBoardAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = data[position]
                    dataPosition = position
                    /*val intent = Intent(activity, HomeEditActivity::class.java).apply {
                        putExtra("type", "edit")
                        putExtra("item", todo)
                        putExtra("categoryNameData", categoryTempData)
                    }
                    requestActivity.launch(intent)*/
                }
            }
        })

        //여기서 edit으로 이동동
       nbBinding.addBtn.setOnClickListener {
            data.add(PostData("2020202", 0, "test", "test"))
            noticeBoardAdapter!!.notifyItemInserted(position)
            position += 1
        }
    }


    private fun initRecyclerView() {
        noticeBoardAdapter = NoticeBoardAdapter()
        noticeBoardAdapter!!.postItemData = data
        nbBinding.noticeBoardRV.adapter = noticeBoardAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        nbBinding.noticeBoardRV.setHasFixedSize(true)
        nbBinding.noticeBoardRV.layoutManager = manager

        nbBinding.noticeBoardRV.itemAnimator =  SlideInUpAnimator(OvershootInterpolator(1f))
        nbBinding.noticeBoardRV.itemAnimator?.apply {
            addDuration = 1000
            removeDuration = 100
            moveDuration = 1000
            changeDuration = 100
        }

    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            RESULT_OK -> {
                val post = it.data?.getSerializableExtra("post") as PostData
                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            data.add(post)
                        }
                        noticeBoardAdapter!!.notifyItemInserted(post.postID)
                    }
                    //edit
                    1 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            data[dataPosition] = post
                        }
                        noticeBoardAdapter!!.notifyItemChanged(post.postID)
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
}