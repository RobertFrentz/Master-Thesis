$files = Get-ChildItem -Path '/data/csvmetrics' -File -Filter '*:*'
foreach ($file in $files) {
    $newName = $file.Name -replace ':', '-'
    Rename-Item -Path $file.FullName -NewName $newName
}
