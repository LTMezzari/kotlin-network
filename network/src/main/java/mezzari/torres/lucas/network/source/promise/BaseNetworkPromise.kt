package mezzari.torres.lucas.network.source.promise

import androidx.annotation.CallSuper
import mezzari.torres.lucas.network.source.Network
import mezzari.torres.lucas.network.source.auth.BaseAuth
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author Lucas T. Mezzari
 * @since 25/07/2019
 **/
abstract class BaseNetworkPromise<T>(
    private val delegate: BaseNetworkPromise<T>.() -> Unit
): Callback<T> {

    var successCallback: (BaseNetworkPromise<T>.(T?) -> Unit)? = null
        private set
    var failureCallback: (BaseNetworkPromise<T>.(String?) -> Unit)? = null
        private set

    var call: Call<T>? = null
    var throwable: Throwable? = null
    var response: Response<T>? = null
    val jsonError: JSONObject? get() {
        response?.errorBody()?.string()?.run {
            return try {
                JSONObject(this)
            } catch (e: JSONException) {
                null
            }
        }
        return null
    }

    protected val auth: BaseAuth get() = Network.auth

    fun then(callback: BaseNetworkPromise<T>.(T?) -> Unit): BaseNetworkPromise<T> {
        this.successCallback = callback
        this.delegate.takeIf { failureCallback != null }?.invoke(this)
        return this
    }

    fun catch(callback: BaseNetworkPromise<T>.(String?) -> Unit): BaseNetworkPromise<T> {
        this.failureCallback = callback
        this.delegate.takeIf { successCallback != null }?.invoke(this)
        return this
    }

    @CallSuper
    override fun onFailure(call: Call<T>, t: Throwable) {
        this.call = call
        this.throwable = t
    }

    @CallSuper
    override fun onResponse(call: Call<T>, response: Response<T>) {
        this.call = call
        this.response = response
    }
}