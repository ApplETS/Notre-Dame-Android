package ca.etsmtl.etsmobile.data.repository.login

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ca.etsmtl.etsmobile.AppExecutors
import ca.etsmtl.etsmobile.data.db.AppDatabase
import ca.etsmtl.etsmobile.data.model.UserCredentials
import java.security.KeyStore
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/**
 * Created by Sonphil on 21-04-18.
 */

class LoginRepository @Inject constructor(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val appExecutors: AppExecutors,
    private val db: AppDatabase
) {
    companion object {
        private const val TAG = "LoginRepository"
        private const val UNIVERSAL_CODE_PREF = "UniversalCodePref"
        private const val ENCRYPTED_PASSWORD_PREF = "EncryptedPasswordPref"

        @JvmStatic
        var userCredentials: AtomicReference<UserCredentials> = AtomicReference()
    }

    /**
     * Saves the user's credentials
     *
     * Saves the [UserCredentials.codeAccesUniversel] to the [SharedPreferences] and the
     * [UserCredentials.motPasse] to the [KeyStore]
     *
     * @param userCredentials The user credentials
     * @return A [LiveData] whose value would be true if the save is complete
     */
    fun saveUserCredentials(userCredentials: UserCredentials): LiveData<Boolean> {
        val finishedBlnLD = MutableLiveData<Boolean>()

        finishedBlnLD.value = false

        appExecutors.diskIO().execute {
            saveUniversalCode(userCredentials.codeAccesUniversel)
            savePassword(userCredentials.motPasse, userCredentials.codeAccesUniversel)

            finishedBlnLD.postValue(true)
        }

        return finishedBlnLD
    }

    /**
     * Returns the [UserCredentials.codeAccesUniversel] from the [SharedPreferences] and the
     * [UserCredentials.motPasse] from the [KeyStore]
     *
     * @return The saved [UserCredentials]
     */
    fun getSavedUserCredentials(): UserCredentials? {
        val codeAccesUniversel = getSavedUniversalCode()

        var userCredentials: UserCredentials? = null

        if (codeAccesUniversel != null) {
            val motPasse = getSavedPassword(codeAccesUniversel)

            if (motPasse != null) {
                userCredentials = UserCredentials(codeAccesUniversel, motPasse)
            }
        }

        return userCredentials
    }

    /**
     * Saves the universal code to the [SharedPreferences]
     *
     * @param universalCode The user's universal code
     */
    private fun saveUniversalCode(universalCode: String) {
        with(prefs.edit()) {
            putString(UNIVERSAL_CODE_PREF, universalCode)
            apply()
        }
    }

    /**
     * Returns the universal code from the [SharedPreferences]
     *
     * @return The user's universal code
     */
    private fun getSavedUniversalCode(): String? {
        return prefs.getString(UNIVERSAL_CODE_PREF, null)
    }

    /**
     * Gets the password from the Keystore, decrypts it and returns it
     *
     * @param alias Key for accessing the key stored in the Keystore
     * @return The user's password
     */
    private fun getSavedPassword(alias: String): String? {
        Log.d(TAG, "Getting password")
        val keyStoreUtils = KeyStoreUtils(context)

        val keyPair = keyStoreUtils.getAndroidKeyStoreAsymmetricKeyPair(alias)

        val encryptedPW = prefs.getString(ENCRYPTED_PASSWORD_PREF, null)

        val privateKey = keyPair?.private

        return if (privateKey != null) {
            val password = CipherUtils().decrypt(encryptedPW, privateKey)

            Log.d(TAG, "Password successfully decrypted")

            return password
        } else {
            null
        }
    }

    /**
     * Encrypts the password and put it in the [KeyStore]
     *
     * @param password The password to be saved
     * @param alias Key that would be used to for accessing the key stored in the Keystore
     */
    private fun savePassword(password: String, alias: String) {
        val keyStoreUtils = KeyStoreUtils(context)

        keyStoreUtils.createAndroidKeyStoreAsymmetricKey(alias)

        val keyPair = keyStoreUtils.getAndroidKeyStoreAsymmetricKeyPair(alias)

        val cipherUtils = CipherUtils()

        keyPair?.let {
            val encryptedPW = cipherUtils.encrypt(password, it.public)

            with(prefs.edit()) {
                putString(ENCRYPTED_PASSWORD_PREF, encryptedPW)
                apply()
            }
        }
    }

    /**
     * Deletes the password saved in the [KeyStore]
     *
     * @param alias Key for accessing the key stored in the Keystore
     */
    private fun deletePassword(alias: String) {
        KeyStoreUtils(context).deleteAndroidKeyStoreKeyEntry(alias)
    }

    /**
     * Clears all the tables that are registered to the database
     *
     * @return A [LiveData] whose the value would be true if the process has finished
     */
    private fun clearDb(): LiveData<Boolean> {
        val clearFinished = MutableLiveData<Boolean>()

        clearFinished.value = false

        appExecutors.diskIO().execute {
            db.clearAllTables()
            clearFinished.postValue(true)
        }

        return clearFinished
    }

    /**
     * Clears the user's data
     *
     * Clears the [SharedPreferences], deletes the password saved in the [KeyStore] and clears the
     * database
     *
     * @return A [LiveData] whose the value would be true if the process has finished
     */
    fun clearUserData(): LiveData<Boolean> {
        prefs.edit().clear().apply()

        deletePassword(LoginRepository.userCredentials.get().codeAccesUniversel)

        LoginRepository.userCredentials.set(null)

        return clearDb()
    }
}