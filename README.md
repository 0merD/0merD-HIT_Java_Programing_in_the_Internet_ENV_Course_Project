# Introduction
This project is made for the Programming in the Internet Environment course in HIT using Java.
Our goal is to have one server to handle requests and several clients that represent clothes shops connecting to it.

# Installation
We used Intellij IDEA as our IDE, and most things installed on their own, except for google's library for handing JSONs, Gson.
File > Project Structure > Libraries > "+" (New Library Project) > From Maven > Search > add com.google.code.gson:gson:2.13.1
In different IDEs the process will be different, but gson was the only library we needed to add in this way.

# Usage
To run the project, run "Server" (/src/server/Server) to start the server, and "ClientChat" (src/Client/ClientChat) for the branches.
You may need to enable multiple instances, in Intellij: Rightclick file > More Run/Debug > Modify Run Configuration > Build and Run > Modify Options > check "Allow multiple instances"
Once you've run both Server and ClientChat, you can now login to the system. Information under /resources/users.json.
As Admin, you can add or modify a new user with the menu.
As Basic Worker, you can use the menu to navigate your options.
As Shift Manager, you have the same functionality as Basic Worker, but also the ability to view all chats and join existing chat.
