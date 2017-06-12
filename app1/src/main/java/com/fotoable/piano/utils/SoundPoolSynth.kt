package com.fotoable.piano.utils


import android.content.Context
import android.content.res.Resources
import android.media.SoundPool
import android.os.ParcelFileDescriptor
import android.util.Log
import com.fotoable.piano1.R
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

class SoundPoolSynth(con: Context) {
    private var activeStreams: Queue<Int>? = null
    private val half_step = Math.pow(2.0, 0.08333333333333333).toFloat()
    private val lowNote = 36
    var mContext: Context
    private var initialized = java.lang.Boolean.valueOf(false)
    private var mSoundPool: SoundPool? = null
    private var mWavMap: HashMap<String, Int>? = HashMap()
    private val maxStreams = 16
    var pitchBendPerChannel: FloatArray
    private val topNote = 108
    private val whole_step = Math.pow(2.0, 0.16666666666666666).toFloat()

//    fun onCreate(con: Context) {
//
//    }

    init {
        this.mContext = con
        this.pitchBendPerChannel = FloatArray(128)
        this.activeStreams = LinkedList()
        initSoundPool(con)
    }

    @Synchronized fun onPause() {
        freeSoundPool()
    }

    @Synchronized fun onStop() {
        freeSoundPool()
    }

    @Synchronized fun onResume() {
        initSoundPool(mContext)
    }

    private fun freeSoundPool() {
        synchronized(this.initialized) {
            initialized = java.lang.Boolean.valueOf(false)
            clearSounds()
            releaseSoundPool()
        }
    }

    private fun initSoundPool(context: Context) {
        synchronized(this.initialized) {
            if (this.mSoundPool == null) {
                Log.d(TAG, "mSoundPool started initializing")
                this.mSoundPool = SoundPool(16, 3, 0)
                loadWAVFiles(context)
                Log.d(TAG, "mSoundPool finished initializing")
            } else {
                Log.d(TAG, "mSoundPool already initialized")
            }
            initialized = java.lang.Boolean.valueOf(true)
        }
    }

    private fun releaseSoundPool() {
        if (this.mSoundPool != null) {
            this.mSoundPool!!.release()
            this.mSoundPool = null
        }
        this.mWavMap = null
    }

    private fun clearSounds() {
        if (this.activeStreams != null) {
            for (streamID in this.activeStreams!!) {
                this.mSoundPool!!.stop(streamID!!.toInt())
            }
            this.activeStreams!!.clear()
        }
        if (this.mSoundPool != null && this.mWavMap != null) {
            for (soundID in this.mWavMap!!.values) {
                this.mSoundPool!!.unload(soundID.toInt())
            }
        }
    }

    private fun leaveBreadcrumbForFile(sfFile: File) {
        var breadcrumb = "SampleID of 0 returned for " + sfFile.absolutePath + "."
        if (sfFile.exists()) {
            breadcrumb = breadcrumb + "file exists. "
            try {
                val fd = ParcelFileDescriptor.open(sfFile, 268435456)
                if (fd != null) {
                    fd.close()
                } else {
                    breadcrumb = breadcrumb + "  File descriptor returned by open() was null."
                }
            } catch (e: FileNotFoundException) {
                breadcrumb = breadcrumb + " FileNotFoundException was thrown.  This probably means no read permission since the file exists."
                Log.e(TAG, " FileNotFoundExcpetion opening: " + sfFile.absolutePath, e)
            } catch (e2: IOException) {
                Log.e(TAG, "error loading " + sfFile.absolutePath, e2)
                breadcrumb = breadcrumb + "  An IOException was thrown."
            }

        } else {
            breadcrumb = breadcrumb + "file does not exist. "
        }
        //        Crittercism.leaveBreadcrumb(breadcrumb);
    }

    private fun loadWAVFiles(context: Context) {
        var wav: String
        var sfFile: File
        var sampleID: Int
        this.mWavMap = HashMap()
        val dir = this.mContext!!.filesDir
        var numErrors = 0
        for (i in 2..7) {
            for (str in degrees) {
                wav = str + Integer.toString(i) + "s_16"
                if (!this.mWavMap!!.containsKey(wav)) {
                    sfFile = File(dir, wav + ".wav")
                    sampleID = this.mSoundPool!!.load(sfFile.absolutePath, 0)
                    if (sampleID == 0 && extractNamedResource(context, wav, sfFile)) {
                        sampleID = this.mSoundPool!!.load(sfFile.absolutePath, 0)
                    }
                    if (sampleID == 0) {
                        numErrors++
                        leaveBreadcrumbForFile(sfFile)
                    } else if (this.mWavMap!![wav] == null) {
                        this.mWavMap!!.put(wav, Integer.valueOf(sampleID))
                    }
                }
            }
        }
        wav = "c8s_16"
        sfFile = File(dir, wav + ".wav")
        sampleID = this.mSoundPool!!.load(sfFile.absolutePath, 0)
        if (sampleID == 0 && extractNamedResource(context, wav, sfFile)) {
            sampleID = this.mSoundPool!!.load(sfFile.absolutePath, 0)
        }
        if (sampleID == 0) {
            numErrors++
            leaveBreadcrumbForFile(sfFile)
        } else if (this.mWavMap!![wav] == null) {
            this.mWavMap!!.put(wav, Integer.valueOf(sampleID))
        }
        val clickFile = File(dir, CLICK_NAME + ".wav")
        sampleID = this.mSoundPool!!.load(clickFile.absolutePath, 0)
        if (sampleID == 0) {
            numErrors++
            leaveBreadcrumbForFile(clickFile)
        }
        if (this.mWavMap!![CLICK_NAME] == null) {
            this.mWavMap!!.put(CLICK_NAME, Integer.valueOf(sampleID))
        }
        if (numErrors > 0) {
            reportSoundPoolError("" + numErrors + " samples had a sampleID of 0")
            //            MagicApplication.getInstance().showToast(MagicApplication.getContext().getString(R.string.error_opening_file), 1);
        }
    }

    fun noteToWav(note: Int): String {
        var note = note
        if (note >= 108) {
            note = 107
        } else if (note <= 36) {
            note = 36
        }
        return degrees[(note + 3) % 12] + Integer.toString(note / 12 - 1) + "s_16"
    }

    private fun needStep(note: Int): Int {
        val degree = (note + 3) % 12
        if (note >= 108 || note < 36) {
            return 0
        }
        if (degree > 1 && degrees[degree] === degrees[degree - 2]) {
            return 2
        }
        if (degree <= 0 || degrees[degree] !== degrees[degree - 1]) {
            return 0
        }
        return 1
    }

    fun noteOn(channel: Int, pitch: Int, velocity: Int) {
        var channel = channel
        var pitch = pitch
        synchronized(this.initialized) {
            if (this.initialized) {
                if (channel < 0) {
                    Log.w(TAG, "Invalid channel $channel. Setting to 0.")
                    channel = 0
                } else if (channel >= this.pitchBendPerChannel!!.size) {
                    Log.w(TAG, "Invalid channel " + channel + " setting to " + (this.pitchBendPerChannel!!.size - 1) + ".")
                    channel = this.pitchBendPerChannel!!.size - 1
                }
                var rate = 1.0f
                val volume = volumeScale * velocity.toFloat() / 127.0f
                while (pitch > 108) {
                    pitch -= 12
                }
                while (pitch < 36) {
                    pitch += 12
                }
                val s = noteToWav(pitch)
                when (needStep(pitch)) {
                    1 -> rate = this.half_step
                    2 -> rate = this.whole_step
                }
                if (this.pitchBendPerChannel!![channel] != 0.0f) {
                    rate = (rate.toDouble() * Math.pow(2.0, this.pitchBendPerChannel!![channel].toDouble() / 12.0)).toFloat()
                }
                playSound(s, volume, rate)
                return
            }
            Log.d(TAG, "getInitialized() returned false, SoundPoolSynth.noteOn() returning early")
        }
    }

    private fun playSound(id: String?, volume: Float, rate: Float) {
        if (id != null && this.mWavMap != null) {
            val sound = this.mWavMap!![id] as Int
            if (sound != null) {
                val streamID = this.mSoundPool!!.play(sound.toInt(), volume, volume, 0, 0, rate)
                if (streamID != 0) {
                    while (this.activeStreams!!.size >= 16) {
                        this.mSoundPool!!.stop((this.activeStreams!!.poll() as Int).toInt())
                    }
                    this.activeStreams!!.offer(Integer.valueOf(streamID))
                }
            }
        }
    }

    fun playClick() {
        if (mClickVolume > 0.0f) {
            playSound(CLICK_NAME, mClickVolume, 1.0f)
        }
    }

    fun noteOff(channel: Int, pitch: Int) {
        Log.i(TAG, "channel =$channel,pitch=$pitch")
    }

    fun pitchBend(channel: Int, pitchDiff: Float) {
        if (channel >= 0 && channel < 128) {
            this.pitchBendPerChannel[channel] = pitchDiff
        }
    }

    private fun reportSoundPoolError(extra: String) {
        //        SharedPreferences prefs = this.mContext.getSharedPreferences(StartupActivity.STARTUP_PREFS, 0);
        //        String playerid = UserManager.getInstance().player();
        //        int appVersion = prefs.getInt(StartupActivity.PREV_VERSION, -1);
        //        Crittercism.leaveBreadcrumb("Player " + playerid + " had an error in SoundPool: " + extra + " Upgraded from app version: " + appVersion + ", OS version: " + VERSION.SDK_INT);
        val errorMessage = "Error in SoundPool: Sample IDs are 0"
        //        Crittercism.logHandledException(new RuntimeException(errorMessage));
        Log.e(TAG, errorMessage)
    }

    companion object {
        private val CLICK_NAME = "click"
        private val TAG = SoundPoolSynth::class.java.name
        private val degrees = arrayOf("a", "a", "a", "c", "c", "d", "d", "d", "f", "f", "g", "g")
        private var mClickVolume = 1.0f
        private var sShowToast = true
        var volumeScale = 0.5f

        fun setClickVolume(volume: Float) {
            mClickVolume = volume
        }

        fun prepareResources(context: Context) {
            var wav: String
            var sfFile: File
            val dir = context.filesDir
            val clickFile = File(dir, CLICK_NAME + ".wav")
            if (!(clickFile.exists() || extractNamedResource(context, CLICK_NAME, clickFile))) {
                showToastOnce()
            }
            val syncFile = File(dir, "sync_loop.wav")
            if (!(syncFile.exists() || extractNamedResource(context, "sync_loop", syncFile))) {
                showToastOnce()
            }
            for (i in 2..7) {
                for (str in degrees) {
                    wav = str + Integer.toString(i) + "s_16"
                    sfFile = File(dir, wav + ".wav")
                    if (!(sfFile.exists() || extractNamedResource(context, wav, sfFile))) {
                        showToastOnce()
                    }
                }
            }
            wav = "c8s_16"
            sfFile = File(dir, wav + ".wav")
            if (!sfFile.exists() && !extractNamedResource(context, wav, sfFile)) {
                showToastOnce()
            }
        }

        private fun showToastOnce() {
            if (sShowToast) {
                //            MagicApplication.getInstance().showToast(MagicApplication.getContext().getString(R.string.error_opening_file), 1);
                sShowToast = false
            }
        }

        fun setVolumeScaleForHeadphones(state: Int) {
            if (state == 1) {
                volumeScale = 0.2f
            } else {
                volumeScale = 0.5f
            }
        }

        fun extractNamedResource(context: Context, name: String, sfFile: File): Boolean {
            try {
                try {
                    try {
                        ResourceUtils.extractStreamToFile(context.resources.openRawResource(R.raw::class.java.getField(name).getInt(null)), sfFile, true)
                        return true
                    } catch (e: IOException) {
                        Log.e(TAG, "couldn't open stream for " + name)
                        //                    Crittercism.leaveBreadcrumb("couldn't open stream for " + name + ", " + phone_storage_free() + " bytes free on device.");
                        logExceptionOnce(e)
                        return false
                    }

                } catch (e2: Resources.NotFoundException) {
                    Log.e(TAG, "resource not found: " + name)
                    //                Crittercism.leaveBreadcrumb("raw resource not found: " + name);
                    logExceptionOnce(e2)
                    return false
                }

            } catch (e3: NoSuchFieldException) {
                Log.i(TAG, "resource field not found: " + name)
                return false
            } catch (e4: IllegalArgumentException) {
                Log.e(TAG, "IllegalArgumentException: " + name)
                //            Crittercism.leaveBreadcrumb("IllegalArgumentException: " + name);
                logExceptionOnce(e4)
                return false
            } catch (e5: IllegalAccessException) {
                Log.e(TAG, "IllegalAccessException:" + name)
                //            Crittercism.leaveBreadcrumb("IllegalAccessException:" + name);
                logExceptionOnce(e5)
                return false
            }

        }

        private fun logExceptionOnce(e: Exception) {
            e.printStackTrace()
        }
    }
}
