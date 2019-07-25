package mezzari.torres.lucas.network.source.module.retrofit

import mezzari.torres.lucas.network.source.Network
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author Lucas T. Mezzari
 * @since 25/07/2019
 *
 * This class adds the GsonConverterModule to retrofit
 */
class GsonConverterModule : Network.RetrofitLevelModule {

    private val factory: GsonConverterFactory = GsonConverterFactory.create()

    override fun onRetrofitBuilderCreated(retrofitBuilder: Retrofit.Builder) {
        //Set the GsonConverterFactory
        retrofitBuilder.addConverterFactory(factory)
    }
}