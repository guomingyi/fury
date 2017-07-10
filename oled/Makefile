BUILD_TARGET := oled.so


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


clean:  
	rm -rf *.o $(BUILD_TARGET) 


.PHONY : all clean
