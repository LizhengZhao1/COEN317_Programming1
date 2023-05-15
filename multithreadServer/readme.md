Name & Student ID:
Lizheng Zhao W1608646

Assignment Name:
COEN 317 Programming Assignment 1

Date 
Apr 23, 2023

High-level description:
    This assignment is to build a functional web server that return specific files according to the requests from clients
    File type includes HTML, TXT, JPG/PNG, GIF
    In order to run this program, we need to execute some command lines on terminal(more details are in the following Instructions)
    When the server gets a request for index.html (which is the default web page if no file is specified), it will prepend the document root to the specified file and determine if
the file exists, and if the proper permissions are set on the file (typically the file has to be world readable). If the file does not exist, a file not found error (HTTP error
code 404) is returned. If a file is present but the proper permissions are not set, a permission denied error is returned. Otherwise, an HTTP OK message is returned along with the contents of a file

List of submitted files:
HandleRequest.java
Server.java
index.html
test1.txt 
test2.gif 
test3.jpg
test4.txt(deleted)
readme.md
ScriptFile.pdf
MakeFile.txt

Instructions for running program:
Java Runtime Environment is required.
1. Compile this program first
javac Server.java
2 Run this program with command line options:
java Server -document_root <$DOCUMENT_ROOT> -port <$PORT>
eg: java Server -document_root "/Users/mac/Idea-workspace/multithreadServer" -port 8080