BUILD_TARGET := fury_server

DESTDIR?=/usr/local/bin
DYNAMIC:=/usr/local/lib

LD := g++
CCFLAGS := -Wall
CCFLAGS += -g

DEPSFLAGS := -lpthread 
DEPSFLAGS += -lwiringPi
DEPSFLAGS += -lm -lcrypt -lrt -lm
#DEPSFLAGS += -loled
#DEPSFLAGS += -ljsx
DEPSFLAGS += \
    $(DYNAMIC)/liboled.so \
    $(DYNAMIC)/libjsx.so

BUILD_SRC := \
    $(shell find . -name '*.c' -o -name '*.cpp' -o -name '*.h')

all:$(BUILD_SRC)
	$(LD) -o $(BUILD_TARGET) $(CCFLAGS) $(BUILD_SRC) $(DEPSFLAGS)  

install:
	cp ./$(BUILD_TARGET) $(DESTDIR)/$(BUILD_TARGET)

clean:  
	rm -rf *.o $(BUILD_TARGET) 


.PHONY : all clean
