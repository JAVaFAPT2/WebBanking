# Create gradle/wrapper directory if it doesn't exist
New-Item -ItemType Directory -Force -Path "gradle/wrapper"

# Download gradle-wrapper.jar
$wrapperJarUrl = "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
$wrapperJarPath = "gradle/wrapper/gradle-wrapper.jar"
Invoke-WebRequest -Uri $wrapperJarUrl -OutFile $wrapperJarPath

# Create gradle-wrapper.properties
$wrapperProperties = @"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@
Set-Content -Path "gradle/wrapper/gradle-wrapper.properties" -Value $wrapperProperties

# Download gradlew.bat
$gradlewBatUrl = "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradlew.bat"
Invoke-WebRequest -Uri $gradlewBatUrl -OutFile "gradlew.bat"

# Download gradlew
$gradlewUrl = "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradlew"
Invoke-WebRequest -Uri $gradlewUrl -OutFile "gradlew"

Write-Host "Gradle wrapper files have been downloaded successfully."
Write-Host "You can now run './gradlew.bat' or './gradlew' to use Gradle." 