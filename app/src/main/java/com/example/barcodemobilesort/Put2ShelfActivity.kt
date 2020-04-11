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
        else if (ean == "3178530402988") { iv.setImageResource(R.drawable._3178530402988) }
        else if (ean == "3596710469079") { iv.setImageResource(R.drawable._3596710469079) }
        else if (ean == "3564707000086") { iv.setImageResource(R.drawable._3564707000086) }
        else if (ean == "3606323172737") { iv.setImageResource(R.drawable._3606323172737) }
        else if (ean == "7622210368997") { iv.setImageResource(R.drawable._7622210368997) }
        else if (ean == "8722700027775") { iv.setImageResource(R.drawable._8722700027775) }
        else if (ean == "3564700002872") { iv.setImageResource(R.drawable._3564700002872) }
        else if (ean == "3564707079884") { iv.setImageResource(R.drawable._3564707079884) }
        else if (ean == "8000320414855") { iv.setImageResource(R.drawable._8000320414855) }
        else if (ean == "3564700755358") { iv.setImageResource(R.drawable._3564700755358) }
        else if (ean == "3564700863305") { iv.setImageResource(R.drawable._3564700863305) }
        else if (ean == "26045795")      { iv.setImageResource(R.drawable._26045795) }
        else if (ean == "3564700001165") { iv.setImageResource(R.drawable._3564700001165) }
        else if (ean == "3068320063003") { iv.setImageResource(R.drawable._3068320063003) }
        else if (ean == "3038350013804") { iv.setImageResource(R.drawable._3038350013804) }
        else if (ean == "3596710379194") { iv.setImageResource(R.drawable._3596710379194) }
        else if (ean == "4002359006715") { iv.setImageResource(R.drawable._4002359006715) }
        else if (ean == "8076808140325") { iv.setImageResource(R.drawable._8076808140325) }
        else if (ean == "3275760000257") { iv.setImageResource(R.drawable._3275760000257) }
        else if (ean == "3157623295454") { iv.setImageResource(R.drawable._3157623295454) }
        else if (ean == "3259190368095") { iv.setImageResource(R.drawable._3259190368095) }
        else if (ean == "5053083145705") { iv.setImageResource(R.drawable._5053083145705) }
        else if (ean == "3606320000170") { iv.setImageResource(R.drawable._3606320000170) }
        else if (ean == "3344428058166") { iv.setImageResource(R.drawable._3344428058166) }
        else if (ean == "3606323144239") { iv.setImageResource(R.drawable._3606323144239) }
        else if (ean == "3344428060909") { iv.setImageResource(R.drawable._3344428060909) }
        else if (ean == "8717418170936") { iv.setImageResource(R.drawable._8717418170936) }
        else if (ean == "9782016253373") { iv.setImageResource(R.drawable._9782016253373) }
        else if (ean == "9782012708808") { iv.setImageResource(R.drawable._9782012708808) }
        else if (ean == "9782014005196") { iv.setImageResource(R.drawable._9782014005196) }
        else if (ean == "9782016253076") { iv.setImageResource(R.drawable._9782016253076) }
        else if (ean == "9782016253052") { iv.setImageResource(R.drawable._9782016253052) }


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
        else if (ean == "3178530402988") { res= "Tartelettes Citron - Bonne Maman" }
        else if (ean == "3596710469079") { res= "Muffins complet - Auchan - 250 g" }
        else if (ean == "3564707000086") { res= "Spaghetti - Bio Village" }
        else if (ean == "3606323172737") { res= "Kung Fu Panda" }
        else if (ean == "7622210368997") { res= "Milka - Chocolat au lait x 2" }
        else if (ean == "8722700027775") { res= "Soupe Thaï - Knorr" }
        else if (ean == "3564700002872") { res= "Just de Pomme 1L - Jafaden" }
        else if (ean == "3564707079884") { res= "Jus d'Orange Bio 1L" }
        else if (ean == "8000320414855") { res= "Pulpe de Tomate Fine - CIRIO" }
        else if (ean == "3564700755358") { res= "Pulpe de Tomates Concassées - Turini" }
        else if (ean == "3564700863305") { res= "Ananas Tranches Entières - 565g" }
        else if (ean == "26045795")      { res= "Olives Noires Denoyautées" }
        else if (ean == "3564700001165") { res= "Pois Cassés - Notre Jardin" }
        else if (ean == "3068320063003") { res= "Evian - Eau  - 33cl" }
        else if (ean == "3038350013804") { res= "Panzani - Penne Rigate - 500g" }
        else if (ean == "3596710379194") { res= "Arôme fleur d'oranger - 200ml" }
        else if (ean == "4002359006715") { res= "Lait de coco - Suzi Wan - 200ml" }
        else if (ean == "8076808140325") { res= "Barilla - Coquillettes - 500g" }
        else if (ean == "3275760000257") { res= "Champignons noirs déshydratés - 50g" }
        else if (ean == "3157623295454") { res= "Norton - Abrasif X25 70x125mm - 80" }
        else if (ean == "3259190368095") { res= "DVD - Babe le cochon devenu berger" }
        else if (ean == "5053083145705") { res= "DVD - Baby Boss" }
        else if (ean == "3606320000170") { res= "DVD - Les Cinq Legendes" }
        else if (ean == "3344428058166") { res= "DVD - Epic" }
        else if (ean == "3606323144239") { res= "DVD - Shrek" }
        else if (ean == "3344428060909") { res= "DVD - Rio 2" }
        else if (ean == "8717418170936") { res= "DVD - Le Renard et l'Enfant" }
        else if (ean == "9782016253373") { res= "Obj CRPE - Histoire et Geographie 2018" }
        else if (ean == "9782012708808") { res= "Obj CRPE - Maths 2016" }
        else if (ean == "9782014005196") { res= "Obj CRPE - Maths" }
        else if (ean == "9782016253076") { res= "Obj Concours - Annales Français" }
        else if (ean == "9782016253052") { res= "Obj CRPE - Entrainement Français" }


        return res
    }


}
