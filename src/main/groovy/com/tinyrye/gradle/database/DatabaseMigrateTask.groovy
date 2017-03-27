package com.tinyrye.gradle.database

import groovy.sql.Sql

import java.sql.Connection
import javax.sql.DataSource

import org.apache.commons.dbcp2.BasicDataSource

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class DatabaseMigrateTask extends DefaultTask
{
    String url
    String username
    String password
    String driverClassName
    File migrationsBaseDir

    @TaskAction
    public void migrate()
    {
        new Sql(getDataSource()).withTransaction { dbAccessor ->
            println "---- Running Migrations ---------------------------------------------------------------"
            dbAccessor.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS database_migrations_versioning (
                    version VARCHAR(128) PRIMARY KEY,
                    file_name VARCHAR(128),
                    migrated BOOLEAN,
                    ran_on TIMESTAMP
                )""".toString())
            getMigrationsBaseDir().eachFile { file ->
                migrate(dbAccessor, file)
            }
        }
    }

    public void migrate(Connection dbConnection, File migrationFile)
    {
        Sql dbAccessor = new Sql(dbConnection)
        Migration migration = Migration.fromFile(migrationFile)
        println "->   ${migration}"
        def checkRecordedSql = 'SELECT migrated, ran_on FROM database_migrations_versioning WHERE version = :version'
        def insertVersionSql = 'INSERT into database_migrations_versioning (version, file_name, migrated) VALUES (:version, :fileName, false)'
        def markMigrationRanSql = 'UPDATE database_migrations_versioning SET migrated = true, ran_on = CURRENT_TIMESTAMP WHERE version = :version'
        def versionExists = false
        def migrated = false
        dbAccessor.eachRow(checkRecordedSql, [version: migration.version]) { row ->
            versionExists = true
            migrated = row.migrated
        }
        if (! migrated) {
            println "    -> has not been run: running now"
            if (! versionExists) dbAccessor.executeInsert(insertVersionSql, [version: migration.version, file_name: migration.fileName])
            migrationFile.withInputStream { dbAccessor.execute(it.text) }
            dbAccessor.executeUpdate(markMigrationRanSql, [version: migration.version])
        }
    }

    public File getMigrationsBaseDir() {
        return migrationsBaseDir ?: project.migrations.baseDir
    }

    public DataSource getDataSource() {
        def datasource = new BasicDataSource()
        datasource.url = url ?: project.migrations.url
        datasource.driverClassName = driverClassName ?: project.migrations.driverClassName
        datasource.username = username ?: project.migrations.username
        datasource.password = password ?: project.migrations.password
        return datasource
    }
}