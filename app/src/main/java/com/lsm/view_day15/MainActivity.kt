package com.lsm.view_day15

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lockPatternView.setPatterChangeListener(object :LockPatternView.OnPatterChangeListener{
            override fun onPatterSuccess(password: String) {
                Toast.makeText(this@MainActivity,"密码为：${password}",Toast.LENGTH_LONG).show()
            }

            override fun onPatterFailure() {
                Toast.makeText(this@MainActivity,"设置错误",Toast.LENGTH_LONG).show()
            }

            override fun onPatterStart(isStart: Boolean) {

            }

        })
    }
}
