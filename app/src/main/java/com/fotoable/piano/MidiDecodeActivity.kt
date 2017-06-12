package com.fotoable.piano.midi

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import com.fotoable.piano.R
import com.fotoable.piano.utils.ResourceUtils
import com.fotoable.piano.utils.SoundPoolSynth
import com.pdrogfer.mididroid.MidiFile
import com.pdrogfer.mididroid.event.MidiEvent
import com.pdrogfer.mididroid.event.NoteOff
import com.pdrogfer.mididroid.event.NoteOn
import com.pdrogfer.mididroid.util.MidiEventListener
import com.pdrogfer.mididroid.util.MidiProcessor
import java.io.File
import java.io.IOException

class MidiDecodeActivity : AppCompatActivity() {

    lateinit var mSoundPoolSynth: SoundPoolSynth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_midi_decode)
        mSoundPoolSynth = SoundPoolSynth(applicationContext)

        var playBt = findViewById(R.id.bt_play) as Button
        playBt.setOnClickListener { v: View? ->
            runOnUiThread {
                decodeMidi()
            }
        }
    }

    fun decodeMidi() {
        val fileName = "twinkle_tutorial_1.mid"
        val mMidiFile = File(ResourceUtils.applicationFilesDir(applicationContext) + "/" + fileName)
        if (mMidiFile.exists() && mMidiFile.isFile()) {
            Log.i("GlobeActivity", mMidiFile.getAbsolutePath())
            try {
                val midiFile = MidiFile(mMidiFile)
                val eventPrinter = EventPrinter()
                val midiProcessor = MidiProcessor(midiFile)
                midiProcessor.registerEventListener(eventPrinter, NoteOn::class.java)
                midiProcessor.registerEventListener(eventPrinter, NoteOff::class.java)

                // Start the processor:
                midiProcessor.start()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.i("GlobeActivity", "exception =" + e)
            }

        }
    }

    private inner class EventPrinter : MidiEventListener {

        override fun onStart(b: Boolean) {
            Log.i("GlobeActivity", "onStart=" + ",value =" + b)
        }

        override fun onEvent(midiEvent: MidiEvent, l: Long) {
            Log.i("GlobeActivity", "midiEvent=$midiEvent,value =$l")
            if (midiEvent is NoteOn) {
                val note = midiEvent.noteValue
                val vert = midiEvent.velocity
                val channel = midiEvent.channel
                mSoundPoolSynth.noteOn(channel, note, vert)
            } else if (midiEvent is NoteOff) {
                val note = midiEvent.noteValue
                val vert = midiEvent.velocity
                val channel = midiEvent.channel
                mSoundPoolSynth.noteOff(channel, note)
            }
        }

        override fun onStop(b: Boolean) {
            Log.i("GlobeActivity", "onStop=" + ",value =" + b)
        }
    }
}
