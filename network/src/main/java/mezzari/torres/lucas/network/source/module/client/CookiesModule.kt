package mezzari.torres.lucas.network.source.module.client

import mezzari.torres.lucas.network.source.Network
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * @author Lucas T. Mezzari
 * @since 25/07/2019
 *
 * Module that handles cookies in requests
 */
class CookiesModule : Network.OkHttpClientLevelModule {
    //Initialize a cookie variable
    private var cookies : HashSet<String> = HashSet()

    //Initialize the interceptor that adds cookies to the header
    private val addCookiesInterceptor = Interceptor {
        //get the request builder from the chain
        val builder = it.request().newBuilder()

        //Loops through the cookies
        for (cookie in cookies) {
            //Set the cookies in the header
            builder.addHeader("Cookie", cookie)
        }

        //Proceeds with the request and returns it's response
        return@Interceptor it.proceed(builder.build())
    }

    //Initialize the interceptor that will save the cookies received
    private val receivedCookiesInterceptor = Interceptor {
        //Proceeds the request and get it's response
        val originalResponse : Response = it.proceed(it.request())
        //Get the cookies
        val setCookies = originalResponse.headers("Set-Cookie")
        //Check if they're not empty
        if (setCookies.isNotEmpty()) {
            //Set the new cookies
            cookies = HashSet(setCookies)
        }

        //Returns the response
        return@Interceptor originalResponse
    }

    override fun onClientBuilderCreated(okHttpClientBuilder: OkHttpClient.Builder) {
        //Add the interceptors
        okHttpClientBuilder.addInterceptor(addCookiesInterceptor)
        okHttpClientBuilder.addInterceptor(receivedCookiesInterceptor)
    }
}