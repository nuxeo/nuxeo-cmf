CMF Funkload tests
=====================

Funkload
--------
For more information on funkload: http://funkload.nuxeo.org/
For installation:
http://funkload.nuxeo.org/INSTALL.html#quick-installation-guide-for-debian-lenny-ubuntu-8-10-9-04
to install funkload you need to:
sudo aptitude install python-dev python-xml python-setuptools \ 
python-webunit python-docutils gnuplot 
sudo aptitude install tcpwatch-httpproxy --without-recommends 
sudo easy_install -f http://funkload.nuxeo.org/snapshots/ -U funkload

Import tools depedencies
------------------------
To launch the import tools, install pypdf (http://pybrary.net/pyPdf/),
reportlab (http://www.reportlab.com/software/opensource/rl-toolkit/)
and jinja (http://jinja.pocoo.org/2/ ).
On a debian based system:
sudo aptitude install python-jinja2 python-pypdf python-reportlab

Monitoring
----------
./monitoring.sh

monitoring needs to be started before the bench.

for hardware log, launch the following command:
sar -d -o $SERVER_HOME/log/sysstat-sar.log 5 720 >/dev/null 2>&1 &
This will monitor the activity every 5s during 1h.

If pg_fouine is installed, and postgresql.conf configured 
accordingly with http://pgfouine.projects.postgresql.org/tutorial.html,
you should have postgres log in /var/log/pgsql

To get the  vacuumdb log, run 
vacuumdb -p 5434  -fzv cmf &> vacuum.log
This command will catch the vacuum output for the cmf database
on the postgres version running on port 5434. You need to be authentified
as postgres user to connect to the database.

To get the garbage collecting log, uncomment the following line
in nuxeo.conf
JAVA_OPTS=$JAVA_OPTS -Xloggc:gc.log  -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps

To create the report, put all log files in the same directory and run
python logchart.py [options] LOG_PATH REPORT_PATH


Test
----
make testCMF: run the CMF test
make testBench: run the CMF test with multiple concurrent users.


