for i in {1..4}
do
	paste -d' ' smarthome/q$i.result.out Queries/smarthome/q$i.epl.out  >  Queries/smarthome/q$i.times.out
	awk '{$3=$1-$2} 1' Queries/smarthome/q$i.times.out > Queries/smarthome/q$i.diff.out 
done
