package com.tinyrye.gradle.database;

import groovy.sql.Sql

import javax.sql.DataSource

import org.apache.commons.dbcp2.BasicDataSource

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DatabaseMigrationPlugin implements Plugin<Project>
{
    @Override
    public void apply(Project project)
    {
        project.extensions.create("migrations", MigrationsConfig)

        /**
         * Create migration versioning repository.
         */        
        project.task('database-migrations-init') << {
            new Sql(getDataSourceHandle(project.migrations.getSuperConfig())).execute(
                "CREATE DATABASE ${project.migrations.database} WITH owner = ${project.migrations.username}".toString())
            new Sql(getDataSourceHandle(project.migrations)).execute("""
                CREATE TABLE database_migrations_versioning (
                    version VARCHAR(128) PRIMARY KEY,
                    file_name VARCHAR(128),
                    migrated BOOLEAN,
                    ran_on TIMESTAMP
                )""")
        }

        /**
         * Drop database and recreate database
         */
        project.task('database-migrations-reset') << {

        }

        project.task('database-migrations-migrate') << {
            def dbAccessor = new Sql(getDataSourceHandle(project.migrations))
            println "---- Running Migrations ---------------------------------------------------------------"
            new File(project.migrations.baseDir).eachFile { file ->
                migrate(dbAccessor, file)
            }
        }
    }
    
    public void migrate(dbAccessor, file)
    {
        Migration migration = Migration.fromFile(file)
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
            file.withInputStream { dbAccessor.execute(it.text) }
            dbAccessor.executeUpdate(markMigrationRanSql, [version: migration.version])
        }
    }
    
    public DataSource getDataSourceHandle(config) {
        println "Data Source Config: ${config}"
        def datasource = new BasicDataSource()
        datasource.url = config.url
        datasource.driverClassName = config.driverClassName
        datasource.username = config.username
        datasource.password = config.password
        return datasource
    }
}