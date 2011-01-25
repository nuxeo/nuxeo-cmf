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


all: start test-addUsers test-addMailbox test-writer stop

addUsers: start test-addUsers stop

delUsers: start test-delUsers stop

addMailbox: start test-addMailbox stop

testDistribution: start test-distribution stop

testWriter: start test-writer stop

benchWriter: start bench-writer-run stop

benchVolumize: start bench-volumize-run stop

start:
	-mkdir -p $(REPORT) $(LOG_HOME)
	-$(MONCTL) restart
	$(CREDCTL) restart

stop:
	-$(MONCTL) stop
	-$(CREDCTL) stop

bench-writer: start bench-writer-run stop


test-writer:
	fl-run-test -d --simple-fetch test_Nxc.py Nxc.test_nxc_writer $(FLOPS)

test-addUsers:
	fl-run-test -d --simple-fetch test_CreateUser.py CreateUser.test_createUser $(FLOPS)

test-delUsers:
	fl-run-test -d --simple-fetch test_Deleteuser.py Deleteuser.test_deleteUser $(FLOPS)

test-addMailbox:
	fl-run-test -d --simple-fetch test_Createmailbox.py Createmailbox.test_createMailbox $(FLOPS)

test-distribution:
	fl-run-test -d --simple-fetch test_Distribution.py Distribution.test_distribution $(FLOPS)

bench-writer-run: 
	-fl-run-bench --simple-fetch test_Nxc.py Nxc.test_nxc_writer --label=test -c 1:2:3:4:5:6 -D 60 -m 0.1 -M .5 -s 1 $(FLOPS)
	-fl-build-report $(LOG_HOME)/nxc-bench.xml --html -o $(REPORT)

bench-volumize-run:
	-fl-run-bench --simple-fetch test_Distribution.py Distribution.test_distribution  --label=volumize -c 1:3:3:3:3:3:3:3:3:1 -D 600  -m 0.1 -M .5 -s 1 $(FLOPS)
	-fl-build-report $(LOG_HOME)/nxc-bench.xml --html -o $(REPORT)

bench-all:
	-fl-run-bench --label=long --simple-fetch -c 1:5:5:5:5:5:1 -D 60 -m 0.1 -M 1 -s 1 test_Nxc.py Nxc.test_nxc $(FLOPS)
	-fl-build-report $(LOG_HOME)/nxc-bench.xml --html -o $(REPORT)


clean:
	-find . "(" -name "*~" -or  -name ".#*" -or  -name "*.pyc" ")" -print0 | xargs -0 rm -f
