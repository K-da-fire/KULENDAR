package com.example.kulendar.dday

import android.app.DatePickerDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kulendar.DB.Dday
import com.example.kulendar.DB.MYDBHelper_User
import com.example.kulendar.DB.MyDBHelper_Dday
import com.example.kulendar.R
import com.example.kulendar.databinding.ActivityMainBinding
import com.example.kulendar.databinding.DdayitemListBinding
import com.example.kulendar.databinding.FragmentDdayFramgentBinding
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*

class DdayFramgent : AppCompatActivity() {
    /** Called when the activity is first created.  */
    private var ddayText: TextView? = null
    lateinit var binding:FragmentDdayFramgentBinding
    lateinit var adapter:MyAdapter
    private val data:ArrayList<MyData> = ArrayList()
    lateinit var MyDBHelper: MyDBHelper_Dday
    lateinit var MyUserDBHelper:MYDBHelper_User
    private var saveBtn: Button? = null
    //오늘 연월일 변수
    private var tYear = 0
    private var tMonth = 0
    private var tDay = 0
    //디데이 연월일 변수
    private var dYear = 1
    private var dMonth = 1
    private var dDay = 1
    private var d: Long = 0
    private var t: Long = 0
    private var r: Long = 0
    private var resultNumber = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= FragmentDdayFramgentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initDB()
        initLayout()
        //initData()
        initRecyclerView()
        getAllRecord()
    }

    private fun initDB() {
        val dbfile=getDatabasePath("myddaydb.db")

        if(!dbfile.parentFile.exists()){
            dbfile.parentFile.mkdir()
        }
        if(!dbfile.exists()){
            val file=resources.openRawResource(R.raw.myddaydb)
            val fileSize=file.available()
            val buffer=ByteArray(fileSize)
            file.read(buffer)
            file.close()
            dbfile.createNewFile()
            val output=FileOutputStream(dbfile)
            output.write(buffer)
            output.close()
        }
    }

    private fun getAllRecord(){
        data.clear()
        data.addAll(MyDBHelper.getAllRecord())
    }

    private fun initLayout() {
        MyDBHelper= MyDBHelper_Dday(this)
        MyUserDBHelper= MYDBHelper_User(this)
        saveBtn = binding.saveButton
        saveBtn!!.setOnClickListener {
            showDialog(0)
        }

        val calendar = Calendar.getInstance() //현재 날짜 불러옴
        tYear = calendar[Calendar.YEAR]
        tMonth = calendar[Calendar.MONTH]
        tDay = calendar[Calendar.DAY_OF_MONTH]
        val dCalendar = Calendar.getInstance()
        dCalendar[dYear, dMonth] = dDay
        t = calendar.timeInMillis //오늘 날짜를 밀리타임으로 바꿈
        d = dCalendar.timeInMillis //디데이날짜를 밀리타임으로 바꿈
        r = (d - t) / (24 * 60 * 60 * 1000) //디데이 날짜에서 오늘 날짜를 뺀 값을 '일'단위로 바꿈
        resultNumber = r.toInt() + 1
    }

    private val dDateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val calendar = Calendar.getInstance()
            dYear = year
            dMonth = monthOfYear
            dDay = dayOfMonth
            val dCalendar = Calendar.getInstance()
            dCalendar[dYear, dMonth] = dDay
            d = dCalendar.timeInMillis
            r = (d - t) / (24 * 60 * 60 * 1000)
            resultNumber = r.toInt()
            val ddayinput=binding.ddayInput.text.toString()
            binding.ddayInput.text=null
            adapter.updateRecycleerView()

            //데이터베이스에 데이터추가하기
            val User_id=MyUserDBHelper.getID()
            val product= Dday(User_id,dYear,dMonth,dDay,ddayinput)
            val result=MyDBHelper.insertProduct(product)
            if(result){
                getAllRecord()
                Toast.makeText(this,"Data INSERT SUCCESS",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"Data INSERT FAILED",Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateDialog(id: Int): Dialog? {
        if (id == DATE_DIALOG_ID) {
            return DatePickerDialog(this, dDateSetListener, tYear, tMonth, tDay)
        }
        return null
    }

    companion object {
        const val DATE_DIALOG_ID = 0
    }

    private fun initRecyclerView(){
        binding.recyclerView.layoutManager=
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        adapter= MyAdapter(data)

        binding.recyclerView.adapter = adapter

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                adapter.moveItem(p1.adapterPosition,p2.adapterPosition)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                var num=viewHolder.adapterPosition
                //데이터베이스에서 삭제
                Toast.makeText(this@DdayFramgent,data[num].pid+"hihi"+(num-1).toString(),Toast.LENGTH_SHORT).show()
                val result=MyDBHelper.deleteProduct(data[num].pid)
                //val result = false
                if(result){
                    Toast.makeText(this@DdayFramgent,"Data DELETE SUCCESS",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@DdayFramgent,"Data DELETE FAILED"+data[viewHolder.adapterPosition].pid+"hihi",Toast.LENGTH_SHORT).show()
                }
                adapter.removeItem(viewHolder.adapterPosition)
                data.clear()
                getAllRecord()
            }
        }

        val itemTouchHelper= ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }
}