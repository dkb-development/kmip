# KMIP Server

This is a Java-based KMIP server implementation using Spring Boot.

## Prerequisites

- Java 17
- Maven 3.6 or higher

## Installation

### Java Installation
1. Verify Java installation:
```cmd
java -version
```
Should show Java 17.x.x

### Maven Installation
1. Download Maven:
   - Go to https://maven.apache.org/download.cgi
   - Download the Binary zip archive (apache-maven-3.9.6-bin.zip)

2. Install Maven:
   - Create directory: `C:\Program Files\Apache\maven`
   - Extract the zip to this directory
   - You should have: `C:\Program Files\Apache\maven\apache-maven-3.9.6`

3. Add Maven to PATH:
   - Open Windows Settings
   - Search for "Environment Variables"
   - Click "Edit the system environment variables"
   - Click "Environment Variables" button
   - Under "System Variables", find and select "Path"
   - Click "Edit"
   - Click "New"
   - Add: `C:\Program Files\Apache\maven\apache-maven-3.9.6\bin`

4. Verify Maven installation:
```cmd
mvn --version
```

## Running the Server

1. Navigate to the server directory:
```cmd
cd kmip_server
```

2. Build the project:
```cmd
mvn clean install
```

3. Run the server:
```cmd
mvn spring-boot:run
```

The server will start and listen on port 5696 (default KMIP port).

## Testing the Server

Once the server is running, you can test it using the Python KMIP client in the `kmip_client` directory.

## Server Configuration

The server is configured to:
- Listen on port 5696
- Accept TCP connections
- Log incoming KMIP requests
- Handle multiple client connections

## Troubleshooting

### Maven Not Found
If `mvn` is not recognized:
1. Verify Maven is in PATH:
```cmd
echo %PATH%
```
2. Check Maven installation:
```cmd
dir "C:\Program Files\Apache\maven"
```

### Port Already in Use
If you get an error about port 5696 being in use:
1. Find the process using the port:
```cmd
netstat -ano | findstr :5696
```
2. Kill the process:
```cmd
taskkill /PID <process_id> /F
```

### Java Version Issues
Make sure you have Java 17 installed. You can check your Java version:
```cmd
java -version
```

### Maven Issues
If you encounter Maven issues:
1. Try cleaning the Maven cache:
```cmd
mvn clean
```
2. Update Maven dependencies:
```cmd
mvn dependency:purge-local-repository
``` 