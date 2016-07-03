
all:
	$(MAKE) -C ./oled
	$(MAKE) -C ./fury_server
	$(MAKE) -C ./ServoBlaster/user


clean:
	$(MAKE) -C ./oled clean
	$(MAKE) -C ./fury_server clean
	$(MAKE) -C ./ServoBlaster/user clean

.PHONY : all clean
