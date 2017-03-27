package com.tinyrye.gradle.database;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DatabaseMigrationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.extensions.create("migrations", MigrationsConfig)
        project.tasks.create("dropDatabase", DatabaseDropTask)
        project.tasks.create("initializeDatabase", DatabaseInitializeTask)
        project.tasks.create("migrateDatabase", DatabaseMigrateTask)
    }
}