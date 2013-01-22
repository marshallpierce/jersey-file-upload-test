When mimepull cannot write temp files during file upload handling, the error is incorrectly given status 400 and therefore not logged as a server-side error.

Run the ServerMain class with `-Djava.io.tmpdir=/something-that-does-not-exist`.

To test with a file that is small enough that it works:

```
curl -v -F file=@sample-file http://127.0.0.1:8080/resource-test
```

It will helpfully spit out the MD5 of the file as a simple check that it uploaded OK.

On my machine, the 10k file 'zero-file' (guess what's in it) is big enough to cause it to fail.

```
curl -v -F file=@zero-file http://127.0.0.1:8080/resource-test
```

This 400's.
