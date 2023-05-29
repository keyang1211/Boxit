rm out/newSearchClient -r
javac newSearchClient/*.java -d ./out
java -jar server.jar -l "$1" -c "java -cp out newSearchClient.SearchClient" -g -s 150 -t 180