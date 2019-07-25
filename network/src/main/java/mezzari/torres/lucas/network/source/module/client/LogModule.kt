package mezzari.torres.lucas.network.source.module.client

import mezzari.torres.lucas.network.source.Network
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * @author Lucas T. Mezzari
 * @since 25/07/2019
 *
 * This module should be added as the last one to show logs of the other modules
 */
class LogModule : Network.OkHttpClientLevelModule {
    //Instantiate the logging interceptor
    private val loggingInterceptor : HttpLoggingInterceptor = HttpLoggingInterceptor()

    init {
        //Set it's level to body (shows everything)
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    }

    override fun onClientBuilderCreated(okHttpClientBuilder: OkHttpClient.Builder) {
        //Add the interceptor to the client
        okHttpClientBuilder.addInterceptor(loggingInterceptor)
    }
}