#!/bin/bash
 
INPUT=$1
IN=$(( ${#INPUT}-2 ))
input=${INPUT:1:IN}
echo "input:"
echo $input

PROFILE=$2
PROF=$(( ${#PROFILE}-2 ))
profile=${PROFILE:1:PROF}
echo "profile:"
echo $profile

NUMLVL=$(( $3+2 ))

OUTPUT=$4
OUT=$(( ${#OUTPUT}-2 ))
output=${OUTPUT:1:OUT}
echo "Output:"
echo $output

MP4BOX=$5
MP4=$(( ${#MP4BOX}-2 ))
mp4box=${MP4BOX:1:MP4}
echo "MP4Box:"
echo $mp4box

NAME=$6

LEVEL=$7
LVL=$(( ${#LEVEL}-2 ))
level=${LEVEL:1:LVL}
echo "Level"
echo $level

function bucle(){
	CONTADOR=0
	ID=0
	while [ $CONTADOR -lt $NUMLVL ]
	do
		echo "$mp4box"
		mp4box=$mp4box" """$output"/"$CONTADOR".mp4:id="$ID":role="$CONTADOR""
		let CONTADOR++
		let ID++
	done
}

echo "ffmpeg $INPUT $PROFILE $LVL"
ffmpeg $input $profile $level

echo "MP4Box $mp4box"

if [[ $? -eq 0 ]]; then
	bucle
	/usr/local/bin/MP4Box $mp4box
	exit $?
else
	exit $?
fi
