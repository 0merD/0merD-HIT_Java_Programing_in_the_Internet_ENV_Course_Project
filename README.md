# Introduction
This project is made for the "Programming in the Internet Environment" course in HIT using Java.  
Our goal is to have one server to handle requests and several clients that represent clothes shops connecting to it.

# Installation
We used Intellij IDEA as our IDE, and most things installed on their own, except for google's library for handing JSONs, Gson.
File > Project Structure > Libraries > "+" (New Library Project) > From Maven > Search > add com.google.code.gson:gson:2.13.1
In different IDEs the process will be different, but gson was the only library we needed to add in this way.  
In image form:  
<img width="1651" height="538" alt="image" src="https://github.com/user-attachments/assets/3fc3f8be-6570-4be0-9791-5e687629c20d" />


# Usage
To run the project, run "Server" (src/server/Server.java) to start the server, and "ChatClientFinal" (src/Client/ChatClientFinal.java) for the branches.
You may need to enable multiple instances, in Intellij: Rightclick file > More Run/Debug > Modify Run Configuration > Build and Run > Modify Options > check "Allow multiple instances"  
In image form:  
<img width="1863" height="908" alt="image" src="https://github.com/user-attachments/assets/7bfda886-a3ea-4048-a135-9b60dcc29be3" />

Once you've run both Server and ChatClientFinal, you can now login to the system. Login information under /resources/users.json.  

As Admin, you can add or modify a new user with the menu.  
As Basic Worker, you can use the menu to navigate your options.  
As Shift Manager, you have the same functionality as Basic Worker, but also the ability to view all chats and join existing chat.  

Each worker is assigned to one branch, and each branch has its own inventory.  
Branch numbers: 1 (Tel Aviv), 2 (Haifa), 3 (Jerusalem).  
Discounts: Applied automatically, based on customer's discount registry.  
Customer upgrade threshold is set in a configureable file, src/server/ThresholdConsts.java.  
Inventory for each branch is set from the start of the server.  



AI summary:  
A comprehensive Java-based server application for managing retail operations across multiple branches, featuring real-time chat communication, inventory management, sales processing, and customer relationship management.  

## Key Features  

### 🏪 Multi-Branch Operations  
- **Branch Management**: Support for multiple retail branches (Tel Aviv, Haifa, Jerusalem)  
- **Branch-Specific Inventory**: Independent inventory tracking per location  
- **User Branch Assignment**: Each user is assigned to a specific branch  
  
### 👥 Role-Based Access Control  
- **Admin**: User management privileges.  
- **Shift Manager**: Same retail operations as Basic Worker, with the added ability to join existing chats.  
- **Basic Worker**: Standard retail operations within their branch  
  
### 💬 Chat System  
- **Chat Queuing**: Automatic queue management when users are busy  
- **Session Management**: Join existing chats (shift managers only)  
- **Chat Logging**: Optional conversation saving  
  
### 📦 Inventory Management  
- **Real-Time Stock Tracking**: Live inventory updates per branch  
- **Concurrent Access Control**: Thread-safe inventory operations  
- **Stock Validation**: Automatic checks before sales processing  
- **Branch-Specific Views**: Users see inventory for their assigned branch  
  
### 🛒 Sales Processing  
- **Dynamic Pricing**: Automatic best discount based on customer type  
- **Multi-Strategy Discounts**: Quantity-based and percentage discounts  
  
### 👤 Customer Management  
- **Customer Types**: New, Returning, and VIP with different discount tiers  
- **Automatic Promotion**: Customers upgrade based on accumulated spending of the customer  
- **Discount Strategies**: Flexible discount system using Strategy pattern  
  
## Architecture  
  
### Design Patterns Used  
  
**Singleton Pattern**: Managers (Customer, Inventory, Sales, User) ensure single instances  
**Strategy Pattern**: Discount calculation with pluggable strategies  
**Factory Pattern**: User and Customer creation with type-specific implementations  
**Template Method**: Abstract customer class with concrete implementations  
  
### Concurrency Handling  
  
The system implements comprehensive thread safety:  
- **File-based Locking**: Per-branch inventory file access control  
- **Synchronized Collections**: Thread-safe data structures for client management  
  
### Data Persistence  
  
- **JSON Storage**: Products, customers, users, and inventory stored in JSON format  
- **Text Logging**: Chats and operations are saved into text based logs.  
  
## User Workflows  
  
### Basic Worker Operations  
2. **View Inventory** for their specific branch  
3. **Process Sales** with automatic discount calculation  
4. **Manage Customers** - add new customers and view existing ones  
5. **Chat Communication** - private conversations with colleagues  
6. **View Product Catalog** with current pricing  
  
### Shift Manager Additional Capabilities  
- **Join Active Chats** - Ability to join existing chats between workers  
   
### Admin Only Functions  
- **User Management** - add, delete, modify user accounts  
- **Role Assignment** - change user permissions and types  
