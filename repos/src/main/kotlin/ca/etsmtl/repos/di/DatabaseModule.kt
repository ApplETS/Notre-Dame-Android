package ca.etsmtl.repos.di

import android.app.Application
import android.arch.persistence.room.Room
import ca.etsmtl.repos.data.db.AppDatabase
import ca.etsmtl.repos.data.db.dao.EtudiantDao
import ca.etsmtl.repos.data.db.dao.ProgrammeDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Sonphil on 28-02-18.
 */
@Module
open class DatabaseModule {

    companion object {
        val instance = DatabaseModule()
    }

    @Singleton @Provides
    open fun provideDb(app: Application): AppDatabase =
            Room.databaseBuilder(app, AppDatabase::class.java, "etsmobilerepos.db")
                    .fallbackToDestructiveMigration()
                    .build()

    @Singleton @Provides
    fun provideInfoEtudiantDao(db: AppDatabase): EtudiantDao = db.etudiantDao()

    @Singleton @Provides
    fun provideProgrammeDao(db: AppDatabase): ProgrammeDao = db.programmeDao()
}