#	CS258 Department Store Coursework.

## Running this code locally is awkward and requires many things, heres a list of how to do it.

Powershell Xterm fix: https://github.com/PowerShell/Win32-OpenSSH/wiki/TTY-PTY-support-in-Windows-OpenSSH

 - SSH into daisy using powershell using the command
 ```console
 ssh -L 7100:daisy:1521 u1814232@daisy.csv.warwick.ac.uk
 ```
 This connects to daisy so you can run commands.
 
 - Next you can edit the code on your local computer
 
 - Now you need to transfer the files to daisy, this can be done through git but this is tedious so we use scp, the command is:
 ```console
 scp ./Assignment.java u1814232@daisy.csv.warwick.ac.uk:~/CW/
 ``` 
 This moves the local Assignment.java file to the CW folder on daisy. To run this command be in your local code directory.
 
 - Now go into your SSH daisy connection and run the command
 ```console
 javac Assignment.java
 ```
 to compile the code.
 
 - Now you need to run the code using the special command to inject the Oracle Driver into the process. this is:
 ```console
 java -cp .:/app/oracle/product/11.2.0.4.0/db_1/jdbc/lib/ojdbc7.jar Assignment
 ```
 
 - Important to close the connections when you have finished....

