package com.tinyrye.gradle.database

import javax.sql.DataSource

import groovy.sql.Sql

import org.apache.commons.dbcp2.BasicDataSource

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class DatabaseDropTask extends DefaultTask
{
	String database
    String superUrl
    String superUsername
    String superPassword
    String driverClassName

    @TaskAction
    public void drop() {
        println "Dropping Database, ${getDatabase()}"
        new Sql(getDataSource()).execute("DROP DATABASE IF EXISTS ${getDatabase()}".toString())
    }

    public String getDatabase() {
        return database ?: project.migrations.database
    }
    public DataSource getDataSource() {
        def datasource = new BasicDataSource()
        datasource.url = superUrl ?: project.migrations.superConfig.url
        datasource.username = superUsername ?: project.migrations.superConfig.username
        datasource.password = superPassword ?: project.migrations.superConfig.password
        datasource.driverClassName = driverClassName ?: project.migrations.driverClassName
        return datasource
    }
}