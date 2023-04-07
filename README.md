## Building
Run `./gradlew shadowJar` using JDK 8 or later

## Running
Run `java -jar build/libs/ResourcePackHost.jar` to run with default settings

### Arguments
The following command demonstrates how all arguments should be passed.
Note that all arguments are optional.
```
java -jar build/libs/ResourcePackHost.jar port=<port> backlog=<backlog> folder=<folder>
```
- **port**: The port number to listen on, default value is 80
- **backlog**: The socket backlog, default value is 0, which implies that the system default will be used
- **folder**: The directory where the resourcepacks will be stored, default value is "./resource-packs/"

### Using the *screen* command
If you intend to let the server run for a long time, you can use the **screen** command
to keep it running after you close the terminal that started it.
1. Start a new session using `screen -S resourcepack-server`
2. Run the server using `java -jar path-to-jar`
3. Exit the screen using Control A D
4. Return to the terminal using `screen -x resourcepack-server`