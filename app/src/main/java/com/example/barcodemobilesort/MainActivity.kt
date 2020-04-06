package com.example.barcodemobilesort

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlin.random.Random
import android.util.Log


class MainActivity : AppCompatActivity() {

    lateinit var sortButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sortButton = findViewById<Button>(R.id.sortButton)
        sortButton.setOnClickListener {

            // change ean...
            val r = Random.nextDouble()
            var eanToSend:String ="123456"
            if (r < 0.33333) {
                eanToSend = "9782290215715"
            } else if (r < 0.6666) {
                eanToSend = "8076809574631"
            }
            //Log.d("TAG", "$r $eanToSend") // displayed each time the screen is shown

            // Random.nextDouble() does create a value between 0 -> 1
            //for (i in 0..100) {
            //    val x = Random.nextDouble()
            //    Log.d("TAG", "$x") // displayed each time the screen is shown
            //}



            val intent: Intent = Intent(applicationContext, Put2ShelfActivity::class.java)
            intent.putExtra(Put2ShelfActivity.EAN_MSG, eanToSend)
            startActivity(intent)
        }

    }



}
