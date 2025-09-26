package com.example.weeklyplanner;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Task.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class TaskDatabase extends RoomDatabase {

    private static volatile TaskDatabase instance;

    public abstract TaskDao taskDao();

    // Миграция с версии 1 на версию 2 для добавления поля weekStartDate
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN weekStartDate INTEGER");
        }
    };

    public static TaskDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (TaskDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    TaskDatabase.class,
                                    "task_database"
                            )
                            .allowMainThreadQueries() // ВАЖНО: разрешаем запросы в главном потоке
                            .addMigrations(MIGRATION_1_2) // Добавляем миграцию
                            .fallbackToDestructiveMigration()
                            .build();

                    // Принудительно инициализируем базу данных
                    try {
                        instance.getOpenHelper().getWritableDatabase();
                        TaskDao dao = instance.taskDao();
                        if (dao == null) {
                            throw new RuntimeException("DAO is null");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to initialize database", e);
                    }
                }
            }
        }
        return instance;
    }
}