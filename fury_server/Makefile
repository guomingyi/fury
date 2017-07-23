BUILD_TARGET := furyd
DAEMON := furyd
DESTDIR?=/usr/local/bin
DYNAMIC:=/usr/local/lib

LD := g++
CCFLAGS := -Wall
CCFLAGS += -g

DEPSFLAGS := -lpthread 
DEPSFLAGS += -lwiringPi
DEPSFLAGS += -lm -lcrypt -lrt -lm
DEPSFLAGS += -loled
DEPSFLAGS += -ljsx

BUILD_SRC := \
    $(shell find . -name '*.c' -o -name '*.cpp' -o -name '*.h')

all:$(BUILD_SRC)
	$(LD) -o $(BUILD_TARGET) $(CCFLAGS) $(BUILD_SRC) $(DEPSFLAGS)  

install: $(BUILD_TARGET)
	[ "`id -u`" = "0" ] || { echo "Must be run as root"; exit 1; }
	cp -f $(BUILD_TARGET) /usr/local/sbin/$(DAEMON)
	cp -f init-script /etc/init.d/$(BUILD_TARGET).sh
	chmod 755 /etc/init.d/$(BUILD_TARGET).sh
	update-rc.d $(BUILD_TARGET).sh defaults 92 08
	/etc/init.d/$(BUILD_TARGET).sh start


uninstall:
	rm -rf $(DESTDIR)/$(BUILD_TARGET)
	rm -rf /usr/local/sbin/$(DAEMON)
	rm -rf /etc/init.d/$(BUILD_TARGET).sh

clean:  
	rm -rf *.o $(BUILD_TARGET) 


.PHONY : all clean
