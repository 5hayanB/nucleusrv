echo "Generating log"
cd ../
sbt "testOnly nucleusrv.components.TopTest -- -DwriteVcd=1 -DprogramFile=./test.hex" > ./trace/core.log
echo "Log generated successfully"
