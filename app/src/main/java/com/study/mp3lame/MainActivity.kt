package com.study.mp3lame

import android.content.Context
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import com.study.mp3lame.MP3Recorder.AudioStageListener
import java.io.File


class MainActivity : AppCompatActivity(), AudioStageListener {

    init {
        LogUtil.e("init activity")
    }

    private val DEFAULT_SAMPLING_RATE = 44100

    val mp3Recorder: MP3Recorder = MP3Recorder()
            .setSampleRate(DEFAULT_SAMPLING_RATE)
            .setFilePath(getInnerSDCardPath() + File.separator + "voice.mp3")

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MP3Recorder.MSG_REC_STARTED -> {
                    LogUtil.e("Record Start")
                    toast(this@MainActivity, "Record Start")
                }
                MP3Recorder.MSG_REC_STOPPED -> {
                    toast(this@MainActivity, "Record Stop")
                }
                MP3Recorder.MSG_ERROR_GET_MIN_BUFFERSIZE -> toast(this@MainActivity, "手机不支持录音功能")
                MP3Recorder.MSG_ERROR_CREATE_FILE -> toast(this@MainActivity, "录音文件生成异常")
                MP3Recorder.MSG_ERROR_REC_START -> toast(this@MainActivity, "录音无法开始")
                MP3Recorder.MSG_ERROR_AUDIO_RECORD -> {
                    toast(this@MainActivity, "无法录音，请检查是否禁止了权限")
                }
                MP3Recorder.MSG_ERROR_AUDIO_ENCODE -> toast(this@MainActivity, "文件编码失败")
                MP3Recorder.MSG_ERROR_WRITE_FILE -> toast(this@MainActivity, "文件保存失败")
                MP3Recorder.MSG_ERROR_CLOSE_FILE -> toast(this@MainActivity, "文件保存失败")
                else -> {
                }
            }
        }
    }

    override fun wellPrepared() {
        LogUtil.e("this is run")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mp3Recorder.setHandler(handler)
        mp3Recorder.setOnAudioStageListener(this)
        val startButton = findViewById(R.id.startRecord) as Button
        startButton.setOnClickListener {
            mp3Recorder.start()
        }

        val stopButton = findViewById(R.id.stopButton) as Button
        stopButton.setOnClickListener {
            mp3Recorder.stop()
        }

        val playButton = findViewById(R.id.button) as Button
        playButton.setOnClickListener {
            //            mp3Recorder.stop()
        }


    }

    fun getInnerSDCardPath(): String {
        return Environment.getExternalStorageDirectory().path
    }


    private fun toast(context: Context, string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }
}
