$csvFiles = Get-ChildItem -Path "C:/GrafanaCsvMetrics" -Filter "*.csv"

foreach ($csvFile in $csvFiles) {
    $csvData = Import-Csv -Path $csvFile.FullName
    $newCsvData = foreach ($row in $csvData) {
        $timestamp = [DateTimeOffset]::FromUnixTimeSeconds([int]$row.'t')
        $row.'t' = $timestamp.ToString("yyyy-MM-dd HH:mm:ss")
        $row
    }
    $newCsvData | Export-Csv -Path $csvFile.FullName -NoTypeInformation
}
