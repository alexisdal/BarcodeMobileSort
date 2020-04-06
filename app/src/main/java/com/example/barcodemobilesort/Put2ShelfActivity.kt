package com.example.barcodemobilesort

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream


class Put2ShelfActivity : AppCompatActivity() {

    // http://www.droidmonk.com/kotlin/load-new-activity-pass-value-kotlin/
    companion object {
        const val EAN_MSG="msg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_put2_shelf_activity)

        val msg=intent.getStringExtra(EAN_MSG)
        //tv_msg.text=msg
        //Log.d("TAG", "AZERTY") // displayed each time the screen is shown
        //Log.d("TAG", msg) // displayed each time the screen is shown
        this.setEAN(msg)
    }

    fun setEAN(ean: String) {

        val tvDesc: TextView = findViewById<TextView>(R.id.textDescription)
        val iv: ImageView = findViewById<ImageView>(R.id.imageView)
        val desc = this.getDesc(ean)
        var filename = "_$ean.png"

        tvDesc.text = "$desc\n\nEAN:$ean"
        if (desc == "unknown") { iv.setImageResource(R.drawable._unknown) }
        else if (ean == "8076809574631") { iv.setImageResource(R.drawable._8076809574631) }
        else if (ean == "9782290215715") { iv.setImageResource(R.drawable._9782290215715) }

        // https://android--code.blogspot.com/2015/08/android-imageview-set-image-from-assets.html

    }

    fun getDesc(ean: String) : String {
        var res = "unknown"
        if      (ean == "9782290215715") { res= "Game of Throne" }
        else if (ean == "8076809574631") { res= "Pasta Barilla Fusilli" }
        return res
    }


}
