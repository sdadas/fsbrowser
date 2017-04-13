# FSBrowser

FSBrowser is a simple desktop interface for Hadoop Distributed File System. 
The application can be used as a more efficient alternative to Hue's file browser. 
It uses native hadoop libraries instead of HttpFS and should be faster for reading and writing directories with large number of files.

![app](images/app.png)

### Features
- Sending files / directories between local file system and HDFS
- Reading directories with millions of files
- Multiple active connections (tabs)
- GUI interface for hadoop commandline tools

### Installation

Maven and JDK8 are the only compile-time prerequisites. Build application with `mvn package`. 

Run `target/fsbrowser.jar`.

Note that on Windows some actions require additional native hadoop libraries (i.e. copying files between two clusters).
While FSBrowser will run without them, it's recommended to have them installed.
You can download precompiled binary package [here](https://github.com/sardetushar/hadooponwindows/archive/master.zip).
After extracting, set `HADOOP_HOME` environment variable to the extracted directory and add `bin` subdirectory to your `PATH` variable.

### Acknowledgments
- This project uses [Fugue Icons](http://p.yusukekamiyamane.com/) by [Yusuke Kamiyamane](http://p.yusukekamiyamane.com/about/)
