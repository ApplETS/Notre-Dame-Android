package ca.etsmtl.repository.data.db.dao.signets

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import ca.etsmtl.repository.data.db.entity.signets.CoursEntity

/**
 * Created by Sonphil on 24-05-18.
 */
@Dao
abstract class CoursDao : SignetsDao<CoursEntity> {
    @Query("SELECT * FROM coursentity")
    abstract fun getAll(): LiveData<List<CoursEntity>>

    @Query("SELECT * FROM coursentity WHERE session LIKE :sessionAbrege")
    abstract fun getCoursBySession(sessionAbrege: String): LiveData<List<CoursEntity>>
}