echo "Generating log"
sbt "testOnly nucleusrv.components.TopTest -- -DwriteVcd=1 -DprogramFile=./test.hex" > core.log
echo "Log generated successfully"
