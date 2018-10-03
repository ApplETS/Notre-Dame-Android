package ca.etsmtl.applets.repository.data.api.response.mapper

import com.squareup.moshi.JsonAdapter
import se.ansman.kotshi.KotshiJsonAdapterFactory

/**
 * Created by Sonphil on 13-03-18.
 */
@KotshiJsonAdapterFactory
internal abstract class ApplicationJsonAdapterFactory : JsonAdapter.Factory {
    companion object {
        val INSTANCE: ApplicationJsonAdapterFactory = SignetsApplicationJsonAdapterFactory()
    }
}