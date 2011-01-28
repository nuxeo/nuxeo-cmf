#!/usr/bin/python
# (C) Copyright 2009 Nuxeo SAS <http://nuxeo.com>
# Authors: Benoit Delbosc <ben@nuxeo.com>
#          Roman Mackovcak (recykl) for the GnuPlot performance charts idea
#          http://blog.zmok.net/articles/2007/02/06/how-to-show-performance-statistics-in-a-chart
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 2 as published
# by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
# 02111-1307, USA.
"""Generate a report from miscellaneous monitor logs.
"""
import os
import re
from math import log10, ceil
from tempfile import mkstemp
from commands import getstatusoutput
from datetime import datetime
from docutils.core import publish_cmdline
from optparse import OptionParser, TitledHelpFormatter

VERSION="1.0"
USAGE="""%prog [options] LOG_PATH REPORT_PATH

Generates a monitoring report given a set of log files.

Expected log file name to present in LOG_PATH:

* gc.log          The garbage collector log produced by the verbose gc jvm option

* jvm.log         The jboss jvm monitoring
* webthreads.log  The jboss http thread pool monitoring
* webrequests.log The jboss http request processor monitoring
* nuxeo-ds.log    The jboss NuxeoDS datasource monitoring
* vcs-ds.log      The jboss NXRepository/default datasource monitoring

* pgsql.log       The syslog output of PostgreSQL
* vacuum.log      The vacuum -fzv output on a database

* sysstat-sar.log The sysstat activity including disk in binary or ascii format


All the log file can be gzipped.
"""



def command(cmd, do_raise=True, silent=False):
    """Return the status, output as a line list."""
    extra = 'LC_ALL=C '
    print('Run: ' + extra + cmd)
    status, output = getstatusoutput(extra +cmd)
    if status:
        if not silent:
            print('ERROR: [%s] return status: [%d], output: [%s]' %
                  (extra + cmd, status, output))
        if do_raise:
            raise RuntimeError('Invalid return code: %s' % status)
    if output:
        output = output.split('\n')
    return (status, output)


def to_float(text):
    if text == 'Infinity':
        # float('Infinity') return inf :/
        return 0
    try:
        x = float(text.replace(',', '.'))
    except ValueError:
        x = 0
    return x

def unzip(filename):
    """Return true if zipped."""
    if not os.path.exists(filename):
        if os.path.exists(filename + '.gz'):
            command('gunzip '+ filename + '.gz')
            return True
        else:
            print "WARN: %s not found" % filename
            raise ValueError("WARN: file %s not found." % filename)
    return False

def rezip(filename, zipped):
    if zipped:
        command('gzip '+ filename)


class BaseChart:
    """Base class for log charting."""
    png_size = "640,480"
    _scale = None

    def __init__(self, log_file, out_directory, title, **options):
        try:
            zipped = unzip(log_file)
        except ValueError:
            return
        self.log_file = log_file
        self.title = title
        self.options = options
        self.out_directory = out_directory
        self.prefix = os.path.splitext(os.path.basename(log_file))[0]
        self.suffix = self.options.get('suffix', '')
        filename = self.prefix + self.suffix
        self.data_file = os.path.join(out_directory, filename + '.data')
        self.gplot_file = os.path.join(out_directory, filename + '.gplot')
        self.png_file = os.path.join(out_directory, filename + '.png')
        self._scale = options.get('scale')
        print "Processing %s" % self.data_file
        try:
            self.processLog()
        except RuntimeError:
            print "Aborting chart " + title
            return
        finally:
            rezip(log_file, zipped)
        print "Processing %s" % self.gplot_file
        self.generateScript()
        print "Processing %s" % self.png_file
        self.generateChart()

    def processLog(self):
        """Generate the gnuplot data."""

    def generateScript(self):
        """Generate the gnuplot script."""

    def generateChart(self):
        """Generate the png chart."""
        command("gnuplot " + self.gplot_file)

    def scale(self, s):
        if self._scale is not None:
            return self._scale
        if not s:
            return 1
        # Get the nearest higher or equal power of 10
        sc = ceil(log10(abs(s) / 101.))
        # This is a hack, but I wanted to avoid 0.00999999. I prefer 0.01.
        res = 1
        for x in range(int(abs(sc))):
            res = res * 10
        if sc < 0:
            return res
        return 1.0 / res



def check_midnight(start, line):
    """Fix time to add 24h after midnight"""
    try:
        hour = int(line[:2])
    except ValueError:
        return  start, line
    if start is None:
        start = hour
    if hour >= start:
        return start, line
    return start, str(hour + 24) + line[2:]


class GCMovingThroughput:
    gcs = []

    def __init__(self, duration=60):
        """Duration for the moving throughput in second."""
        self.duration = duration
        self.gcs = []

    def add(self, time, minor, major):
        time = datetime.strptime(time, '%H:%M:%S')
        minor = float(minor)
        major = float(major)
        to_drop = 0
        for gc in self.gcs:
            if (time - gc[0]).seconds > self.duration:
                to_drop += 1
            else:
                break
        if to_drop:
            self.gcs = self.gcs[to_drop:]
        self.gcs.append((time, minor, major))

    def getMovingThroughput(self):
        total = 0
        for gc in self.gcs:
            total += (gc[1] + gc[2])
        return str(1 - (total / self.duration))

class GCChart(BaseChart):
    """Process a gc log file.

    ...
    3.099: [GC [PSYoungGen: 94415K->6528K(305856K)] 94415K->6528K(1004928K), 0.0205770 secs]
    3.120: [Full GC [PSYoungGen: 6528K->0K(305856K)] [PSOldGen: 0K->6320K(699072K)] 6528K->6320K(1004928K) [PSPermGen: 13070K->13070K(26432K)], 0.0556970 secs]
    ...
    """
    def processLog(self):
        min_time = self.options['min_time']
        if min_time:
            min_time = datetime.strptime(min_time, '%H:%M:%S')
            min_time = min_time.hour * 3600 + min_time.minute * 60 + min_time.second
        else:
            min_time = 0
        log = open(self.log_file, 'r')
        f = open(self.data_file, 'w+')
        f.write('time Minor Major Throughput-1min\n')
        gcmt = GCMovingThroughput()
        for i, line in enumerate(log):
            if "[GC " in line:
                try:
                    minor = line.split(', ')[1].split(' ')[0]
                except IndexError:
                    print "Skip line %d: %s" % (i + 1, line.strip())
                    continue
                major = 0
            elif "[Full GC " in line:
                minor = 0
                try:
                    major = line.split(', ')[1].split(' ')[0]
                except IndexError:
                    print "Skip line %d: %s" % (i + 1, line.strip())
                    continue
            else:
                continue
            time = line.split(':')[0]
            time = datetime.fromtimestamp(float(time) + min_time).strftime('%H:%M:%S')
            gcmt.add(time, minor, major)
            f.write(("%s %s %s %s" % (time, minor, major, gcmt.getMovingThroughput())).replace(',', '.') + '\n')
        f.close()
        log.close()

    def generateScript(self):
        gplot = open(self.gplot_file, 'w+')
        gplot.write('''set terminal png size %s
set title "%s"
set output "%s"
set xdata time
set timefmt "%%H:%%M:%%S"
set format x "%%H:%%M"
set datafile missing "NaN"
set datafile missing "Infinity"
set grid back
# cols  ['minor', 'major']
plot "%s" u 1:3 smooth frequency w impulses t "Major","" u 1:2 smooth frequency w impulses t "Minor", "" u 1:4 with lines t "0.01 * Throughput 1min"
''' %  (self.png_size, self.title, self.png_file, self.data_file))
        gplot.close()



class MBeanChart(BaseChart):
    """Process a log file produce by logging-monitor.

    ...
    2009-06-05 04:05:19,355 INFO  [jvm] jboss.system:type=ServerInfo monitor format: (ActiveThreadCount,FreeMemory,TotalMemory,MaxMemory)
    2009-06-05 04:05:19,356 INFO  [jvm] 27,754073584,1029046272,1029046272
    ...
    """
    script_header_tpl = '''set terminal png size %s
set title "%s"
set output "%s"
set xdata time
set timefmt "%%H:%%M:%%S"
set format x "%%H:%%M"
set yrange [0:105]
set datafile missing "NaN"
set datafile missing "Infinity"
set grid back
'''
    script_line_tpl = '''"%s" using 1:(%s*$%s) with lines title "%s * %s"'''
    min_time = None

    def processLog(self):
        log = open(self.log_file, 'r')
        f = open(self.data_file, 'w+')
        while True:
            line = log.readline()
            if not line:
                break
            if "format: (" in line:
                column = line.split('(')[1][:-2].split(',')
                f.write('time ' + ' '.join(column) + '\n')
                continue
            time = line[11:19]
            if self.min_time is None:
                self.min_time = time
            columns = line.split(']')[1].split(',')
            f.write((time+ ' ' + ' '.join(columns)).replace(',', '.'))
        f.close()
        log.close()

    def generateScript(self):
        gplot = open(self.gplot_file, 'w+')
        gplot.write(self.script_header_tpl % (self.png_size, self.title, self.png_file))
        cols = self.options['cols']
        data = open(self.data_file, 'r')
        titles = None
        maxes = []
        for x in cols[1:]:
            maxes.append(0)
        lines = []
        count = 0
        line = data.readline()
        last_hour = 0
        while line:
            count += 1
            if line is None:
                break
            row = re.split("[\ \t;]+", line)
            try:
                values = [row[x].strip() for x in cols]
            except IndexError:
                print "WARN: Skip invalid line %d: %s" % (count, line)
                line = data.readline()
                continue
            # print values
            if titles is None:
                titles = values
            else:
                hour = int(values[0][:2])
                if hour < last_hour:
                    hour += 24
                    values[0] = str(hour) + values[0][2:]

                last_hour = hour
                #data.write(' '.join(values) + '\n')
                maxes = [max(maxes[i], to_float(x)) for i, x
                         in enumerate(values[1:])]
            line = data.readline()

        data.close()
        gplot.write("# cols  " + str(titles[1:]) + "\n")
        gplot.write("# maxes " + str(maxes) + "\n")
        i = 0
        lines = []
        for title in titles[1:]:
            lines.append(self.script_line_tpl % (self.data_file,
                                                 self.scale(maxes[i]),
                                                 cols[i+1] + 1,
                                                 self.scale(maxes[i]),
                                                 title))
            i += 1
        gplot.write('plot '+ ','.join(lines) + '\n')
        gplot.close()

class SarChart(MBeanChart):
    """Process sysstat sar file."""

    def processLog(self):
        tmp_file = None
        code, ret = command("file " + self.log_file + " | grep ASCII",
                            do_raise=False, silent=True)
        sar_command = self.options.get('sar_command', 'sar')
        sar_options = self.options.get('sar_options', '')
        sar_filter = self.options.get('filter')
        suffix = self.suffix
        if not code:
            # Ascii sysstat file nothing to do
            log = open(self.log_file)
        else:
            f, tmp_file = mkstemp(prefix='logchart_')
            command(sar_command + ' ' + sar_options +'  -f ' +
                    self.log_file + ' > ' + tmp_file)
            log = open(tmp_file, 'r')
        f = open(self.data_file, 'w+')
        has_header = False
        start = None
        while True:
            line = log.readline()
            if not line:
                break
            if (not sar_options and "%user" in line) or (
                "-d" in sar_options and "%util" in line):
                if has_header:
                    continue
                has_header = True
            elif ("%nice" in line) and (suffix == '-cpu'):
                if has_header:
                    continue
                has_header = True
            elif "Average" in line:
                continue
            elif sar_filter and sar_filter not in line:
                continue
            start, line = check_midnight(start, line)
            f.write(' '.join(line.split()) + '\n')
        f.close()
        log.close()
        if tmp_file:
            os.unlink(tmp_file)

MONITOR_RST = '''
============================
LogChart monitoring report
============================

:abstract: Aggregation of miscellaneous monitor logs during a test run.
           This report is done with LogChart_ and pgFouine_ to enhance
           FunkLoad_ default monitoring.

.. _FunkLoad: http://funkload.nuxeo.org/
.. _LogChart: http://svn.nuxeo.org/nuxeo/tools/qa/logchart/trunk
.. _pgFouine: http://pgfouine.projects.postgresql.org/

.. sectnum::    :depth: 2
.. contents:: Table of contents


CPU
-------------


 .. image:: sysstat-sar-cpu.png

* %user: Percentage of CPU utilization that occurred while executing at the
  user level (application).

* %nice: Percentage of CPU utilization that occurred while executing at the
  user level with nice priority.

* %system: Percentage of CPU utilization that occurred while executing at the
  system level (kernel).

* %iowait: Percentage of time that the CPU or CPUs were idle during which the
  system had an outstanding disk I/O request.

* %steal: Percentage of time spent in involuntary wait by the virtual CPU or
  CPUs while the hypervisor was servicing another virtual processor.


Disk utilization
-----------------

 .. image:: sysstat-sar-disk.png

* rd_sec/s: Number of sectors read from the device. The size of a sector is 512
  bytes.

* wr_sec/s: Number of sectors written to the device. The size of a sector is
  512 bytes.

* %util: Percentage of CPU time during which I/O requests were issued to the
  device (bandwidth utilization for the device). Device saturation occurs when
  this value is close to 100%.


Garbage Collector
-------------------

 .. image:: gc.png


* Minor: Objects are allocated in eden, and because of infant mortality most
  objects die there. When Eden fills up it causes a minor collection, in which
  some surviving objects are moved to an older generation

* Major: (aka Full GC) When older generations need to be collected there is a major
  collection that is often much slower because it involves all living objects.

* Throughput 1min: This is the percentage of total time not spent in garbage collection,
  considered over the last minute.


Global HTTP request processor
-------------------------------

 .. image:: webrequests.png

* bytesSent: Bytes sent by the http connector.

* bytesReceived: Bytes received by the http connector.

* processingTime: processing time of the http connector

* errorCount: Number of errors.

* maxTime: Max http request time

* requestCount: Number of http requests.


HTTP Thread pool
-------------------

 .. image:: webthreads.png

* currentThreadCount: Number of threads in the pool

* currentThreadsBusy: Number of busy threads in the pool

* maxThreads: Max number of threads that you can open in the pool


JCA Datasource Connection Pool
--------------------------------


* InUseConnectionCount: Shows how many of the open connections are in use

* AvailableConnectionCount: Shows how much room is left in the pool

* ConnectionCreatedCount and ConnectionDestroyedCount keep running totals of
  the number of connections created and destroyed by the pool

* MaxConnectionsInUseCount: Keeps track of the highest number of connections in
  use at a time


NuxeoDS
~~~~~~~~

 .. image:: nuxeo-ds.png

NXRepository/default
~~~~~~~~~~~~~~~~~~~~~~

 .. image:: vcs-ds.png



JVM Server info
----------------

 .. image:: jvm.png

* ActiveThreadCount: The current number of active threads for this app server
  instance

* FreeMemory: The amount of free memory for the JVM this app server instance is
  running on

* TotalMemory: The amount of total memory for the JVM this app server instance
  is running on

* MaxMemory: The amount of max memory for the JVM this app server instance is
  running on

'''

PG_RST = '''\

PostgreSQL log analysis
-------------------------

* pgFouine `log analysis report`_

.. _`log analysis report`: pgfouine.html

'''

VACUUM_RST = '''\

PostgreSQL VACUUM analysis
---------------------------

* pgFouine `VACUUM log analysis report`_

.. _`VACUUM log analysis report`: pgfouine-vacuum.html


'''





def main():
    """Main test"""
    parser = OptionParser(USAGE, formatter=TitledHelpFormatter(),
                          version="LogChart " + VERSION)
    parser.add_option("--device", type="string",
                      dest="device", default="sda",
                      help="Disk device filter: sda1, dev253-3 ...")
    parser.add_option("--with-sar", type="string",
                      dest="sar", default="sar",
                      help="Path of the sar command to use")
    parser.add_option("--pg-logtype-stderr", action="store_true",
                      dest="stderrlogtype", default=False,
                      help="PostgreSQL log is stderr type not syslog")

    options, args = parser.parse_args()
    if len(args) != 2:
        parser.error("incorrect number of arguments")
    log_dir = args[0]
    report_dir = args[1]
    device = options.device

    if not os.access(report_dir, os.W_OK):
        os.mkdir(report_dir, 0775)


    # TODO: move this to a configuration file
    MBeanChart(os.path.join(log_dir, 'webrequests.log'),
               report_dir, "Global http requests",
               cols=[0, 1, 2, 3, 4, 5])

    MBeanChart(os.path.join(log_dir, 'webthreads.log'),
               report_dir, "Tomcat Thread Pool",
               cols=[0, 1, 2, 3])

    MBeanChart(os.path.join(log_dir, 'nuxeo-ds.log'),
               report_dir, "NuxeoDS Connection Pool",
               cols=[0, 1, 2, 3, 4, 5])

    MBeanChart(os.path.join(log_dir, 'vcs-ds.log'),
               report_dir, "NXRepository/default Connection Pool",
               cols=[0, 1, 2, 3, 4, 5])

    a = MBeanChart(os.path.join(log_dir, 'jvm.log'),
                   report_dir, "JVM",
                   cols=[0, 1, 2, 3, 4])

    min_time = a.min_time
    GCChart(os.path.join(log_dir, 'gc.log'), report_dir,
            "Garbage Collector", min_time=min_time)

    SarChart(os.path.join(log_dir, 'sysstat-sar.log'),
             report_dir, "CPU (all)",
             sar_options="", scale=1,
             cols=[0, 2, 3, 4, 5, 6], filter=" all ", suffix="-cpu",
             sar_command=options.sar)

    SarChart(os.path.join(log_dir, 'sysstat-sar.log'),
             report_dir, "Disk utilization (%s)" % device,
             sar_options="-d -p",
             cols=[0, 4, 3, 9], filter=" "+device, suffix="-disk",
             sar_command=options.sar)

    # pgfouine reports
    pglog = os.path.join(log_dir, 'pgsql.log')
    try:
        cmd = 'pgfouine'
        if options.stderrlogtype:
            cmd += ' -logtype stderr'
        zipped = unzip(pglog)
        code, ret = command(cmd + ' -file ' + pglog +
                ' -top 30 > ' + os.path.join(report_dir, 'pgfouine.html'),
                do_raise=False)
        rezip(pglog, zipped)
        if not code:
            global MONITOR_RST
            MONITOR_RST += PG_RST
    except ValueError:
        pass
    vacuumlog = os.path.join(log_dir, 'vacuum.log')
    try:
        zipped = unzip(vacuumlog)
        code, ret = command('pgfouine_vacuum -file ' + vacuumlog +
                ' -filter unknown.public > ' + os.path.join(report_dir,
                'pgfouine-vacuum.html'), do_raise=False)
        rezip(vacuumlog, zipped)
        if not code:
            global MONITOR_RST
            MONITOR_RST += VACUUM_RST
    except ValueError:
        pass


    # Create a ReST report
    rst_file = os.path.join(report_dir, 'monitor.rst')
    f = open(rst_file, 'w+')
    f.write(MONITOR_RST)
    f.close()

    # Generate html
    html_file = os.path.join(report_dir, 'monitor.html')
    css_file = 'http://funkload.nuxeo.org/funkload.css'
    cmdline = "-t --link-stylesheet --stylesheet=%s %s %s" % (
        css_file, rst_file, html_file)
    cmd_argv = cmdline.split(' ')
    publish_cmdline(writer_name='html', argv=cmd_argv)


if __name__ == '__main__':
    main()
