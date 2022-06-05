package com.example.kulendar.calendar

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.kulendar.R
import com.example.kulendar.TableActivity



class CalendarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_calendar,container,false)
        val button_ttable = root.findViewById<Button>(R.id.btn_ttable)

        //버튼 기능
        button_ttable?.setOnClickListener {
            val intent = Intent(activity, TableActivity::class.java)
            startActivity(intent)
        }
        return root
    }

}