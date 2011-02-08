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


all: createUsers testCMF

createUsers: start users-CMF stop

createCaseItems: start create-Cases stop

testCMF: start test-CMF stop

testBench: start test-bench stop

start:
	-mkdir -p $(REPORT) $(LOG_HOME)
	-$(MONCTL) restart
	$(CREDCTL) restart

stop:
	-$(MONCTL) stop
	-$(CREDCTL) stop

users-CMF: 
	fl-run-test -d  --simple-fetch users_CMF.py $(FLOPS)
	
test-CMF:
	fl-run-test -d  --simple-fetch test_CMF.py $(FLOPS)
	
create-Cases:
	fl-run-test -d -V --simple-fetch create_Cases.py $(FLOPS)	

test-bench:
	-fl-run-bench --simple-fetch test_CMF.py CMF.test_CMF  --label=test -c 10 -D 300 -m 0.1 -M .5 -s 1 $(FLOPS)
	-fl-build-report -T 3 $(LOG_HOME)/cmf-bench.xml --html -o $(REPORT)

clean:
	-find . "(" -name "*~" -or  -name ".#*" -or  -name "*.pyc" ")" -print0 | xargs -0 rm -f
