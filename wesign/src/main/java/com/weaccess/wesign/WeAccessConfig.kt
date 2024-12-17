package com.weaccess.wesign

object WeAccessConfig {
    var requestKey: String? =null
    private var isActive = true

    fun initialize(requestKey: String) {
        this.requestKey = requestKey

    }

    fun isActive(): Boolean {
        return isActive
    }

    fun setActive(isActive: Boolean) {
        this.isActive = isActive
    }
}