echo "Generating log"
cd ../
sbt "testOnly nucleusrv.components.TopTest -- -DwriteVcd=1 -DprogramFile=./asm/assembly.hex" > ./trace/core.log
echo "Log generated successfully"

cd trace/
echo "Generating CSV"
python ./toCSV.py
echo "CSV generated successfully"
