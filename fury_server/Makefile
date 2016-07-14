BUILD_TARGET := fury_server


LD := g++
CCFLAGS := -Wall
CCFLAGS += -g

DEPSFLAGS := -lpthread 
DEPSFLAGS += -lwiringPi
DEPSFLAGS += /home/pi/coding/github/fury/oled/oled.so
#DEPSFLAGS += ../oled/oled.so

BUILD_SRC := \
    $(shell find . -name '*.c' -o -name '*.cpp' -o -name '*.h')

all:$(BUILD_SRC)
	$(LD) -o $(BUILD_TARGET) $(CCFLAGS) $(DEPSFLAGS) $(BUILD_SRC)  


clean:  
	rm -rf *.o $(BUILD_TARGET) 


.PHONY : all clean
