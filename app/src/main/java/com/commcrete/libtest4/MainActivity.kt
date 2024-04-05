package com.commcrete.libtest4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ustadmobile.codec2.Codec2

class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Codec2.create(Codec2.CODEC2_MODE_700)
    }
}