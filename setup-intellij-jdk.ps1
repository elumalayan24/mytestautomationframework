# PowerShell script to set up IntelliJ IDEA with portable JDK
Write-Host "Setting up IntelliJ IDEA with portable JDK..." -ForegroundColor Green

# Set environment variables for this session
$env:JAVA_HOME = "C:\JDK\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verify Java setup
Write-Host "Java Home: $env:JAVA_HOME" -ForegroundColor Yellow
& java -version

Write-Host ""
Write-Host "IntelliJ IDEA Configuration Steps:" -ForegroundColor Cyan
Write-Host "1. Open IntelliJ IDEA"
Write-Host "2. Go to File → Project Structure → Platform Settings → SDKs"
Write-Host "3. Click '+' → Add SDK → JDK"
Write-Host "4. Navigate to: C:\JDK\jdk-21"
Write-Host "5. Click OK to add the JDK"
Write-Host "6. Go to Project Settings → Project"
Write-Host "7. Select the new JDK (C:\JDK\jdk-21) as Project SDK"
Write-Host "8. Click Apply → OK"
Write-Host ""
Write-Host "This will permanently fix the 'Access is denied' error!" -ForegroundColor Green
Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
