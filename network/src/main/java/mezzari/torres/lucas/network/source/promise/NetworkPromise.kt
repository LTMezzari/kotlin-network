package mezzari.torres.lucas.network.source.promise

import retrofit2.Call
import retrofit2.Response

/**
 * @author Lucas T. Mezzari
 * @since 25/07/2019
 **/
open class NetworkPromise<T>(delegate: BaseNetworkPromise<T>.() -> Unit): BaseNetworkPromise<T>(delegate) {
    override fun onFailure(call: Call<T>, t: Throwable) {
        super.onFailure(call, t)
        this.failureCallback?.invoke(this, if (call.isCanceled) null else t.message)
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        super.onResponse(call, response)
        if (response.isSuccessful) {
            this.successCallback?.invoke(this, response.body())
        } else if (!this.auth.isAuthenticated(response)) {
            this.auth.onUnauthorized(call, this)
        } else {
            this.failureCallback?.invoke(this, response.message())
        }
    }
}