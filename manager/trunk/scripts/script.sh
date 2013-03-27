#!/bin/bash
 
INPUT=$1
echo $INPUT
PROFILE=$2
NUMLVL=$(( $3+2 ))
MP4BOX=$4
NAME=$5
LVL=$6

function bucle(){
	CONTADOR=1
	while [ $CONTADOR -lt $NUMLVL ]
	do
		MP4BOX=$MP4BOX" """$NAME""$CONTADOR".mp4"
		let CONTADOR++
	done
}

echo "/Users/i2cat/ffmpeg/ffmpeg $INPUT $PROFILE $LVL"
/Users/i2cat/ffmpeg/ffmpeg $INPUT $PROFILE $LVL
	
if [[ $? -eq 0 ]]; then
	bucle
	MP4Box $MP4BOX
	exit $?
else
	exit $?
fi