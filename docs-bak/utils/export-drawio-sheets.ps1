$DIR_DRAWIO = "..\.vuepress\public\img\figures\"

$DrawIoFiles = Get-ChildItem $DIR_DRAWIO *.drawio -File

foreach ($file in $DrawIoFiles) {
    
    "File: '$($file.FullName)'"
    
    $xml_file = "$($file.DirectoryName)/$($file.BaseName).xml"

    if ((Test-Path $xml_file)) {
        
        Remove-Item -Path $xml_file -Force
    }
    
    # Export to XML
    & "C:/Program Files/draw.io/draw.io.exe" '--export' '--format' 'xml' $file.FullName

    # Wait for XML file creation
    while ($true) {
        if (-not (Test-Path $xml_file)) {
            Start-Sleep -Milliseconds 200
        }
        else {
            break
        }
    }
    # Load to XML Document (cast text array to object)
    $drawio_xml = [xml](Get-Content $xml_file)

    # For each page export png
    for ($i = 0; $i -lt $drawio_xml.mxfile.pages; $i++) {
        
        $file_out = "$($file.DirectoryName)/$($file.BaseName)-$($i + 1).png"

        & "C:/Program Files/draw.io/draw.io.exe" '--export' '--border' '10' '--page-index' $i '--output' $file_out $file.FullName
    }

    # Wait for last file PNG image file
    while ($true) {
        if (-not (Test-Path "$($file.DirectoryName)/$($file.BaseName)$($drawio_xml.mxfile.pages).png")) {
            Start-Sleep -Milliseconds 200
        }
        else {
            break
        }
    }
    # Remove XML file
    if ((Test-Path $xml_file)) {
        
        Remove-Item -Path $xml_file -Force
    }
}