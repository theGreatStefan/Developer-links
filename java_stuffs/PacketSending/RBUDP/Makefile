JFLAGS = -g
JC = javac
JVM = java

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	  sender.java \
	  receiverFrame.java \
	  receiver.java \
	  senderFrame.java \
	  FileServer.java \
	  Tut2.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

