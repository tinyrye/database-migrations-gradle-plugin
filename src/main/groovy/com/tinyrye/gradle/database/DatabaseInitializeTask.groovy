package com.tinyrye.gradle.database

import javax.sql.DataSource

import groovy.sql.Sql

import org.apache.commons.dbcp2.BasicDataSource

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class DatabaseInitializeTask extends DefaultTask
{
	String database
    String url
    String username
    String password
    String driverClassName
    String superUrl
    String superUsername
    String superPassword

    @TaskAction
    public void initialize() {
    	def superDbAccessor = new Sql(getSuperDataSource())
    	def dbExists = superDbAccessor.rows("SELECT COUNT(*) as count FROM pg_database WHERE datname = :database", [database: getDatabase()]).get(0).with {
    		it.count > 0
    	}
    	if (! dbExists) {
    		println "Creating Database: ${getDatabase()}"
	    	superDbAccessor.execute("CREATE DATABASE ${getDatabase()} WITH owner = ${getUsername()}".toString())
	    	def dbAccessor = new Sql(getDataSource())
		    dbAccessor.execute("""
	            CREATE TABLE database_migrations_versioning (
	                version VARCHAR(128) PRIMARY KEY,
	                file_name VARCHAR(128),
	                migrated BOOLEAN,
	                ran_on TIMESTAMP
	            )""".toString())
    	}
    }

    public String getDatabase() {
    	return database ?: project.migrations.database
    }

    public String getUsername() {
    	return username ?: project.migrations.username
    }

    public DataSource getDataSource() {
        def datasource = new BasicDataSource()
        datasource.url = url ?: project.migrations.url
        datasource.username = project.migrations.username
        datasource.password = project.migrations.password
        datasource.driverClassName = project.migrations.driverClassName
        return datasource
    }

    public DataSource getSuperDataSource() {
        def datasource = new BasicDataSource()
        datasource.url = superUrl ?: project.migrations.superConfig.url
        datasource.driverClassName = driverClassName ?: project.migrations.driverClassName
        datasource.username = superUsername ?: project.migrations.superConfig.username
        datasource.password = superPassword ?: project.migrations.superConfig.password
        return datasource
    }
}