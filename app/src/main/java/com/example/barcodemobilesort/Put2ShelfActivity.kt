package com.example.barcodemobilesort

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


//$ curl http://192.168.1.200:8080/get_states
//{"loc_6": 0, "loc_7": 0, "loc_4": 0, "loc_5": 1, "loc_2": 0, "loc_3": 0, "loc_0": 0, "loc_1": 1}
//$ curl http://192.168.1.200:8080/reset
//OK

interface MobileSortAPI {
    @GET("/get_states")
    fun getStates(): Call<Map<String, Int>>

    @GET("/reset")
    fun reset(): Call<String>
}

class RestAPI() {

    private val mobileSortAPI: MobileSortAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.200:8080/")

            //.baseUrl("https://www.google.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        mobileSortAPI = retrofit.create(MobileSortAPI::class.java)
    }

    fun getStates(): Call<Map<String, Int>> {
        return mobileSortAPI.getStates()
    }
    fun reset(): Call<String> {
        return mobileSortAPI.reset()
    }
}




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

        val api = RestAPI()
        /*
        val mycall : Call<String> = api.reset()
        //val result = call.execute().body()   // will trigger a android.os.NetworkOnMainThreadException because using UI thread for network is baaaaaad :D
        mycall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("TAG", response.body())
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("TAG", "ERROR: "+t.message.toString())
            }

        })
        */
        val mycall : Call<Map<String, Int>> = api.getStates()
        mycall.enqueue(object : Callback<Map<String, Int>> {
            override fun onFailure(call: Call<Map<String, Int>>, t: Throwable) {
                Log.d("TAG", "ERROR: "+t.message.toString())
            }

            override fun onResponse(
                call: Call<Map<String, Int>>,
                response: Response<Map<String, Int>>
            ) {
                Log.d("TAG", response.body().toString())
            }
        })

    }

    fun getDesc(ean: String) : String {
        var res = "unknown"
        if      (ean == "9782290215715") { res= "Game of Throne" }
        else if (ean == "8076809574631") { res= "Pasta Barilla Fusilli" }
        return res
    }


}
