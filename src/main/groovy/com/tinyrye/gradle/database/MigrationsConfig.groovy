package com.tinyrye.gradle.database;

import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString
public class MigrationsConfig
{
    def String driverClassName
    def String database
    def String url
    def String username
    def String password
    def File baseDir
    def MigrationsConfig superConfig

    def superConfig(Closure handler) {
    	superConfig = new MigrationsConfig()
    	superConfig.with(handler)
    }
}