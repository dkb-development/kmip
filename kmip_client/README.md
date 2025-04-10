# KMIP Client

This is a Python-based KMIP client that demonstrates basic key management operations using the PyKMIP library.

## Prerequisites

- Python 3.11.6
- pip (Python package installer)

## Installation

1. Verify Python Installation:
```cmd
python --version
# or
py --version
```

If Python is not found, add it to your system PATH:
1. Open Windows Settings
2. Search for "Environment Variables"
3. Click "Edit the system environment variables"
4. Click "Environment Variables" button
5. Under "System Variables", find and select "Path"
6. Click "Edit"
7. Click "New"
8. Add these paths (adjust based on your Python installation):
   ```
   C:\Users\YourUsername\AppData\Local\Programs\Python\Python311
   C:\Users\YourUsername\AppData\Local\Programs\Python\Python311\Scripts
   ```
9. Close and reopen Command Prompt
10. Verify Python is now in PATH:
    ```cmd
    where python
    ```

2. Create a virtual environment (recommended):
```bash
# For Windows PowerShell:
python3.11 -m venv venv

# For Windows Command Prompt:
python3.11 -m venv venv
```

3. Activate the virtual environment:
```bash
# For Windows PowerShell (Run as Administrator):
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
.\venv\Scripts\Activate.ps1

# For Windows Command Prompt:
.\venv\Scripts\activate.bat
```

4. Verify the virtual environment is active:
```bash
# The prompt should show (venv) at the beginning
# You can also check using:
where python  # Should show venv\Scripts\python.exe
# Or:
python -c "import sys; print(sys.executable)"  # Should show venv path
```

5. Install the required packages:
```bash
pip install -r requirements.txt
```

## Usage

The client provides the following operations:
- Create symmetric key
- Get symmetric key
- Rotate symmetric key
- Destroy symmetric key

### Running the Example

1. Make sure the KMIP server is running
2. Run the client:
```bash
python kmip_client.py
```

The client will:
1. Create a new symmetric key
2. Retrieve the created key
3. Rotate the key
4. Retrieve the rotated key
5. Destroy both keys

### Customizing the Client

You can modify the client configuration by changing the parameters in the `KmipClient` initialization:

```python
client = KmipClient(
    host='your-server-host',
    port=5696  # Default KMIP port
)
```

## Error Handling

The client includes basic error handling and logging. All operations are logged to the console with appropriate log levels (INFO for successful operations, ERROR for failures).

## Security Notes

- This example client does not use SSL/TLS. For production use, you should configure proper SSL/TLS certificates.
- The client uses default cryptographic parameters. Adjust these based on your security requirements.

## Python Version

This client is specifically designed to work with Python 3.11.6. Using other Python versions may result in compatibility issues.

## Troubleshooting

### Python Not Found
If `where python` returns nothing:

1. Verify Python installation:
```cmd
python --version
# or
py --version
```

2. If Python is not found, add it to your system PATH:
   - Open Windows Settings
   - Search for "Environment Variables"
   - Click "Edit the system environment variables"
   - Click "Environment Variables" button
   - Under "System Variables", find and select "Path"
   - Click "Edit"
   - Click "New"
   - Add these paths (adjust based on your Python installation):
     ```
     C:\Users\YourUsername\AppData\Local\Programs\Python\Python311
     C:\Users\YourUsername\AppData\Local\Programs\Python\Python311\Scripts
     ```
   - Close and reopen Command Prompt
   - Verify Python is now in PATH:
     ```cmd
     where python
     ```

### PowerShell Execution Policy Error
If you encounter the error "running scripts is disabled on this system", you have two options:

1. Run PowerShell as Administrator and execute:
```powershell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

2. Or use Command Prompt instead of PowerShell:
```cmd
.\venv\Scripts\activate.bat
```

### Virtual Environment Activation Issues
If the virtual environment doesn't appear to activate (no (venv) prefix in prompt):

1. Verify you're in the correct directory:
```cmd
cd C:\path\to\kmip_client
```

2. Check if the virtual environment was created properly:
```cmd
dir venv\Scripts
```
You should see `activate.bat` and `python.exe` in this directory.

3. Try running the activation script with the full path:
```cmd
C:\path\to\kmip_client\venv\Scripts\activate.bat
```

4. Verify Python path after activation:
```cmd
where python
```
This should show the path to Python in your venv directory.

5. If still not working, try creating a new virtual environment:
```cmd
python -m venv venv_new
.\venv_new\Scripts\activate.bat
```

6. Check if the activation script is being executed:
```cmd
echo %PATH%
```
Look for the venv\Scripts directory in the PATH.

### Verifying Virtual Environment
To check if your virtual environment is active:

1. Look for `(venv)` at the beginning of your command prompt
2. Run `where python` - should show venv\Scripts\python.exe
3. Run `python -c "import sys; print(sys.executable)"` - should show venv path
4. Run `pip list` - should show only packages installed in venv 