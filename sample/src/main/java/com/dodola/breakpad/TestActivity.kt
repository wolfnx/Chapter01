package com.dodola.breakpad

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sample.breakpad.BreakpadInit
import java.io.File

class TestActivity : Activity() {

    private lateinit var mCrashBt: TextView
    private var mCrashFile: File? = null
    companion object {
        private var TAG = TestActivity::class.java.simpleName
        private const val CRASH_LIB = "crash-lib"
        private const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100
    }

    init {
        System.loadLibrary(CRASH_LIB)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mCrashBt = findViewById(R.id.crash_btn)
        initLogic()
    }

    private fun initLogic() {
        checkPermission()
        mCrashBt.setOnClickListener {
            initCrashDir(false)
            crash()
        }
    }

    private fun checkPermission() {
        val permissionCode = ContextCompat.checkSelfPermission(this
                                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)
        when(permissionCode) {
            PackageManager.PERMISSION_GRANTED -> initCrashDir(true)
            else -> ActivityCompat.requestPermissions(this
                , arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                , WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    private fun initCrashDir(external: Boolean) {
        Log.d(TAG, "Init crash dir is external: $external")
        mCrashFile?.let {
            Log.d(TAG, "mCrashFile is " + it.absoluteFile)
            BreakpadInit.initBreakpad(it.absolutePath)
        } ?: run {
            Log.d(TAG, "mCrashFile is null.")
            mCrashFile = when (external) {
                true -> File(Environment.getExternalStorageDirectory(), "crashDump")
                else -> File(filesDir, "crashDump")
            }
            Log.d(TAG, "mCrashFile mkdir before: " + mCrashFile?.exists())
            mCrashFile?.takeIf { !it.exists() }?.mkdir()
            Log.d(TAG, "mCrashFile mkdir after: " + mCrashFile?.exists())
            BreakpadInit.initBreakpad(mCrashFile?.absolutePath)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //权限申请成功，创建crashDump
        grantResults.forEach { item ->
            if(requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
               when(item) {
                   PackageManager.PERMISSION_GRANTED -> initCrashDir(true)
                   PackageManager.PERMISSION_DENIED -> {
                       mCrashFile = null
                       initCrashDir(false)
                   }
               }
            }
        }
    }

    private external fun crash()
}