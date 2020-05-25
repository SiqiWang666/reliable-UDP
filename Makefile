# Define variable
JFLAGS = -g
JC = javac
WORKDIR = src/main

# Clear default targets
.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = $(WORKDIR)/Sender.java

classes: $(CLASSES:.java=.class)

clean:
	$(RM) $(WORKDIR)/*.class
	$(RM) 127.*

build: clean classes

test: build
	python TestHarness.py -s JavaSender.py -r Receiver.py

# Define default arguments
file = README
port = 33122
address = localhost
ack = 4567

run: build
	java src.main.Sender -f $(file) -p $(port) -a $(address) -k $(ack)

debug: build
	java src.main.Sender -f $(file) -d