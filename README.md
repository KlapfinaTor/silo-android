# SiLo-Android



## Overview
A simple Android remote logger. Its build ontop of the standard Log class. The library saves the logentries into a database. The developer has the option to push the saved logs to an external server.



## Download
Download the latest version from Github.


## Initialize
Inside the onCreate Method in your Application

```
Silo.initialize(context);
```


## Usage
```
Silo.i(TAG,"A Info Log");
```


## Send Logs to a Remote Server

First, set a URL before calling the `Silo.push()` method.

```
Silo.setUrl("API URL");
```
After that you can push the saved logs with calling:

```
Silo.push():
```

You can also directly send a message to the specified endpoint without calling push(). This will only send the one logmessage that you specified. The saved logs in the database wont be pushed.

```
Silo.send("Direct Message");
```
---
## Additional Methods

Sets the size in which the logs will be pushed to the remote server.
```
Silo.setBatchLogSize(500);
```

Set to true if you also want the logoutput in the Logcat console.
```
Silo.setLogCatOutputEnabled(true);
```

Sets the loglevel.
```
Silo.setLogLevel()
```

Gets the log amount that are stored in the database.

```
Silo.getPendingLogAmount()
```


## License

```
MIT License

Copyright (c) 2019 KlapfinaTor

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
