package com.example.mio.TabCategory

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.NoticeBoardAdapter
import com.example.mio.Model.PostData
import com.example.mio.NoticeBoard.NoticeBoardEditActivity
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.R
import com.example.mio.databinding.FragmentCarpoolTabBinding
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
 * Use the [CarpoolTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CarpoolTabFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var carpoolBinding : FragmentCarpoolTabBinding
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private var noticeBoardAdapter : NoticeBoardAdapter? = null
    //게시글 데이터
    private var carpoolAllData : MutableList<PostData?> = mutableListOf()
    //게시글 선택 시 위치를 잠시 저장하는 변수
    private var dataPosition = 0
    //게시글 포지션
    private var position = 0
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
    ): View? {
        // Inflate the layout for this fragment
        carpoolBinding = FragmentCarpoolTabBinding.inflate(inflater, container, false)

        initRecyclerView()

        //recyclerview item클릭 시
        noticeBoardAdapter!!.setItemClickListener(object : NoticeBoardAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = carpoolAllData[position]
                    dataPosition = position
                    val intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                        putExtra("type", "READ")
                        putExtra("postItem", temp)
                    }
                    requestActivity.launch(intent)
                }
            }
        })

        //여기서 edit으로 이동동
        carpoolBinding.addBtn.setOnClickListener {
            /*data.add(PostData("2020202", 0, "test", "test"))
            noticeBoardAdapter!!.notifyItemInserted(position)
            position += 1*/
            val intent = Intent(activity, NoticeBoardEditActivity::class.java).apply {
                putExtra("type","ADD")
            }
            requestActivity.launch(intent)
            noticeBoardAdapter!!.notifyDataSetChanged()
        }

        return carpoolBinding.root
    }

    private fun initRecyclerView() {
        noticeBoardAdapter = NoticeBoardAdapter()
        noticeBoardAdapter!!.postItemData = carpoolAllData
        carpoolBinding.noticeBoardRV.adapter = noticeBoardAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        carpoolBinding.noticeBoardRV.setHasFixedSize(true)
        carpoolBinding.noticeBoardRV.layoutManager = manager

        carpoolBinding.noticeBoardRV.itemAnimator =  SlideInUpAnimator(OvershootInterpolator(1f))
        carpoolBinding.noticeBoardRV.itemAnimator?.apply {
            addDuration = 1000
            removeDuration = 100
            moveDuration = 1000
            changeDuration = 100
        }

    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val post = it.data?.getSerializableExtra("postData") as PostData
                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            carpoolAllData.add(post)
                            println(post)
                        }
                        noticeBoardAdapter!!.notifyItemInserted(post.postID)
                    }
                    //edit
                    1 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            carpoolAllData[dataPosition] = post
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CarpoolTabFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CarpoolTabFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}