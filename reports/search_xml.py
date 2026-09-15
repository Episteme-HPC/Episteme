import os

xml_path = r"c:\Silvere\Encours\Developpement\Episteme\reports\TEST-org.episteme.benchmarks.test.audit.LinearAlgebraComplianceTest.xml"
if os.path.exists(xml_path):
    with open(xml_path, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
    
    idx = 0
    while True:
        idx = content.find("CUDADense", idx)
        if idx == -1:
            break
        print("--- MATCH AT INDEX", idx, "---")
        start = max(0, idx - 400)
        end = min(len(content), idx + 600)
        print(content[start:end])
        print("\n")
        idx += 1
else:
    print("XML file not found at:", xml_path)
