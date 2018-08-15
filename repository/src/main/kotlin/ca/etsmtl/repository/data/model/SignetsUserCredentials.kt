package ca.etsmtl.repository.data.model

import android.os.Parcel
import android.os.Parcelable
import ca.etsmtl.repository.data.model.SignetsUserCredentials.CREATOR.INSTANCE
import java.util.concurrent.atomic.AtomicReference

/**
 * User's credentials for accessing Signets' webservice
 * An instance of this class is stored in the [INSTANCE] AtomicReference.
 *
 * Created by Sonphil on 28-02-18.
 */
data class SignetsUserCredentials(
    val codeAccesUniversel: String,
    val motPasse: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(codeAccesUniversel)
        parcel.writeString(motPasse)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SignetsUserCredentials> {
        override fun createFromParcel(parcel: Parcel): SignetsUserCredentials {
            return SignetsUserCredentials(parcel)
        }

        override fun newArray(size: Int): Array<SignetsUserCredentials?> {
            return arrayOfNulls(size)
        }

        @JvmStatic
        var INSTANCE: AtomicReference<SignetsUserCredentials> = AtomicReference()
    }
}