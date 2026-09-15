$xmlPath = "c:\Silvere\Encours\Developpement\Episteme\reports\TEST-org.episteme.benchmarks.test.audit.LinearAlgebraComplianceTest.xml"
if (Test-Path $xmlPath) {
    $content = [System.IO.File]::ReadAllText($xmlPath)
    $matches = [regex]::Matches($content, "(?i)[^\n]{0,200}Found symbol[^\n]{0,200}")
    Write-Host "Found $($matches.Count) matches:"
    # Only print first 20 matches to avoid giant output
    $limit = [Math]::Min($matches.Count, 20)
    for ($i = 0; $i -lt $limit; $i++) {
        Write-Host $matches[$i].Value
        Write-Host "--------------------"
    }
} else {
    Write-Host "XML file not found"
}
