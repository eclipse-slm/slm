New-Item -ItemType Directory -Path .\release-zip
Copy-Item -Path (Get-Item -Path ".\*" -Exclude ('release-zip','*.ps1','*.zip','build.yml')).FullName -Destination .\release-zip -Recurse -Force
Remove-Item .\release-zip\config\_config_generated

Compress-Archive -CompressionLevel Optimal -Force -Path .\release-zip\* -DestinationPath .\eclipse-slm-setup-compose.zip

Remove-Item -Recurse .\release-zip