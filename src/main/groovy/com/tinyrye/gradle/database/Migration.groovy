package com.tinyrye.gradle.database

import groovy.transform.ToString

@ToString
public class Migration
{
    def static final FILE_NAME_PATTERN = /(\w+)_\w+.\w+/

    public static Migration fromFile(File file)
    {
        String fileName = file.name
        def fileNameMatch = (fileName =~ FILE_NAME_PATTERN)
        if (fileNameMatch.size() == 1) {
            def version = fileNameMatch[0][1]
            return new Migration(version: version, fileName: fileName)
        }
        else {
            throw new IllegalArgumentException('File name pattern is invalid')
        }
    }

    def String version
    def String fileName
}