BUILD_TARGET := libjsx.so
DESTDIR?=/usr/local/lib

LD := gcc
CCFLAGS := -Wall
CCFLAGS += -g
CCFLAGS += -shared

DEPSFLAGS := -lpthread 
DEPSFLAGS += -fPIC 
#DEPSFLAGS += -lwiringPi

BUILD_SRC := \
    $(shell find . -name '*.c' -o -name '*.cpp' -o -name '*.h')


all:$(BUILD_SRC)
	$(LD) -o $(BUILD_TARGET) $(CCFLAGS) $(BUILD_SRC) $(DEPSFLAGS)  

install:
	cp ./$(BUILD_TARGET) $(DESTDIR)/$(BUILD_TARGET)

clean:  
	rm -rf *.o $(BUILD_TARGET) 


.PHONY : all clean
