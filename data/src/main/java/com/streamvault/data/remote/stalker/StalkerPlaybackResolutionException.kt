package com.streamvault.data.remote.stalker

import java.io.IOException

class StalkerPlaybackResolutionException(
    message: String,
    cause: Throwable? = null
) : IOException(message, cause)
