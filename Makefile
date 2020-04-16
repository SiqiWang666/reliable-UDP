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

debug: build
	java src.main.Sender -f README -d