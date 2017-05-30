package com.study.mp3lame

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by lvgy on 2017/5/28.
 */
class MP3Recorder {

    init {
        LogUtil.e("find library")
        System.loadLibrary("mp3lame")
    }

    companion object{
        /**
         * 开始录音
         */
        val MSG_REC_STARTED = 1
        /**
         * 结束录音
         */
        val MSG_REC_STOPPED = 2
        /**
         * 暂停录音
         */
        val MSG_REC_PAUSE = 3
        /**
         * 继续录音
         */
        val MSG_REC_RESTORE = 4
        /**
         * 缓冲区挂了,采样率手机不支持
         */
        val MSG_ERROR_GET_MIN_BUFFERSIZE = -1
        /**
         * 创建文件时发生错误
         */
        val MSG_ERROR_CREATE_FILE = -2
        /**
         * 初始化录音器时发生错误
         */
        val MSG_ERROR_REC_START = -3
        /**
         * 录音的时候出错
         */
        val MSG_ERROR_AUDIO_RECORD = -4
        /**
         * 编码时发生错误
         */
        val MSG_ERROR_AUDIO_ENCODE = -5
        /**
         * 写文件时发生错误
         */
        val MSG_ERROR_WRITE_FILE = -6
        /**
         * 没法关闭文件流
         */
        val MSG_ERROR_CLOSE_FILE = -7

        init {
            LogUtil.e("companion")
        }
    }


    var mListener: AudioStageListener? = null
    var filePath: String = ""
    private var sampleRate: Int = 0
    private var isRecording = false
    private var isPause = false
    private var handler: Handler? = null
    private var voiceLevel: Int = 0

    fun setSampleRate(sampleRate: Int): MP3Recorder {
        this.sampleRate = sampleRate
        return this
    }

    /**
     * 在SD卡上创建文件

     * @throws IOException
     */
    @Throws(IOException::class)
    fun createSDFile(fileName: String): File {
        val file = File(fileName)
        if (!file.exists())
            if (file.isDirectory) {
                file.mkdirs()
            } else {
                file.createNewFile()
            }
        return file
    }

    /**
     * 初始化录制参数
     */
    fun init(inSampleRate: Int, outChannel: Int,
             outSampleRate: Int, outBitRate: Int) {
        init(inSampleRate, outChannel, outSampleRate, outBitRate, 7)
    }

    /**
     * 初始化录制参数 quality:0=很好很慢 9=很差很快
     */
    external fun init(inSampleRate: Int, outChannel: Int,
                      outSampleRate: Int, outBitRate: Int, quality: Int)

    /**
     * 音频数据编码(PCM左进,PCM右进,MP3输出)
     */
    external fun encode(buffer_l: ShortArray, buffer_r: ShortArray,
                        samples: Int, mp3buf: ByteArray): Int

    /**
     * 据说录完之后要刷干净缓冲区
     */
    external fun flush(mp3buf: ByteArray): Int

    /**
     * 结束编码
     */
    external fun close()

    fun setFilePath(filePath: String): MP3Recorder {
        this.filePath = filePath
        return this
    }

    /**
     * 开片
     */
    fun start() {
        if (isRecording) {
            return
        }

        object : Thread() {
            override fun run() {
                android.os.Process
                        .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
                // 根据定义好的几个配置，来获取合适的缓冲大小
                val minBufferSize = AudioRecord.getMinBufferSize(
                        sampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT)
                if (minBufferSize < 0) {
                    if (handler != null) {
                        handler!!.sendEmptyMessage(MSG_ERROR_GET_MIN_BUFFERSIZE)
                    }
                    return
                }
                val audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC, sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2)
                // 5秒的缓冲
                val buffer = ShortArray(sampleRate * (16 / 8) * 5)
                val mp3buffer = ByteArray((7200 + buffer.size.toDouble() * 2.0 * 1.25).toInt())

                var output: FileOutputStream? = null
                try {
                    val file = createSDFile(filePath)
                    output = FileOutputStream(file)
                } catch (e: FileNotFoundException) {
                    if (handler != null) {
                        handler!!.sendEmptyMessage(MSG_ERROR_CREATE_FILE)
                    }
                    return
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                init(sampleRate, 1, sampleRate, 32)
                isRecording = true // 录音状态
                isPause = false // 录音状态
                try {
                    try {
                        audioRecord.startRecording() // 开启录音获取音频数据

                        if (mListener != null) {
                            mListener!!.wellPrepared()
                        }
                    } catch (e: IllegalStateException) {
                        // 不给录音...
                        if (handler != null) {
                            handler!!.sendEmptyMessage(MSG_ERROR_REC_START)
                        }
                        return
                    }

                    try {
                        // 开始录音
                        if (handler != null) {
                            handler!!.sendEmptyMessage(MSG_REC_STARTED)
                        }

                        var readSize: Int
                        var pause = false
                        while (isRecording) {
                            /*--暂停--*/
                            if (isPause) {
                                if (!pause) {
                                    handler!!.sendEmptyMessage(MSG_REC_PAUSE)
                                    pause = true
                                }
                                continue
                            }
                            if (pause) {
                                handler!!.sendEmptyMessage(MSG_REC_RESTORE)
                                pause = false
                            }
                            /*--End--*/
                            /*--实时录音写数据--*/
                            readSize = audioRecord.read(buffer, 0,
                                    minBufferSize)
                            voiceLevel = getVoiceSize(readSize, buffer)
                            if (readSize < 0) {
                                if (handler != null) {
                                    handler!!.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD)
                                }
                                break
                            } else if (readSize == 0) {
                            } else {
                                val encResult = encode(buffer,
                                        buffer, readSize, mp3buffer)
                                if (encResult < 0) {
                                    if (handler != null) {
                                        handler!!.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE)
                                    }
                                    break
                                }
                                if (encResult != 0) {
                                    try {
                                        output!!.write(mp3buffer, 0, encResult)
                                    } catch (e: IOException) {
                                        if (handler != null) {
                                            handler!!.sendEmptyMessage(MSG_ERROR_WRITE_FILE)
                                        }
                                        break
                                    }

                                }
                            }
                            /*--End--*/
                        }
                        /*--录音完--*/
                        val flushResult = flush(mp3buffer)
                        if (flushResult < 0) {
                            if (handler != null) {
                                handler!!.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE)
                            }
                        }
                        if (flushResult != 0) {
                            try {
                                output!!.write(mp3buffer, 0, flushResult)
                            } catch (e: IOException) {
                                if (handler != null) {
                                    handler!!.sendEmptyMessage(MSG_ERROR_WRITE_FILE)
                                }
                            }

                        }
                        try {
                            output!!.close()
                        } catch (e: IOException) {
                            if (handler != null) {
                                handler!!.sendEmptyMessage(MSG_ERROR_CLOSE_FILE)
                            }
                        }

                        /*--End--*/
                    } finally {
                        audioRecord.stop()
                        audioRecord.release()
                    }
                } finally {
                    close()
                    isRecording = false
                }
                if (handler != null) {
                    handler!!.sendEmptyMessage(MSG_REC_STOPPED)
                }
            }
        }.start()
    }

    fun stop() {
        isRecording = false
    }

    fun pause() {
        isPause = true
    }

    fun restore() {
        isPause = false
    }

    fun isRecording(): Boolean {
        return isRecording
    }

    fun isPause(): Boolean {
        if (!isRecording) {
            return false
        }
        return isPause
    }

    // 获得声音的level
    fun getVoiceSize(r: Int, buffer: ShortArray): Int {
        if (isRecording) {
            try {
                var v: Long = 0
                // 将 buffer 内容取出，进行平方和运算
                for (i in buffer.indices) {
                    v += (buffer[i] * buffer[i]).toLong()
                }
                // 平方和除以数据总长度，得到音量大小。
                val mean = v / r.toDouble()
                val volume = 10 * Math.log10(mean)
                return volume.toInt() / 10 - 1
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        return 1
    }

    fun getVoiceLevel(): Int {
        return voiceLevel
    }

    fun setOnAudioStageListener(listener: AudioStageListener) {
        mListener = listener
    }

    fun setHandler(handler: Handler) {
        this.handler = handler
    }

    interface AudioStageListener {
        fun wellPrepared()
    }

}