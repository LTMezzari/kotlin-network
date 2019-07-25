package mezzari.torres.lucas.network.source.auth

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author Lucas T. Mezzari
 * @since 25/07/2019
 *
 * This class will handle authentication of the network
 */
abstract class BaseAuth {

    //Declares a variable that will control how many times the network wil try to authenticate
    protected var count : Int = 0

    /**
     * This method will deal with the authentication
     * It is called on the onUnauthorized method
     */
    abstract fun <T>doAuthentication (call : Call<T>, callback : Callback<T>)

    /**
     * This method handles the failed authentication callback
     * It is called on the onUnauthorized method when the count reaches the max
     */
    abstract fun onAuthenticationFailed ()

    /**
     * This method is called when the isAuthenticated returns false
     * It will handle the calls doAuthentication and onAuthenticationFailed
     *
     * This method is open to be override when needed
     */
    open fun <T>onUnauthorized (call : Call<T>, callback : Callback<T>) {
        //Check if the count reached the max of tree
        if (count >= 3) {
            //Clears the counter
            count = 0
            //Call onAuthenticationFailed
            onAuthenticationFailed()
            //ends the execution
            return
        }

        //Do the authentication
        doAuthentication(call, callback)
    }

    /**
     * This method test if the user is authenticated, or not
     * It is open to be override when needed
     *
     * @return true when the user is authenticated
     */
    open fun <T>isAuthenticated (response : Response<T>) : Boolean {
        //Check if the response code is 401
        val result = response.code() != 401

        //If the user is authenticated, reset the count
        if (result)
            count = 0
        //Return the result
        return result
    }
}