# Episteme Hugging Face Deployment Script (Industrial Version)
# This script creates a lean deployment branch and pushes it to Hugging Face.

$HF_REMOTE = "hf"
$DEPLOY_BRANCH = "hf-deployment"

Write-Host "--- Starting Episteme HF Deployment ---" -ForegroundColor Cyan

# 1. Ensure we are in a clean state and synced with GitHub
$currentBranch = git rev-parse --abbrev-ref HEAD
if ($null -eq $currentBranch) { Write-Error "Not in a git repo"; exit }
Write-Host "Syncing $currentBranch with GitHub..." -ForegroundColor Yellow
git push origin $currentBranch

# 2. Create or reset the deployment branch (Orphan to save space)
Write-Host "Creating lean deployment branch..." -ForegroundColor Yellow
if (git branch | Select-String $DEPLOY_BRANCH) {
    git branch -D $DEPLOY_BRANCH
}
git checkout --orphan $DEPLOY_BRANCH

# 3. Clear the index (it starts with all files from previous branch)
git rm -rf . --cached > $null

# 4. Add ONLY the essential modules (The Scientific Engine)
Write-Host "Adding essential modules..." -ForegroundColor Yellow

# Temporarily copy infrastructure folders to root for HF compatibility
Copy-Item -Path "infrastructure/agent" -Destination "agent" -Recurse -Force
Copy-Item -Path "infrastructure/docker" -Destination "docker" -Recurse -Force
Copy-Item -Path "infrastructure/docker/Dockerfile.huggingface" -Destination "Dockerfile" -Force

git add .gitattributes
git add Dockerfile
git add pom.xml
git add README.md
git add .gitignore
git add episteme-core/
git add episteme-server/
git add episteme-natural/
git add episteme-social/
git add episteme-native/
git add agent/
git add docker/

# Remove heavy 3D models and textures rejected by Hugging Face
Write-Host "Pruning heavy 3D assets, textures, and native binaries..." -ForegroundColor Yellow
git rm -rf --cached episteme-natural/src/main/resources/org/episteme/natural/medicine/anatomy/models/*.fbx 2>$null
git rm -rf --cached episteme-natural/src/main/resources/org/episteme/natural/physics/astronomy/*.jpg 2>$null
git rm -rf --cached episteme-natural/src/main/resources/org/episteme/natural/physics/astronomy/*.tif 2>$null
git rm -rf --cached episteme-core/src/main/resources/org/episteme/core/ui/*.png 2>$null
git rm -rf --cached episteme-native/libs/*.dll 2>$null
git rm -rf --cached episteme-native/.discovery_cache 2>$null
git rm -rf --cached episteme-server/.discovery_cache 2>$null

# 5. Commit the lean version
Write-Host "Committing lean version..." -ForegroundColor Yellow
git commit -m "deploy: industrial lean build for Hugging Face Spaces"

# 6. Push to Hugging Face
Write-Host "Pushing to Hugging Face..." -ForegroundColor Green
git push $HF_REMOTE $DEPLOY_BRANCH:main --force

# 7. Cleanup and return to main branch
Write-Host "Cleaning up..." -ForegroundColor Yellow
Remove-Item -Path "agent" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "docker" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "Dockerfile" -Force -ErrorAction SilentlyContinue

git checkout -f $currentBranch
git branch -D $DEPLOY_BRANCH

Write-Host "--- Deployment Complete! ---" -ForegroundColor Green
Write-Host "Monitor your build at: https://huggingface.co/spaces/silveremartin/Episteme"
