package com.example

import android.app.Application
import com.example.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlQadiApplication : Application() {
    lateinit var container: AppContainer
    private val _containerVersion = MutableStateFlow(0)
    val containerVersion = _containerVersion.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    fun resetContainer() {
        container = AppContainer(this)
        _containerVersion.value++
    }
}
