BUILD_TARGET := liboled.so
DESTDIR?=/usr/local/lib

LD := g++
CCFLAGS := -Wall
CCFLAGS += -g
CCFLAGS += -shared

DEPSFLAGS := -lpthread 
DEPSFLAGS += -lwiringPi -fPIC

BUILD_SRC := \
    $(shell find . -name '*.c' -o -name '*.cpp' -o -name '*.h')


all:$(BUILD_SRC)
	$(LD) -o $(BUILD_TARGET) $(CCFLAGS) $(BUILD_SRC) $(DEPSFLAGS)  
	cp ./$(BUILD_TARGET) $(DESTDIR)/$(BUILD_TARGET)

install:
	cp ./$(BUILD_TARGET) $(DESTDIR)/$(BUILD_TARGET)
	ldconfig

uninstall:
	rm $(DESTDIR)/$(BUILD_TARGET)

clean:  
	rm -rf *.o $(BUILD_TARGET) 


.PHONY : all clean
