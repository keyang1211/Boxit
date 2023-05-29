@ECHO OFF
SET folderpath=%1
RMDIR /s /q .\out\
DEL /s /q .\group44.zip
javac newSearchClient/*.java -d ./out
java -jar server.jar -l %folderpath% -c "java -classpath out newSearchClient.SearchClient" -t 180 -o group44.zip