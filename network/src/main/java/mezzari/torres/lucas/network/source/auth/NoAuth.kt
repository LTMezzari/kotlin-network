package mezzari.torres.lucas.network.source.auth

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author Lucas T. Mezzari
 * @since 25/07/2019
 *
 * This Class is a generic class for Networks that don't need authentication
 */
class NoAuth : BaseAuth() {
    override fun onAuthenticationFailed() {
        //Nothing will be done here
    }

    override fun <T> doAuthentication(call: Call<T>, callback: Callback<T>) {
        //Nothing will be done here
    }

    override fun <T> isAuthenticated(response: Response<T>): Boolean {
        return true //It will always be authenticated
    }
}