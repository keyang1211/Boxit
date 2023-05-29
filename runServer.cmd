@ECHO OFF
SET levelpath=%1
RMDIR /s /q .\out\
javac newSearchClient/*.java -d ./out
java -jar server.jar -l %levelpath% -c "java -classpath out newSearchClient.SearchClient" -g -s 150 -t 180