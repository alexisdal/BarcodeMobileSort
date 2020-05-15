package com.example.barcodemobilesort

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap
import kotlin.random.Random


//$ curl http://192.168.1.200:8080/get_states
//{"loc_6": 0, "loc_7": 0, "loc_4": 0, "loc_5": 1, "loc_2": 0, "loc_3": 0, "loc_0": 0, "loc_1": 1}
//$ curl http://192.168.1.200:8080/reset
//OK

interface SortAPI {
    @GET("/get_states")
    fun getStates(): Call<Map<String, Int>>

    @GET("/reset")
    fun reset(): Call<String>
}

class SortWebservice() {

    private val api: SortAPI

    init {
        val retrofit = Retrofit.Builder()
            //.baseUrl("https://www.google.com/")
            //.baseUrl("http://192.168.1.200:8080/")
            .baseUrl("http://10.155.195.46:8080/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        api = retrofit.create(SortAPI::class.java)
    }

    fun getStates(): Call<Map<String, Int>> {
        return api.getStates()
    }
    fun reset(): Call<String> {
        return api.reset()
    }
}

interface LightAPI {
    //@GET("/c")
    //fun setLightColor(@Query(value="colors", encoded=false) colors String?): Call<String>
    //fun setLightColor(
    //    @QueryMap(
    //        value = "params",
    //        encoded = false
    //    ) params: String?
    //): Call<String>

    @GET("/c")
    fun setLightColor(
        @QueryMap options: Map<String?, String?>?
    ): Call<String>


    @GET("/x")
    fun reset(): Call<String>
}
class LightWebservice() {

    private val api: LightAPI

    init {
        val retrofit = Retrofit.Builder()
            //.baseUrl("https://www.google.com/")
            //.baseUrl("http://192.168.1.200:8081/")
            .baseUrl("http://10.155.195.46:8081/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        api = retrofit.create(LightAPI::class.java)
    }

    fun setLightColor(colors: Map<String?, String?>?): Call<String> {
        //Log.d("TAG", "setLightColors:${colors}")
        return api.setLightColor(colors)
    }
    fun reset(): Call<String> {
        return api.reset()
    }
}


class Put2ShelfActivity : AppCompatActivity() {

    // http://www.droidmonk.com/kotlin/load-new-activity-pass-value-kotlin/
    companion object {
        const val EAN_MSG="msg"
    }

    var location : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_put2_shelf_activity)

        resetUILocation()
        val msg=intent.getStringExtra(EAN_MSG)
        //tv_msg.text=msg
        //Log.d("TAG", "AZERTY") // displayed each time the screen is shown
        //Log.d("TAG", msg) // displayed each time the screen is shown
        this.setEAN(msg)


    }

    fun setEAN(msg: String) {

        val tvDesc: TextView = findViewById<TextView>(R.id.textDescription)
        val ean = getEanRemoveDuplicates(msg)
        val desc = this.getDesc(ean)

        tvDesc.text = "$desc\n\nEAN:$msg"
        setEanImage(ean)
        if (desc != "unknown") {
            // OK
            //vibrate()
            vibratePredefined(VibrationEffect.EFFECT_TICK)
            // see https://developer.android.com/reference/android/os/VibrationEffect
            // beep
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 150)
            //toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } else {
            // UNKNOWN CODE
            vibrate()
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_SLS, 500)

            // display message
            //Toast.makeText( applicationContext,"unknown EAN\r\n\r\n", Toast.LENGTH_SHORT).show()

            // go back to scan screen
            Handler().postDelayed({
                //Do something after 100ms
                val intent: Intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }, 2000)
            return
        }

        // assign a location
        location = getAssignedLocation()

        // update UI with location
        setUILocation(location)

        // light up LEDs in white
        //var colors: MutableMap<String, String> =  HashMap()
        //val data: MutableMap<String, String> = HashMap()
        //data["author"] = "Marcus"
        //data["page"] = 2.toString()
        //val colors = mapOf(location : "w")
        val colors: Map<String?, String?>? = mapOf(location to  "w")
        callSetLightColor(colors)

        // https://android--code.blogspot.com/2015/08/android-imageview-set-image-from-assets.html
        callResetSort()

    }

    fun callResetSort(){
        val api = SortWebservice()
        val my_reset_call : Call<String> = api.reset()
        //val result = call.execute().body()   // will trigger a android.os.NetworkOnMainThreadException because using UI thread for network is baaaaaad :D
        my_reset_call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                //Log.d("TAG", response.body())
                callGetStates()
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("TAG", "ERROR reset: "+t.message.toString())
            }
        })
    }


    fun callGetStates() {
        val api = SortWebservice()
        val mycall : Call<Map<String, Int>> = api.getStates()
        mycall.enqueue(object : Callback<Map<String, Int>> {
            override fun onFailure(call: Call<Map<String, Int>>, t: Throwable) {
                Log.d("TAG", "ERROR get_states: "+t.message.toString())
            }

            override fun onResponse(
                call: Call<Map<String, Int>>,
                response: Response<Map<String, Int>>
            ) {
                //Log.d("TAG", response.body().toString())
                val r = response.body()
                if (r != null) { handleGetStatesResponse(r) }
            }
        })
    }

    fun callSetLightColor(colors: Map<String?, String?>?) {
        val api = LightWebservice()
        val mycall : Call<String> = api.setLightColor(colors)
        mycall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("TAG", "LED color:"+response.body())
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("TAG", "ERROR color: "+t.message.toString())
            }
        })

    }

    fun callResetLight(){
        val api = LightWebservice()
        val mycall : Call<String> = api.reset()
        //val result = call.execute().body()   // will trigger a android.os.NetworkOnMainThreadException because using UI thread for network is baaaaaad :D
        mycall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("TAG", "LED reset:"+response.body())
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("TAG", "ERROR reset light: "+t.message.toString())
            }
        })
    }


    fun handleGetStatesResponse(response: Map<String, Int>) {
        //val x = response.get("aaa")
        //Log.d("TAG", "aaa: ${x}")
        var sum = 0
        var last_winner = ""
        // https://mkyong.com/kotlin/kotlin-how-to-loop-a-map/
        for ((k, v) in response) {
            if (v > 0) {
                sum += v
                last_winner = k.toLowerCase()
            }
        }
        var success = false
        if(sum == 0) {
            // ask again
            Log.d("TAG", "nothing detected")
            this.callGetStates()
            return
        } else if (sum == 1) {
            // we have a single case selected
            Log.d("TAG", "one cell. expected=${location}  observed=${last_winner}")
            if (last_winner == this.location) {
                success = true
                this.setUILocationWin(last_winner)
                val colors: Map<String?, String?>? = mapOf(last_winner to  "g")
                callSetLightColor(colors)
            } else {
                this.setUILocationLose(last_winner)
                val colors: Map<String?, String?>? = mapOf(last_winner to  "r")
                callSetLightColor(colors)
            }
        } else {
            // several cases ==> game lost
            var msg: String = "observed: "
            this.resetUILocation()
            val c: MutableMap<String, String> = HashMap()

            for ((k, v) in response) {
                if (v >= 1) {
                    this.setUILocationLose(k.toLowerCase())
                    msg += "${k.toLowerCase()} "
                    c[k.toLowerCase()] = "r"
                }
            }
            val colors: Map<String?, String?>? = c.toMap()
            callSetLightColor(colors)
            Log.d("TAG", msg)
        }

        if (!success){
            vibrate()
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_SLS, 500)
        }

        // come back to main screen after 1s
        Handler().postDelayed({
            callResetLight()
            val intent: Intent = Intent(applicationContext, MainActivity::class.java)
            if (success) { intent.putExtra(MainActivity.SUCCESS_MSG, "SUCCESS") }
            startActivity(intent)
        }, 1000)


    }

    fun getAssignedLocation(): String {
        val r = Random.nextDouble()
        if       (r <=  1/20.0) { return "a1" }
        else if  (r <=  2/20.0) { return "b1" }
        else if  (r <=  3/20.0) { return "c1" }
        else if  (r <=  4/20.0) { return "d1" }
        else if  (r <=  5/20.0) { return "e1" }
        else if  (r <=  6/20.0) { return "a2" }
        else if  (r <=  7/20.0) { return "b2" }
        else if  (r <=  8/20.0) { return "c2" }
        else if  (r <=  9/20.0) { return "d2" }
        else if  (r <= 10/20.0) { return "e2" }
        else if  (r <= 11/20.0) { return "a3" }
        else if  (r <= 12/20.0) { return "b3" }
        else if  (r <= 13/20.0) { return "c3" }
        else if  (r <= 14/20.0) { return "d3" }
        else if  (r <= 15/20.0) { return "e3" }
        else if  (r <= 16/20.0) { return "a4" }
        else if  (r <= 17/20.0) { return "b4" }
        else if  (r <= 18/20.0) { return "c4" }
        else if  (r <= 19/20.0) { return "d4" }
        return "e4"
    }

    fun resetUILocation() {
        findViewById<TextView>(R.id.locA1).text = ""
        findViewById<TextView>(R.id.locB1).text = ""
        findViewById<TextView>(R.id.locC1).text = ""
        findViewById<TextView>(R.id.locD1).text = ""
        findViewById<TextView>(R.id.locE1).text = ""
        findViewById<TextView>(R.id.locA2).text = ""
        findViewById<TextView>(R.id.locB2).text = ""
        findViewById<TextView>(R.id.locC2).text = ""
        findViewById<TextView>(R.id.locD2).text = ""
        findViewById<TextView>(R.id.locE2).text = ""
        findViewById<TextView>(R.id.locA3).text = ""
        findViewById<TextView>(R.id.locB3).text = ""
        findViewById<TextView>(R.id.locC3).text = ""
        findViewById<TextView>(R.id.locD3).text = ""
        findViewById<TextView>(R.id.locE3).text = ""
        findViewById<TextView>(R.id.locA4).text = ""
        findViewById<TextView>(R.id.locB4).text = ""
        findViewById<TextView>(R.id.locC4).text = ""
        findViewById<TextView>(R.id.locD4).text = ""
        findViewById<TextView>(R.id.locE4).text = ""

        // reset all to gray
        findViewById<TextView>(R.id.locA1).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locB1).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locC1).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locD1).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locE1).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locA2).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locB2).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locC2).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locD2).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locE2).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locA3).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locB3).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locC3).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locD3).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locE3).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locA4).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locB4).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locC4).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locD4).setBackgroundResource(R.drawable.gray)
        findViewById<TextView>(R.id.locE4).setBackgroundResource(R.drawable.gray)
    }

    fun setUILocation(loc: String) {
        if (loc == "a1")  { findViewById<TextView>(R.id.locA1).setBackgroundResource( R.drawable.white ) }
        if (loc == "b1")  { findViewById<TextView>(R.id.locB1).setBackgroundResource( R.drawable.white ) }
        if (loc == "c1")  { findViewById<TextView>(R.id.locC1).setBackgroundResource( R.drawable.white ) }
        if (loc == "d1")  { findViewById<TextView>(R.id.locD1).setBackgroundResource( R.drawable.white ) }
        if (loc == "e1")  { findViewById<TextView>(R.id.locE1).setBackgroundResource( R.drawable.white ) }
        if (loc == "a2")  { findViewById<TextView>(R.id.locA2).setBackgroundResource( R.drawable.white ) }
        if (loc == "b2")  { findViewById<TextView>(R.id.locB2).setBackgroundResource( R.drawable.white ) }
        if (loc == "c2")  { findViewById<TextView>(R.id.locC2).setBackgroundResource( R.drawable.white ) }
        if (loc == "d2")  { findViewById<TextView>(R.id.locD2).setBackgroundResource( R.drawable.white ) }
        if (loc == "e2")  { findViewById<TextView>(R.id.locE2).setBackgroundResource( R.drawable.white ) }
        if (loc == "a3")  { findViewById<TextView>(R.id.locA3).setBackgroundResource( R.drawable.white ) }
        if (loc == "b3")  { findViewById<TextView>(R.id.locB3).setBackgroundResource( R.drawable.white ) }
        if (loc == "c3")  { findViewById<TextView>(R.id.locC3).setBackgroundResource( R.drawable.white ) }
        if (loc == "d3")  { findViewById<TextView>(R.id.locD3).setBackgroundResource( R.drawable.white ) }
        if (loc == "e3")  { findViewById<TextView>(R.id.locE3).setBackgroundResource( R.drawable.white ) }
        if (loc == "a4")  { findViewById<TextView>(R.id.locA4).setBackgroundResource( R.drawable.white ) }
        if (loc == "b4")  { findViewById<TextView>(R.id.locB4).setBackgroundResource( R.drawable.white ) }
        if (loc == "c4")  { findViewById<TextView>(R.id.locC4).setBackgroundResource( R.drawable.white ) }
        if (loc == "d4")  { findViewById<TextView>(R.id.locD4).setBackgroundResource( R.drawable.white ) }
        if (loc == "e4")  { findViewById<TextView>(R.id.locE4).setBackgroundResource( R.drawable.white ) }

    }
    fun setUILocationWin(loc: String) {
        if (loc == "a1")  { findViewById<TextView>(R.id.locA1).setBackgroundResource( R.drawable.green ) }
        if (loc == "b1")  { findViewById<TextView>(R.id.locB1).setBackgroundResource( R.drawable.green ) }
        if (loc == "c1")  { findViewById<TextView>(R.id.locC1).setBackgroundResource( R.drawable.green ) }
        if (loc == "d1")  { findViewById<TextView>(R.id.locD1).setBackgroundResource( R.drawable.green ) }
        if (loc == "e1")  { findViewById<TextView>(R.id.locE1).setBackgroundResource( R.drawable.green ) }
        if (loc == "a2")  { findViewById<TextView>(R.id.locA2).setBackgroundResource( R.drawable.green ) }
        if (loc == "b2")  { findViewById<TextView>(R.id.locB2).setBackgroundResource( R.drawable.green ) }
        if (loc == "c2")  { findViewById<TextView>(R.id.locC2).setBackgroundResource( R.drawable.green ) }
        if (loc == "d2")  { findViewById<TextView>(R.id.locD2).setBackgroundResource( R.drawable.green ) }
        if (loc == "e2")  { findViewById<TextView>(R.id.locE2).setBackgroundResource( R.drawable.green ) }
        if (loc == "a3")  { findViewById<TextView>(R.id.locA3).setBackgroundResource( R.drawable.green ) }
        if (loc == "b3")  { findViewById<TextView>(R.id.locB3).setBackgroundResource( R.drawable.green ) }
        if (loc == "c3")  { findViewById<TextView>(R.id.locC3).setBackgroundResource( R.drawable.green ) }
        if (loc == "d3")  { findViewById<TextView>(R.id.locD3).setBackgroundResource( R.drawable.green ) }
        if (loc == "e3")  { findViewById<TextView>(R.id.locE3).setBackgroundResource( R.drawable.green ) }
        if (loc == "a4")  { findViewById<TextView>(R.id.locA4).setBackgroundResource( R.drawable.green ) }
        if (loc == "b4")  { findViewById<TextView>(R.id.locB4).setBackgroundResource( R.drawable.green ) }
        if (loc == "c4")  { findViewById<TextView>(R.id.locC4).setBackgroundResource( R.drawable.green ) }
        if (loc == "d4")  { findViewById<TextView>(R.id.locD4).setBackgroundResource( R.drawable.green ) }
        if (loc == "e4")  { findViewById<TextView>(R.id.locE4).setBackgroundResource( R.drawable.green ) }

    }
    fun setUILocationLose(loc: String) {
        if (loc == "a1")  { findViewById<TextView>(R.id.locA1).setBackgroundResource( R.drawable.red ) }
        if (loc == "b1")  { findViewById<TextView>(R.id.locB1).setBackgroundResource( R.drawable.red ) }
        if (loc == "c1")  { findViewById<TextView>(R.id.locC1).setBackgroundResource( R.drawable.red ) }
        if (loc == "d1")  { findViewById<TextView>(R.id.locD1).setBackgroundResource( R.drawable.red ) }
        if (loc == "e1")  { findViewById<TextView>(R.id.locE1).setBackgroundResource( R.drawable.red ) }
        if (loc == "a2")  { findViewById<TextView>(R.id.locA2).setBackgroundResource( R.drawable.red ) }
        if (loc == "b2")  { findViewById<TextView>(R.id.locB2).setBackgroundResource( R.drawable.red ) }
        if (loc == "c2")  { findViewById<TextView>(R.id.locC2).setBackgroundResource( R.drawable.red ) }
        if (loc == "d2")  { findViewById<TextView>(R.id.locD2).setBackgroundResource( R.drawable.red ) }
        if (loc == "e2")  { findViewById<TextView>(R.id.locE2).setBackgroundResource( R.drawable.red ) }
        if (loc == "a3")  { findViewById<TextView>(R.id.locA3).setBackgroundResource( R.drawable.red ) }
        if (loc == "b3")  { findViewById<TextView>(R.id.locB3).setBackgroundResource( R.drawable.red ) }
        if (loc == "c3")  { findViewById<TextView>(R.id.locC3).setBackgroundResource( R.drawable.red ) }
        if (loc == "d3")  { findViewById<TextView>(R.id.locD3).setBackgroundResource( R.drawable.red ) }
        if (loc == "e3")  { findViewById<TextView>(R.id.locE3).setBackgroundResource( R.drawable.red ) }
        if (loc == "a4")  { findViewById<TextView>(R.id.locA4).setBackgroundResource( R.drawable.red ) }
        if (loc == "b4")  { findViewById<TextView>(R.id.locB4).setBackgroundResource( R.drawable.red ) }
        if (loc == "c4")  { findViewById<TextView>(R.id.locC4).setBackgroundResource( R.drawable.red ) }
        if (loc == "d4")  { findViewById<TextView>(R.id.locD4).setBackgroundResource( R.drawable.red ) }
        if (loc == "e4")  { findViewById<TextView>(R.id.locE4).setBackgroundResource( R.drawable.red ) }
    }

    fun getEanRemoveDuplicates(ean: String): String {
        var res = ean;
        if       (ean == "XA2667421252I0 Y") { res = "3561292290977" } // google pixel 3XL
        //else if  (ean == "3561292290885")    { res = "3561292290977" }
        //else if  (ean == "3561292290885")    { res = "3561292290977" }
        return res
    }

    fun setEanImage(ean: String) {
        val iv: ImageView = findViewById<ImageView>(R.id.imageView)
        if      (ean == "8076809574631") { iv.setImageResource(R.drawable._8076809574631) }
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

        // smartphones
        else if (ean == "888462061124") { iv.setImageResource(R.drawable._888462061124) }
        else if (ean == "190198745606") { iv.setImageResource(R.drawable._190198745606) }
        else if (ean == "3561292290977") { iv.setImageResource(R.drawable._3561292290977) }
        else if (ean == "3561292290885") { iv.setImageResource(R.drawable._3561292290885) }
        else if (ean == "888462065740") { iv.setImageResource(R.drawable._888462065740) }
        else if (ean == "190198482570") { iv.setImageResource(R.drawable._190198482570) }
        else if (ean == "190198482259") { iv.setImageResource(R.drawable._190198482259) }
        else if (ean == "888462566353") { iv.setImageResource(R.drawable._888462566353) }
        else if (ean == "888462804578") { iv.setImageResource(R.drawable._888462804578) }
        else if (ean == "885909783786") { iv.setImageResource(R.drawable._885909783786) }
        else if (ean == "888462560870") { iv.setImageResource(R.drawable._888462560870) }
        else if (ean == "888462732918") { iv.setImageResource(R.drawable._888462732918) }


        else { iv.setImageResource(R.drawable._unknown) }

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
        // smartphones
        else if (ean == "888462061124") { res= "IPHONE 6 SPACE GRAY 16GB DEMO" }
        else if (ean == "190198745606") { res= "IPHONE 8 Plus Red, 64MB" }
        else if (ean == "3561292290977") { res= "Google Pixel 3XL" }
        else if (ean == "3561292290885") { res= "Google Pixel 3" }
        else if (ean == "888462065740")  { res= "Apple iPhone 6 Plus Gold 16GB" }
        else if (ean == "190198482570")  { res= "Apple iPhone 8 Plus Gold 64GB" }
        else if (ean == "190198482259")  { res= "Apple iPhone 8 Plus Silver 64GB" }
        else if (ean == "888462566353")  { res= "Apple iPhone 6s Plus Gold 16GB" }
        else if (ean == "888462804578")  { res= "Apple iPhone SE Rose Gold 16GB" }
        else if (ean == "885909783786")  { res= "Apple iPhone 5s Space Grey 16GB" }
        else if (ean == "888462560870")  { res= "Apple iPhone 6s Rose Gold 16GB" }
        else if (ean == "888462732918")  { res= "Apple iPhone SE Space Grey 16GB" }

        return res
    }
    // Extension property to check whether device has Vibrator
    val Context.hasVibrator:Boolean
        get() {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            return vibrator.hasVibrator()
        }

    // Extension method to vibrate a phone programmatically
    fun Context.vibrate(milliseconds:Long = 500){
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Check whether device/hardware has a vibrator
        val canVibrate:Boolean = vibrator.hasVibrator()

        if(canVibrate){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                // void vibrate (VibrationEffect vibe)
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        milliseconds,
                        // The default vibration strength of the device.
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }else{
                // This method was deprecated in API level 26
                vibrator.vibrate(milliseconds)
            }
        }
    }


    // Extension method to vibrate a phone programmatically
    fun Context.vibratePredefined(effectId:Int = VibrationEffect.EFFECT_TICK ){
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Check whether device/hardware has a vibrator
        val canVibrate:Boolean = vibrator.hasVibrator()

        if(canVibrate){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vibrator.vibrate(
                    VibrationEffect.createPredefined(effectId)
                )
            }else{
                // This method was deprecated in API level 26
                vibrator.vibrate(100)
            }
        }
    }

}
