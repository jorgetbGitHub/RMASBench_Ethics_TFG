#!\bin\sh

totalSimulations=10;
argsFiles='';

for i in $(seq 1 $totalSimulations); do
    ./start.sh -v -b -c example -m paris -s Ambulancias_Collapse_ConySinNormas
done

sleep 3s

argsFiles=$(find | grep "report.\.txt")

echo $argsFiles;

echo "$totalSimulations simulations have been finished":
cd ..;
cd ..;
java -jar "AnalyticsProcess/dist/AnalyticsProcess.jar" $argsFiles > out