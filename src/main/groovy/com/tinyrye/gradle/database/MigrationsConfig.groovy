package com.tinyrye.gradle.database;

import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString
public class MigrationsConfig
{
    def String driverClassName

    def String superUrl
    private String superUsername
    private String superPassword

    def String database
    def String url
    def String username
    def String password

    def baseDir
    
    public String getSuperUsername() {
        if (! superUsername) return username
        else return superUsername
    }

    public MigrationsConfig setSuperUsername(String superUsername) {
        if (superUsername) this.superUsername = superUsername
        return this
    }

    public String getSuperPassword() {
        if (! superPassword) return password
    }

    public MigrationsConfig setSuperPassword(String superPassword) {
        if (superPassword) this.superPassword = superPassword
        return this
    }

    public MigrationsConfig getSuperConfig() {
        return new MigrationsConfig(
                    url: superUrl, username: getSuperUsername(),
                    password: getSuperPassword())
    }
}