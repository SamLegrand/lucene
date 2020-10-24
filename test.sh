#!/bin/bash
export CLASSPATH=./lucene-8.6.3/core/lucene-core-8.6.3.jar:./lucene-8.6.3/demo/lucene-demo-8.6.3.jar:./lucene-8.6.3/analysis/common/lucene-analyzers-common-8.6.3.jar:./lucene-8.6.3/queryparser/lucene-queryparser-8.6.3.jar
java org.apache.lucene.demo.IndexFiles -docs ./lucene-8.6.3/
java org.apache.lucene.demo.SearchFiles
