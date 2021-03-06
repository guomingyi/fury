
all:
	$(MAKE) -C ./js all
	$(MAKE) -C ./oled all
	$(MAKE) -C ./servoBlaster/user all
	$(MAKE) -C ./fury_server all


install:
	$(MAKE) -C ./js install
	$(MAKE) -C ./oled install
	$(MAKE) -C ./servoBlaster/user install
	$(MAKE) -C ./fury_server install

uninstall:
	$(MAKE) -C ./js uninstall
	$(MAKE) -C ./oled uninstall
	$(MAKE) -C ./servoBlaster/user uninstall
	$(MAKE) -C ./fury_server uninstall


clean:
	$(MAKE) -C ./js clean
	$(MAKE) -C ./oled clean
	$(MAKE) -C ./servoBlaster/user clean
	$(MAKE) -C ./fury_server clean

.PHONY : all install clean
