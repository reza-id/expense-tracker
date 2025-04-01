package dev.cianjur.expense.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.cianjur.expense.data.local.dao.CategoryDao
import dev.cianjur.expense.data.local.dao.ExpenseDao
import dev.cianjur.expense.data.local.dao.ExpenseImageDao
import dev.cianjur.expense.data.local.entity.CategoryEntity
import dev.cianjur.expense.data.local.entity.ExpenseEntity
import dev.cianjur.expense.data.local.entity.ExpenseImageEntity

@Database(
    entities = [
        ExpenseEntity::class,
        CategoryEntity::class,
        ExpenseImageEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseImageDao(): ExpenseImageDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getInstance(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
