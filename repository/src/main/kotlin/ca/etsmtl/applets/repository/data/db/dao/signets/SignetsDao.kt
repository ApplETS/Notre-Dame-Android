package ca.etsmtl.applets.repository.data.db.dao.signets

import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy

/**
 * Created by Sonphil on 17-05-18.
 */

interface SignetsDao<T> {
    /**
     * Insert a data into the DB
     *
     * @param obj The data to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(obj: T)

    /**
     * Insert multiple data into the DB
     *
     * @param obj The data to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(objs: List<T>)
}