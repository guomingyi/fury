LD := gcc
CCFLAGS := -Wall
CCFLAGS += -g

DEPSFLAGS := -lpthread 
DEPSFLAGS += -fPIC 

ifeq ($(strip $(EXEC)),)
    BUILD_TARGET := libjsx.so
    DESTDIR?=/usr/local/lib
    CCFLAGS += -shared
else
    BUILD_TARGET := jsx
    DESTDIR?=/usr/local/bin
endif

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
