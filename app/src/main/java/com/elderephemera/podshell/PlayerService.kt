package com.elderephemera.podshell

import android.app.Service
import android.content.Intent

class PlayerService : Service() {
    override fun onBind(intent: Intent?) = binder
    private val binder = Binder(this)
    class Binder(val service: Service) : android.os.Binder()
}