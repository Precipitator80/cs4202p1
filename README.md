# CS4202-Practical-1 - Cache Simulator

The task was to develop a memory cache simulator that simulates various memory caching
hierarchies and strategies, ultimately reporting back statistics about the runtime behaviour of
those caches. One run of the simulator accepts a JSON configuration file that describes
a cache hierarchy, reads in a pre-generated dynamic memory trace file and uses those entries to
access the simulated caches.

This project does not include a report.

To compile, run the following in the src folder:
```bash
make all
```

If make is not supported, run the compilation command manually:
```
javac *.java -cp ../lib/fastjson2-2.0.45.android4.jar
```

After this, the simulator can be run with the following command:
```
java -cp ".:../lib/fastjson2-2.0.45.android4.jar" CacheSimulator <cacheConfig.json> <programTrace.out>
```

Or, alternatively, on Windows:
```
java -cp ".;../lib/fastjson2-2.0.45.android4.jar" CacheSimulator <cacheConfig.json> <programTrace.out>
```

cacheConfig.json: Path to a JSON configuration file.  
programTrace.out: Path to trace file.  
