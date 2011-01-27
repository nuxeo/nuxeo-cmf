CREDCTL := fl-credential-ctl credential.conf
MONCTL := fl-monitor-ctl monitor.conf
LOG_HOME := ../../target/ftest/funkload/log

ifdef URL
	FLOPS = -u $(URL) $(EXT)
else
	FLOPS = $(EXT)
endif

ifdef REPORT_HOME
	REPORT = $(REPORT_HOME)
else
	REPORT = ../../target/ftest/funkload/report
endif


all: start testCMF stop

testCMF: start test-CMF stop

start:
	-mkdir -p $(REPORT) $(LOG_HOME)
	-$(MONCTL) restart
	$(CREDCTL) restart

stop:
	-$(MONCTL) stop
	-$(CREDCTL) stop

test-CMF:
	fl-run-test -d  -V --simple-fetch test_CMF.py $(FLOPS)



clean:
	-find . "(" -name "*~" -or  -name ".#*" -or  -name "*.pyc" ")" -print0 | xargs -0 rm -f
