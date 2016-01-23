In order to run Gepard directly from the compiled .class files, you need to include 
the main Gepard folder in the classpath so the program finds the resources/ subfolder.


Windows:
java -cp bin\;. org.gepard.client.Start

Linux/Unix/Mac:
java -cp bin/:. org.gepard.client.Start
