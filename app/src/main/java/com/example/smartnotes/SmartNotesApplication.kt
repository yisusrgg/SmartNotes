package com.example.smartnotes

import android.app.Application
import com.example.smartnotes.data.AppContainer
import com.example.smartnotes.data.AppDataContainer

class SmartNotesApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer


    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}