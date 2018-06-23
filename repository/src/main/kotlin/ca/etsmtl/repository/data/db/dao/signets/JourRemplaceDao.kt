package ca.etsmtl.repository.data.db.dao.signets

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import ca.etsmtl.repository.data.model.signets.JourRemplace

/**
 * Created by Sonphil on 24-05-18.
 */
@Dao
abstract class JourRemplaceDao : SignetsDao<JourRemplace> {
    @Query("SELECT * FROM jourremplace")
    abstract fun getAll(): LiveData<List<JourRemplace>>
}