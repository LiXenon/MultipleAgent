/*******************************************************\
|                AI and MAS: Searchclient               |
|                        README                         |
\*******************************************************/

This readme describes how to use the included Python searchclient with the server that is contained in server.jar. 

The Python search client requires at least Python version 3.7, and has been tested with CPython.
The search client requires the 'psutil' package to monitor its memory usage; the package can be installed with pip:
    $ pip install psutil

All the following commands assume the working directory is the one this readme is located in.

You can read about the server options using the -h argument:
    $ java -jar ../server.jar -h

Starting the server using the searchclient:
    $ java -jar ../server.jar -l ../levels/SAD1.lvl -c "python searchclient/searchclient.py" -g -s 150 -t 180

The searchclient uses the BFS search strategy by default. Use arguments -dfs, -astar, -wastar, or -greedy to set alternative search strategies (after you implement them). For instance, to use DFS search on the same level as above:
    $ java -jar ../server.jar -l ../levels/SAD1.lvl -c "python searchclient/searchclient.py -dfs" -g -s 150 -t 180

Memory settings:
    * Unless your hardware is unable to support this, you should let the searchclient allocate at least 2GB of memory *
    The searchclient monitors its own process' memory usage and terminates the search if it exceeds a given memory threshold.
    To set the max memory usage to 2GB (which is also the default):
        $ java -jar ../server.jar -l ../levels/SAD1.lvl -c "python searchclient/searchclient.py --max-memory 2048" -g -s 150 -t 180
    Avoid setting max memory usage too high, since it will lead to your OS doing memory swapping which is terribly slow.