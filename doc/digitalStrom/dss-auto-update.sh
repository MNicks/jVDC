#!/bin/bash

#The server where the files should be uploaded to
DSS="dss-cbumae.local"

declare -A FILES
#List here all files you want to have monitored
#FILES[path_to_the_file_on_your_machine]="path_to_the_destination_directory_on_the_server"
FILES[/home/cbumae/ETH/mt/src/smart-grid/ui/js/main.js]="/www/pages/add-ons/smart-grid/js/"
FILES[/home/cbumae/ETH/mt/src/smart-grid/ui/js/time.js]="/www/pages/add-ons/smart-grid/js/"
FILES[/home/cbumae/ETH/mt/src/smart-grid/ui/js/configWindow.js]="/www/pages/add-ons/smart-grid/js/"
FILES[/home/cbumae/ETH/mt/src/smart-grid/ui/js/dss/dss-json.js]="/www/pages/add-ons/smart-grid/js/dss/"
FILES[/home/cbumae/ETH/mt/src/smart-grid/ui/js/deviceWindow.js]="/www/pages/add-ons/smart-grid/js/"
FILES[/home/cbumae/ETH/mt/src/smart-grid/ui/ds_gui.css]="/www/pages/add-ons/smart-grid/"
FILES[/home/cbumae/ETH/mt/src/smart-grid/ui/index.html]="/www/pages/add-ons/smart-grid/"
FILES[/home/cbumae/ETH/mt/src/smart-grid/scripts/smartgrid.js]="/usr/share/dss/add-ons/smart-grid/"


while true; do
	#loop over all the files
	for i in ${!FILES[@]}; do
		#Check files for new changes
		if [ -N $i ]; then
			#do some logging
			date=`date +"[%Y-%m-%d %H:%M:%S]"`
			echo "$date $i"

			#actually copy the file to the server
			scp $i dssadmin@$DSS:${FILES[$i]} > /dev/null
			#update the access field of the file
			touch -a $i
		fi
	done
	sleep 0.5
done;
